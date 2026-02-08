package com.example.ebank.auth.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.observability.SecurityEventLogger;

import io.jsonwebtoken.ExpiredJwtException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final SecurityEventLogger sec;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, SecurityEventLogger sec) {
    this.jwtUtil = jwtUtil;
    this.sec = sec;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    // ヘッダがない or "Bearer " じゃなければスルー
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    try {
      // 署名＆expチェック（JwtUtil側のvalidateでもOK）
      if (!jwtUtil.validateToken(token)) {
        sec.emit(
            "AUTH_TOKEN_INVALID",
            "LOW",
            "anonymous",
            request.getRemoteAddr(),
            Map.of("path", request.getRequestURI())
        );
        filterChain.doFilter(request, response);
        return;
      }

      String username = jwtUtil.extractUsername(token);
      Long userId = jwtUtil.extractUserId(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // principal は username のまま（既存コードが getName() 前提でも壊れにくい）
        // credentials は null
        // authorities は空（role無し）
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());

        // details に userId を載せておく（後で取り出せる）
        // 既存の details を壊さないよう、Mapでまとめて格納
        var details = new WebAuthenticationDetailsSource().buildDetails(request);
        authToken.setDetails(Map.of(
            "web", details,
            "userId", userId
        ));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

    } catch (ExpiredJwtException e) {
      sec.emit(
          "AUTH_TOKEN_EXPIRED",
          "LOW",
          "anonymous",
          request.getRemoteAddr(),
          Map.of("path", request.getRequestURI())
      );
    } catch (Exception e) {
      sec.emit(
          "AUTH_TOKEN_INVALID",
          "LOW",
          "anonymous",
          request.getRemoteAddr(),
          Map.of("path", request.getRequestURI())
      );
    }

    filterChain.doFilter(request, response);
  }
}
