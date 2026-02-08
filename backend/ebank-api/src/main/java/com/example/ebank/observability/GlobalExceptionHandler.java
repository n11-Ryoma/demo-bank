package com.example.ebank.observability;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus();
        logByStatus(status, req, ex);
        String reason = ex.getReason();
        return ResponseEntity.status(status).body(reason == null ? status.getReasonPhrase() : reason);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<String> handleBadRequest(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByStatus(status, req, ex);
        return ResponseEntity.status(status).body(status.getReasonPhrase());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logByStatus(status, req, ex);
        return ResponseEntity.status(status).body(status.getReasonPhrase());
    }

    private void logByStatus(HttpStatus status, HttpServletRequest req, Exception ex) {
        String path = req.getRequestURI();
        String method = req.getMethod();
        String ip = req.getRemoteAddr();
        String ua = req.getHeader("User-Agent");
        String msg = String.format("HTTP %d %s %s ip=%s ua=%s ex=%s",
                status.value(), method, path, ip, ua == null ? "" : ua, ex.getClass().getSimpleName());

        if (status.is4xxClientError()) {
            log.warn(msg);
        } else {
            log.error(msg, ex);
        }
    }
}
