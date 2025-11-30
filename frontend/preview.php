<?php
// preview.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

// デフォルトは「明細プレビュー用テンプレート」
$tpl = $_GET['tpl'] ?? 'statement.php';

// 本来ならホワイトリスト & ../ 禁止にすべきだが、CTF/ラボ用に緩くする
$baseDir = __DIR__ . '/templates/';
$path = $baseDir . $tpl;

if (!is_file($path)) {
    http_response_code(404);
    echo 'テンプレートが見つかりません: '
        . htmlspecialchars($tpl, ENT_QUOTES, 'UTF-8');
    exit;
}

// ★ LFIの起点：ユーザー指定ファイルをそのまま include
//   ※ 本番では絶対にやっちゃダメ。ラボ・CTF専用。
include $path;





