<?php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

// APIから取得
$limit  = 100;
$offset = 0;

$items = [];
try {
    $url = "/api/accounts/transactions?limit={$limit}&offset={$offset}";
    $res = api_request('GET', $url, null, true);
    if ($res['status'] === 200 && is_array($res['body'])) {
        $items = $res['body'];
    } else {
        die('取引履歴の取得に失敗しました。');
    }
} catch (Exception $e) {
    die('通信エラー: ' . $e->getMessage());
}

// exports ディレクトリに CSV を保存
$baseDir = __DIR__ . '/exports/';
if (!is_dir($baseDir)) {
    mkdir($baseDir, 0777, true);
}

$filename = 'transactions_' . ($_SESSION['username'] ?? 'user') . '_' . date('Ymd_His') . '.csv';
$path = $baseDir . $filename;

$fp = fopen($path, 'w');

// ヘッダ
fputcsv($fp, ['日時', '口座番号', '種別', '金額', '残高（取引後）', '相手口座', 'メモ']);

foreach ($items as $row) {
    fputcsv($fp, [
        $row['createdAt']            ?? '',
        $row['accountNumber']        ?? '',
        $row['type']                 ?? '',
        $row['amount']               ?? '',
        $row['balanceAfter']         ?? '',
        $row['relatedAccountNumber'] ?? '',
        $row['description']          ?? '',
    ]);
}

fclose($fp);

// ここで2つ目の処理へ：download.php にリダイレクト
header('Location: download.php?file=' . urlencode($filename));
exit;
