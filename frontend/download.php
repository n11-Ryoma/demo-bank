<?php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$baseDir = __DIR__ . '/exports/';

// CT Fっぽくするなら、ここをあえて緩くするのもアリ
$file = $_GET['file'] ?? '';   // ← これがLFI入口になり得るやつ

$path = $baseDir . $file;

if (!is_file($path)) {
    http_response_code(404);
    echo 'ファイルが見つかりません';
    exit;
}

header('Content-Type: text/csv; charset=UTF-8');
header('Content-Disposition: attachment; filename="' . basename($file) . '"');
readfile($path);
exit;
