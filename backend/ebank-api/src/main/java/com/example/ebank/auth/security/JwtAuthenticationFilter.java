package com.example.ebank.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
      String username = jwtUtil.extractUsername(token);
      List<String> roles = jwtUtil.extractRoles(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

    } catch (ExpiredJwtException e) {
      // 期限切れは「検知ログ」に出す（Blueがルール化しやすい）
      sec.emit(
          "AUTH_TOKEN_EXPIRED",
          "LOW",
          "anonymous",
          request.getRemoteAddr(),
          Map.of("path", request.getRequestURI())
      );
    } catch (Exception e) {
      // 不正トークンも検知ログへ
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
