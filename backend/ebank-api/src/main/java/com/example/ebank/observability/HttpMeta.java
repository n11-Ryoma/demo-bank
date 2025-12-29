package com.example.ebank.observability;

public record HttpMeta(
    String path,
    String method,
    int status,
    String remoteIp,
    String userAgent,
    long latencyMs
) {}
