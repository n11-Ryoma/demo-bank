<?php
require_once 'config.php';

function h($value)
{
    return htmlspecialchars((string)$value, ENT_QUOTES, 'UTF-8');
}

function require_login(): void
{
    if (!isset($_SESSION['jwt_token'])) {
        header('Location: login.php');
        exit;
    }
}

function api_error_message(array $res, string $fallback): string
{
    if (is_array($res['body'] ?? null)) {
        $body = $res['body'];
        if (isset($body['message']) && is_string($body['message'])) {
            return $body['message'];
        }
        if (isset($body['error']) && is_string($body['error'])) {
            return $body['error'];
        }
    }

    if (is_string($res['raw'] ?? null) && trim($res['raw']) !== '') {
        return trim($res['raw']);
    }

    return $fallback;
}

function api_auth_request(string $method, string $path, ?array $body = null): array
{
    try {
        return api_request($method, $path, $body, true);
    } catch (Exception $e) {
        return [
            'status' => 0,
            'body' => null,
            'raw' => '',
            'exception' => $e->getMessage(),
        ];
    }
}

