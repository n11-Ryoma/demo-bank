package com.example.ebank.auth.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureTracker {

  private static final Logger log = LogManager.getLogger(LoginFailureTracker.class);
  private static final int THRESHOLD = 5;
  private static final long WINDOW_SEC = 60;

  private final Map<String, Deque<Long>> map = new ConcurrentHashMap<>();

  public int recordFailure(String key) {
    long now = Instant.now().getEpochSecond();
    Deque<Long> q = map.computeIfAbsent(key, k -> new ArrayDeque<>());
    synchronized (q) {
      q.addLast(now);
      while (!q.isEmpty() && q.peekFirst() < now - WINDOW_SEC) {
        q.removeFirst();
      }
      log.info("recordFailure: key={}, count={}", key, q.size());
      return q.size();
    }
  }

  public void clear(String key) {
    log.info("clear failures: key={}", key);
    map.remove(key);
  }

  public int threshold() { return THRESHOLD; }
  public long windowSec() { return WINDOW_SEC; }
}
