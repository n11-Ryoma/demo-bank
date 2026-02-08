package com.example.ebank.auth.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.dto.AuthResponse;
import com.example.ebank.auth.dto.LoginRequest;
import com.example.ebank.auth.dto.RegisterRequest;
import com.example.ebank.auth.dto.RegisterResponse;
import com.example.ebank.auth.entity.User;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.auth.security.LoginFailureTracker;
import com.example.ebank.auth.service.AuthService;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;
import com.example.ebank.observability.SecurityEventLogger;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger log = LogManager.getLogger(AuthController.class);

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  // 追加
  private final AuditLogger audit;
  private final SecurityEventLogger sec;
  private final LoginFailureTracker failureTracker;

  public AuthController(AuthService authService,
                        JwtUtil jwtUtil,
                        AuditLogger audit,
                        SecurityEventLogger sec,
                        LoginFailureTracker failureTracker) {
    this.authService = authService;
    this.jwtUtil = jwtUtil;
    this.audit = audit;
    this.sec = sec;
    this.failureTracker = failureTracker;
  }
  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest request, HttpServletRequest httpReq) {

    long start = System.nanoTime();
    String ip = httpReq.getRemoteAddr();
    String ua = httpReq.getHeader("User-Agent");
    String actor = request.getUsername();
    String key = ip + ":" + actor;

    log.info("Login attempt: {}", actor);

    List<User> users = authService.loginWeak(request.getUsername(), request.getPassword());

    // 失敗
    if (users.isEmpty()) {
      int cnt = failureTracker.recordFailure(key);

      // 失敗バースト検知（Blueがアラート化しやすい）
      if (cnt >= failureTracker.threshold()) {
        sec.emit(
            "AUTH_LOGIN_FAILED_BURST",
            "MEDIUM",
            actor,
            ip,
            Map.of("count", cnt, "windowSec", failureTracker.windowSec())
        );
      }

      long latencyMs = (System.nanoTime() - start) / 1_000_000;
      audit.fail(
          "AUTH_LOGIN",
          actor,
          null,
          null,
          "INVALID_CREDENTIALS",
          new HttpMeta("/api/auth/login", "POST", 401, ip, ua == null ? "" : ua, latencyMs),
          Map.of("failCount", cnt)
      );

      // ※元コードは RuntimeException だったが、演習では 401 を返せる方がログと整合する
      // 既存挙動を変えたくなければ RuntimeException に戻してOK
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.UNAUTHORIZED,
          "Invalid username or password"
      );
    }

    // ★ 脆弱点：複数ユーザが返ったら勝手に先頭を採用
    if (users.size() >= 2) {
      sec.emit(
          "AUTH_LOGIN_MULTIPLE_USERS",
          "HIGH",
          actor,
          ip,
          Map.of("count", users.size())
      );
    }

    // 成功
    failureTracker.clear(key);

    User user = users.get(0);

    // JWT 発行
    String token = jwtUtil.generateToken(user.getId(), user.getUsername());

    long latencyMs = (System.nanoTime() - start) / 1_000_000;
    audit.success(
        "AUTH_LOGIN",
        user.getUsername(),
        null,
        null,
        new HttpMeta("/api/auth/login", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
        Map.of("userId", user.getId())
    );

    return new AuthResponse(token, "success");
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpReq) {
    long start = System.nanoTime();
    String ip = httpReq.getRemoteAddr();
    String ua = httpReq.getHeader("User-Agent");

    RegisterResponse res = authService.register(request);

    long latencyMs = (System.nanoTime() - start) / 1_000_000;
    audit.success(
        "AUTH_REGISTER",
        res.getUsername(),
        res.getAccountNumber(),
        null,
        new HttpMeta("/api/auth/register", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
        Map.of()
    );

    return ResponseEntity.ok(res);
  }
}
