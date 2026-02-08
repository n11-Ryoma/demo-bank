<?php
// config.php

session_start();

//define('API_BASE_URL', 'http://localhost:8080'); // Spring BootのURL
define('API_BASE_URL', 'http://192.168.1.76:8080');

function api_request($method, $path, $body = null, $needAuth = false)
{
    $url = rtrim(API_BASE_URL, '/') . $path;

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);

    $headers = ['Content-Type: application/json'];

    if ($needAuth && isset($_SESSION['jwt_token'])) {
        $headers[] = 'Authorization: Bearer ' . $_SESSION['jwt_token'];
    }

    if ($body !== null) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($body, JSON_UNESCAPED_UNICODE));
    }

    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

    $responseBody = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if ($responseBody === false) {
        $error = curl_error($ch);
        curl_close($ch);
        throw new Exception("cURL error: " . $error);
    }

    curl_close($ch);

    $decoded = json_decode($responseBody, true);
    return [
        'status' => $httpCode,
        'body'   => $decoded,
        'raw'    => $responseBody,
    ];
}

// フラッシュメッセージ用ヘルパ
function flash($key)
{
    if (!isset($_SESSION[$key])) return null;
    $msg = $_SESSION[$key];
    unset($_SESSION[$key]);
    return $msg;
}
