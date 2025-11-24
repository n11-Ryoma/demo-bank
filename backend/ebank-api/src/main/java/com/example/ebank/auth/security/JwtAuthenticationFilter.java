package com.example.ebank.auth.security;

import java.io.IOException;
import java.util.List;
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

import io.jsonwebtoken.ExpiredJwtException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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
            // 期限切れなどのハンドリング。今回はスルーして 401 を後段に任せる
        } catch (Exception e) {
            // 不正トークンなども一旦スルー（必要なら 401 返す実装にしてもOK）
        }

        filterChain.doFilter(request, response);
    }
}
