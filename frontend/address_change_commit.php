<?php
// address_change_commit.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

// POST以外は無効
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: address_change.php');
    exit;
}

// 住所変更データがない
$addr = $_SESSION['addr_change'] ?? null;
if ($addr === null) {
    $status  = 'error';
    $message = '住所変更情報が見つかりません。最初からやり直してください。';
} else {
    // 値取り出し
    $postal    = $addr['postal']    ?? '';
    $pref      = $addr['pref']      ?? '';
    $city      = $addr['city']      ?? '';
    $address1  = $addr['address1']  ?? '';
    $address2  = $addr['address2']  ?? '';
    $filePath  = $addr['file_path'] ?? '';  // 例: tmp/address/XXXX.ext

    // 一時ファイルの実パス（address_change.php で保存した uploads/ 配下を想定）
    $tempFullPath = __DIR__ . '/uploads/' . $filePath;

    if ($filePath === '' || !is_readable($tempFullPath)) {
        $status  = 'error';
        $message = 'アップロードされた住所確認書類が見つかりません。もう一度アップロードし直してください。';
    } else {
        // ファイルを読み込んで base64 化
        $fileData   = file_get_contents($tempFullPath);
        $fileBase64 = base64_encode($fileData);

        // Java 側 DTO (AddressChangeCommitRequest) に合わせたキーでボディを作成
        $body = [
            'postalCode'   => $postal,
            'prefecture'   => $pref,
            'city'         => $city,
            'addressLine1' => $address1,
            'addressLine2' => $address2,
            'fileName'     => basename($tempFullPath), // とりあえずファイル名は basename でOK
            'fileBase64'   => $fileBase64,
        ];

        try {
            $res = api_request('POST', '/api/address-change/commit', $body, true);

            if ($res['status'] === 200) {
                // 一時データ削除
                unset($_SESSION['addr_change']);

                // PHP 側の一時ファイルも消したいならここで
                // @unlink($tempFullPath);

                $status  = 'success';
                $message = '住所変更の申請を受け付けました。審査結果をお待ちください。';
            } else {
                $status  = 'error';
                if ((int)$res['status'] === 401) {
                    $message = 'ログインの有効期限が切れたか、認証に失敗しました。再度ログインしてやり直してください。（コード: 401）';
                } else {
                    $message = '住所変更の確定に失敗しました。（コード: ' . (int)$res['status'] . '）';
                }
            }

        } catch (Exception $e) {
            $status  = 'error';
            $message = '通信エラー: ' . $e->getMessage();
        }
    }
}

$username = $_SESSION['username'] ?? 'guest';
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>住所変更 完了 - eBank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>

<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-4">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="dashboard.php">
            <img src="images/logo.png" alt="eBank Logo" style="height:90px; margin-right:8px;">
            <span class="fw-bold">+Acts Bank</span>
        </a>
        <div class="d-flex">
            <span class="navbar-text me-3 text-white">
                <?= htmlspecialchars($username, ENT_QUOTES, 'UTF-8') ?> さん
            </span>
            <a href="logout.php" class="btn btn-light btn-sm">ログアウト</a>
        </div>
    </div>
</nav>

<div class="container">
    <div class="col-md-8 mx-auto">

        <?php if ($status === 'success'): ?>
            <div class="alert alert-success">
                <?= htmlspecialchars($message, ENT_QUOTES, 'UTF-8') ?>
            </div>
        <?php else: ?>
            <div class="alert alert-danger">
                <?= htmlspecialchars($message, ENT_QUOTES, 'UTF-8') ?>
            </div>
        <?php endif; ?>

        <div class="text-center mt-4">
            <a href="dashboard.php" class="btn btn-primary btn-lg">ダッシュボードへ戻る</a>
        </div>

    </div>
</div>

</body>
</html>
