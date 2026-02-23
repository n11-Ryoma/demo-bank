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

function log_php_to_java($requestId, $method, $path, $status, $latencyMs, $result)
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
        log_php_to_java($requestId, $method, $path, 0, $latencyMs, 'fail');
        throw new Exception("cURL error: " . $error);
    }

    curl_close($ch);

    $result = ($httpCode >= 200 && $httpCode < 400) ? 'ok' : 'fail';
    log_php_to_java($requestId, $method, $path, $httpCode, $latencyMs, $result);

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
