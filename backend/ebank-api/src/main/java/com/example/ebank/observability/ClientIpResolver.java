package com.example.ebank.observability;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public final class ClientIpResolver {

    private static final Pattern FORWARDED_FOR_PATTERN = Pattern.compile("for=([^;,$]+)", Pattern.CASE_INSENSITIVE);
    private static final String[] FORWARDED_HEADERS = {
            "X-Forwarded-For",
            "X-Original-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "True-Client-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        for (String header : FORWARDED_HEADERS) {
            String value = firstNonBlank(request.getHeader(header));
            if (!value.isEmpty()) {
                String resolved = extractClientIp(value);
                if (!resolved.isEmpty()) {
                    return resolved;
                }
            }
        }

        String forwarded = firstNonBlank(request.getHeader("Forwarded"));
        if (!forwarded.isEmpty()) {
            Matcher m = FORWARDED_FOR_PATTERN.matcher(forwarded);
            if (m.find()) {
                String resolved = extractClientIp(m.group(1));
                if (!resolved.isEmpty()) {
                    return resolved;
                }
            }
        }

        String remoteAddr = firstNonBlank(request.getRemoteAddr());
        return remoteAddr;
    }

    private static String extractClientIp(String headerValue) {
        String[] parts = headerValue.split(",");
        for (String part : parts) {
            String ip = sanitizeIpToken(part);
            if (!ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return "";
    }

    private static String sanitizeIpToken(String value) {
        String token = firstNonBlank(value);
        if (token.isEmpty()) {
            return "";
        }
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            token = token.substring(1, token.length() - 1).trim();
        }
        if (token.startsWith("[")) {
            int end = token.indexOf(']');
            if (end > 1) {
                return token.substring(1, end).trim();
            }
        }
        int colonCount = 0;
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == ':') {
                colonCount++;
            }
        }
        if (colonCount == 1) {
            int lastColon = token.lastIndexOf(':');
            String host = token.substring(0, lastColon).trim();
            String port = token.substring(lastColon + 1).trim();
            if (!host.isEmpty() && port.matches("\\d+")) {
                return host;
            }
        }
        return token;
    }

    private static String firstNonBlank(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }
}

