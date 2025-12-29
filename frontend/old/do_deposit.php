<?php
// do_deposit.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$amount = (int)($_POST['amount'] ?? 0);
$description = $_POST['description'] ?? '';

if ($amount <= 0) {
    $_SESSION['flash_error'] = '金額は1以上を指定してください。';
    header('Location: dashboard.php');
    exit;
}

try {
    $res = api_request('POST', '/api/accounts/deposit', [
        'amount' => $amount,
        'description' => $description,
    ], true);

    if ($res['status'] === 200) {
        $_SESSION['flash_message'] = '入金が完了しました。';
    } else {
        $_SESSION['flash_error'] = '入金に失敗しました。';
    }
} catch (Exception $e) {
    $_SESSION['flash_error'] = '通信エラー: ' . $e->getMessage();
}

header('Location: dashboard.php');
exit;
