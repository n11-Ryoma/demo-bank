package com.example.ebank.observability;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuditLogger {

  private static final Logger AUDIT = LogManager.getLogger("AUDIT");
  private final ObjectMapper om;

  public AuditLogger(ObjectMapper om) {
    this.om = om;
  }

  public void success(String action, String actor, String target, Long amount, HttpMeta meta, Map<String, Object> extra) {
    write(build("SUCCESS", null, action, actor, target, amount, meta, extra));
  }

  public void fail(String action, String actor, String target, Long amount, String reason, HttpMeta meta, Map<String, Object> extra) {
    write(build("FAIL", reason, action, actor, target, amount, meta, extra));
  }

  private Map<String, Object> build(String result, String reason, String action, String actor,
                                    String target, Long amount, HttpMeta meta, Map<String, Object> extra) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("type", "AUDIT");
    m.put("ts", OffsetDateTime.now().toString());
    m.put("requestId", ThreadContext.get("requestId"));

    m.put("action", action);
    m.put("result", result);
    if (reason != null) m.put("reason", reason);

    m.put("actor", (actor == null || actor.isBlank()) ? "anonymous" : actor);
    if (target != null) m.put("target", target);
    if (amount != null) m.put("amount", amount);

    if (meta != null) {
      m.put("path", meta.path());
      m.put("method", meta.method());
      m.put("status", meta.status());
      m.put("remoteIp", meta.remoteIp());
      m.put("userAgent", meta.userAgent());
      m.put("latencyMs", meta.latencyMs());
    }

    if (extra != null && !extra.isEmpty()) m.put("extra", extra);
    return m;
  }

  private void write(Map<String, Object> m) {
    try {
      AUDIT.info(om.writeValueAsString(m));
    } catch (JsonProcessingException e) {
      AUDIT.info("{\"type\":\"AUDIT\",\"result\":\"FAIL\",\"reason\":\"JSON_SERIALIZE_ERROR\"}");
    }
  }
}
