<?php
// preview.php
// 本来は厳密なパスチェックをすべきだが、デモ／CTF 用にあえてガバガバ実装にしている

$file = $_GET['file'] ?? '';

if ($file === '') {
    http_response_code(400);
    echo 'file parameter is required';
    exit;
}

// アップロード用のベースディレクトリ
// 例: /var/www/html/uploads/
// ここからの相対パスとして扱うつもり…だが、realpath等でのチェックはしていないのでLFIになる
$baseDir = __DIR__ . '/uploads/';

// ★ そのまま連結（../ を許してしまうパターン）
$target = $baseDir . $file;

// 存在チェックだけはしておく
if (!file_exists($target)) {
    http_response_code(404);
    echo 'file not found';
    exit;
}

// Content-Type は雑に処理（画像でもテキストでもブラウザに任せる）
include $target;

