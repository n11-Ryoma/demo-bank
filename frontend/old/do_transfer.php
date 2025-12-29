<?php
// do_transfer.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$toAccountNumber = $_POST['toAccountNumber'] ?? '';
$amount = (int)($_POST['amount'] ?? 0);
$description = $_POST['description'] ?? '';

if ($amount <= 0 || $toAccountNumber === '') {
    $_SESSION['flash_error'] = '振込先と金額を正しく入力してください。';
    header('Location: dashboard.php');
    exit;
}

try {
    $res = api_request('POST', '/api/accounts/transfer', [
        'toAccountNumber' => $toAccountNumber,
        'amount' => $amount,
        'description' => $description,
    ], true);

    if ($res['status'] === 200) {
        $_SESSION['flash_message'] = '振込が完了しました。';
    } else {
        $_SESSION['flash_error'] = '振込に失敗しました。残高不足または口座番号誤りの可能性があります。';
    }
} catch (Exception $e) {
    $_SESSION['flash_error'] = '通信エラー: ' . $e->getMessage();
}

header('Location: dashboard.php');
exit;
