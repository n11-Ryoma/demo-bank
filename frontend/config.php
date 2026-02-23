<?php
// config.php

session_start();

define('API_BASE_URL', 'http://localhost:8080'); // Spring Boot API URL
//define('API_BASE_URL', 'http://192.168.1.76:8080');

function current_request_id()
{
    static $rid = null;
    if ($rid !== null) {
        return $rid;
    }

    $incoming = isset($_SERVER['HTTP_X_REQUEST_ID']) ? trim((string)$_SERVER['HTTP_X_REQUEST_ID']) : '';
    if ($incoming !== '') {
        $rid = $incoming;
    } else {
        try {
            $rid = bin2hex(random_bytes(16));
        } catch (Exception $e) {
            $rid = uniqid('rid_', true);
        }
    }

    if (!headers_sent()) {
        header('X-Request-Id: ' . $rid);
    }
    return $rid;
}

function java_api_path_only($path)
{
    $pathOnly = parse_url((string)$path, PHP_URL_PATH);
    if (!is_string($pathOnly) || $pathOnly === '') {
        return '/';
    }
    return $pathOnly;
}

function valid_ip_or_empty($value)
{
    $ip = trim((string)$value);
    if ($ip === '' || filter_var($ip, FILTER_VALIDATE_IP) === false) {
        return '';
    }
    return $ip;
}

function parse_ip_token_for_java($value)
{
    $token = trim((string)$value);
    if ($token === '') {
        return '';
    }

    if (strlen($token) >= 2 && $token[0] === '"' && $token[strlen($token) - 1] === '"') {
        $token = trim(substr($token, 1, -1));
    }

    if ($token !== '' && $token[0] === '[') {
        $end = strpos($token, ']');
        if ($end !== false && $end > 1) {
            $token = substr($token, 1, $end - 1);
        }
    } else {
        $colonCount = substr_count($token, ':');
        if ($colonCount === 1) {
            $pos = strrpos($token, ':');
            $host = trim(substr($token, 0, $pos));
            $port = trim(substr($token, $pos + 1));
            if ($host !== '' && ctype_digit($port)) {
                $token = $host;
            }
        }
    }

    return valid_ip_or_empty($token);
}

function incoming_forwarded_for_chain()
{
    $ips = [];

    $xff = isset($_SERVER['HTTP_X_FORWARDED_FOR']) ? (string)$_SERVER['HTTP_X_FORWARDED_FOR'] : '';
    if ($xff !== '') {
        foreach (explode(',', $xff) as $part) {
            $ip = parse_ip_token_for_java($part);
            if ($ip !== '') {
                $ips[] = $ip;
            }
        }
    }

    $forwarded = isset($_SERVER['HTTP_FORWARDED']) ? (string)$_SERVER['HTTP_FORWARDED'] : '';
    if ($forwarded !== '' && preg_match_all('/for=([^;,$]+)/i', $forwarded, $matches)) {
        foreach ($matches[1] as $part) {
            $ip = parse_ip_token_for_java($part);
            if ($ip !== '') {
                $ips[] = $ip;
            }
        }
    }

    $unique = [];
    foreach ($ips as $ip) {
        if ($ip !== '') {
            $unique[$ip] = true;
        }
    }

    return array_keys($unique);
}

function client_ip_for_java()
{
    $forwarded = incoming_forwarded_for_chain();
    if (!empty($forwarded)) {
        return $forwarded[0];
    }

    $candidates = [
        isset($_SERVER['HTTP_X_REAL_IP']) ? $_SERVER['HTTP_X_REAL_IP'] : '',
        isset($_SERVER['HTTP_TRUE_CLIENT_IP']) ? $_SERVER['HTTP_TRUE_CLIENT_IP'] : '',
        isset($_SERVER['HTTP_CF_CONNECTING_IP']) ? $_SERVER['HTTP_CF_CONNECTING_IP'] : '',
        isset($_SERVER['HTTP_X_CLIENT_IP']) ? $_SERVER['HTTP_X_CLIENT_IP'] : '',
        isset($_SERVER['HTTP_CLIENT_IP']) ? $_SERVER['HTTP_CLIENT_IP'] : '',
        isset($_SERVER['REMOTE_ADDR']) ? $_SERVER['REMOTE_ADDR'] : '',
    ];

    foreach ($candidates as $candidate) {
        $ip = parse_ip_token_for_java($candidate);
        if ($ip !== '') {
            return $ip;
        }
    }

    return '';
}

function forwarded_for_for_java()
{
    $chain = incoming_forwarded_for_chain();
    $remoteAddr = valid_ip_or_empty(isset($_SERVER['REMOTE_ADDR']) ? $_SERVER['REMOTE_ADDR'] : '');

    if ($remoteAddr !== '' && (empty($chain) || $chain[count($chain) - 1] !== $remoteAddr)) {
        $chain[] = $remoteAddr;
    }

    return implode(', ', $chain);
}

function log_php_to_java($requestId, $method, $path, $status, $latencyMs, $result, $clientIpSent = '', $xffSent = '')
{
    $entry = [
        'type' => 'PHP_TO_JAVA',
        'ts' => date('c'),
        'request_id' => $requestId,
        'src' => 'php',
        'dst' => 'java',
        'method' => strtoupper((string)$method),
        'url' => java_api_path_only((string)$path),
        'status' => (int)$status,
        'latency_ms' => (int)$latencyMs,
        'result' => $result === 'ok' ? 'ok' : 'fail',
        'frontend_remote_addr' => isset($_SERVER['REMOTE_ADDR']) ? (string)$_SERVER['REMOTE_ADDR'] : '',
        'frontend_incoming_xff' => isset($_SERVER['HTTP_X_FORWARDED_FOR']) ? (string)$_SERVER['HTTP_X_FORWARDED_FOR'] : '',
        'frontend_incoming_forwarded' => isset($_SERVER['HTTP_FORWARDED']) ? (string)$_SERVER['HTTP_FORWARDED'] : '',
        'to_java_x_real_ip' => (string)$clientIpSent,
        'to_java_x_forwarded_for' => (string)$xffSent,
    ];
    error_log(json_encode($entry, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES));
}

function api_request($method, $path, $body = null, $needAuth = false)
{
    $url = rtrim(API_BASE_URL, '/') . $path;
    $requestId = current_request_id();
    $start = microtime(true);

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    // Timeouts to avoid hanging requests
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 3);
    curl_setopt($ch, CURLOPT_TIMEOUT, 6);

    $headers = ['Content-Type: application/json'];

    if ($needAuth && isset($_SESSION['jwt_token'])) {
        $headers[] = 'Authorization: Bearer ' . $_SESSION['jwt_token'];
    }
    $headers[] = 'X-Request-Id: ' . $requestId;

    $clientIp = client_ip_for_java();
    if ($clientIp !== '') {
        $headers[] = 'X-Real-IP: ' . $clientIp;
    }

    $xff = forwarded_for_for_java();
    if ($xff !== '') {
        $headers[] = 'X-Forwarded-For: ' . $xff;
    }

    if ($body !== null) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($body, JSON_UNESCAPED_UNICODE));
    }

    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

    $responseBody = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $latencyMs = (int)round((microtime(true) - $start) * 1000);

    if ($responseBody === false) {
        $error = curl_error($ch);
        curl_close($ch);
        log_php_to_java($requestId, $method, $path, 0, $latencyMs, 'fail', $clientIp, $xff);
        throw new Exception("cURL error: " . $error);
    }

    curl_close($ch);

    $result = ($httpCode >= 200 && $httpCode < 400) ? 'ok' : 'fail';
    log_php_to_java($requestId, $method, $path, $httpCode, $latencyMs, $result, $clientIp, $xff);

    $decoded = json_decode($responseBody, true);
    return [
        'status' => $httpCode,
        'body' => $decoded,
        'raw' => $responseBody,
    ];
}

// Flash message helper
function flash($key)
{
    if (!isset($_SESSION[$key])) {
        return null;
    }
    $msg = $_SESSION[$key];
    unset($_SESSION[$key]);
    return $msg;
}
