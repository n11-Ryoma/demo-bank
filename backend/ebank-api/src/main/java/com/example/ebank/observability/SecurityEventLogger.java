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
public class SecurityEventLogger {

  private static final Logger SEC = LogManager.getLogger("SECURITY_EVENT");
  private final ObjectMapper om;

  public SecurityEventLogger(ObjectMapper om) {
    this.om = om;
  }

  public void emit(String name, String severity, String actor, String sourceIp, Map<String, Object> fields) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("type", "SECURITY_EVENT");
    m.put("ts", OffsetDateTime.now().toString());
    m.put("requestId", ThreadContext.get("requestId"));

    m.put("name", name);
    m.put("severity", severity);
    m.put("actor", (actor == null || actor.isBlank()) ? "anonymous" : actor);
    m.put("sourceIp", sourceIp);
    if (fields != null && !fields.isEmpty()) m.put("fields", fields);

    try {
      SEC.warn(om.writeValueAsString(m));
    } catch (JsonProcessingException e) {
      SEC.warn("{\"type\":\"SECURITY_EVENT\",\"name\":\"JSON_SERIALIZE_ERROR\",\"severity\":\"LOW\"}");
    }
  }
}
