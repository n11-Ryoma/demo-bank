package com.example.ebank.observability;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * X-Request-Id を受け取り（無ければ生成）、
 * Log4j2 の ThreadContext に requestId をセットする。
 * レスポンスにも同じ X-Request-Id を返す。
 */
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Request-Id";
  public static final String CTX_KEY = "requestId";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String rid = Optional.ofNullable(request.getHeader(HEADER))
        .filter(s -> !s.isBlank())
        .orElseGet(() -> UUID.randomUUID().toString());

    ThreadContext.put(CTX_KEY, rid);
    response.setHeader(HEADER, rid);

    try {
      chain.doFilter(request, response);
    } finally {
      ThreadContext.remove(CTX_KEY);
    }
  }
}
