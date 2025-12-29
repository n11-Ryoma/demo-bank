package com.example.ebank.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.observability.SecurityEventLogger;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil, SecurityEventLogger sec) throws Exception {

    http.csrf(csrf -> csrf.disable());

    // JWT Filter を chain に追加（permitAllでも「ヘッダがあれば認証情報を作る」だけなので壊れにくい）
    http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, sec), UsernamePasswordAuthenticationFilter.class);

    http.authorizeHttpRequests(auth -> auth
        .antMatchers("/api/auth/**").permitAll()
        .antMatchers("/api/accounts/**").permitAll()
        .antMatchers("/v3/api-docs/**").permitAll()
        .antMatchers("/swagger-ui/**").permitAll()
        .antMatchers("/swagger-ui.html").permitAll()
        .anyRequest().permitAll()
    );

    return http.build();
  }
}
