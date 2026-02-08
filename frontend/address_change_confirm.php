<?php
// address_change_confirm.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$username = $_SESSION['username'] ?? 'guest';

// 住所変更情報がなければフォームへ戻す
$addr = $_SESSION['addr_change'] ?? null;
if ($addr === null) {
    // ざっくりリダイレクト
    header('Location: address_change.php');
    exit;
}

// 表示用に変数へ
$postal    = $addr['postal']    ?? '';
$pref      = $addr['pref']      ?? '';
$city      = $addr['city']      ?? '';
$address1  = $addr['address1']  ?? '';
$address2  = $addr['address2']  ?? '';
$filePath  = $addr['file_path'] ?? ''; // 例: tmp/address/xxxxxxx.png
$fileName  = $addr['file_name'] ?? '';

// preview.php に渡すパラメータ（CTF 的にはここが LFI 起点）
$previewParam = $filePath;
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>住所変更確認 - eBank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-0">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="dashboard.php">
            <img src="images/logo.png" alt="eBank Logo"
                 style="height:100px; margin-right:8px;">
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

<div class="container py-4">
    <h2 class="mb-3">住所変更内容の確認</h2>

    <div class="row g-4">
        <!-- 左：住所の確認 -->
        <div class="col-lg-6">
            <div class="card shadow-sm mb-3">
                <div class="card-header bg-white">
                    新しいご住所
                </div>
                <div class="card-body">
                    <dl class="row mb-0">
                        <dt class="col-sm-3">郵便番号</dt>
                        <dd class="col-sm-9"><?= htmlspecialchars($postal, ENT_QUOTES, 'UTF-8') ?></dd>

                        <dt class="col-sm-3">都道府県</dt>
                        <dd class="col-sm-9"><?= htmlspecialchars($pref, ENT_QUOTES, 'UTF-8') ?></dd>

                        <dt class="col-sm-3">市区町村</dt>
                        <dd class="col-sm-9"><?= htmlspecialchars($city, ENT_QUOTES, 'UTF-8') ?></dd>

                        <dt class="col-sm-3">番地・建物名</dt>
                        <dd class="col-sm-9"><?= htmlspecialchars($address1, ENT_QUOTES, 'UTF-8') ?></dd>

                        <dt class="col-sm-3">部屋番号等</dt>
                        <dd class="col-sm-9">
                            <?= htmlspecialchars($address2 !== '' ? $address2 : '（なし）', ENT_QUOTES, 'UTF-8') ?>
                        </dd>
                    </dl>
                </div>
            </div>

            <form action="address_change_commit.php" method="post" class="mt-3">
                <button type="submit" class="btn btn-primary w-100 mb-2">
                    この内容で住所変更を確定する
                </button>
                <a href="address_change.php" class="btn btn-secondary w-100">
                    修正する（入力画面に戻る）
                </a>
            </form>
        </div>

        <!-- 右：本人確認書類プレビュー -->
        <div class="col-lg-6">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    住所確認書類のプレビュー
                </div>
                <div class="card-body">
                    <p class="small mb-2">
                        アップロードされたファイル：
                        <code><?= htmlspecialchars($fileName, ENT_QUOTES, 'UTF-8') ?></code>
                    </p>

                    <!-- ★ 別タブで開くボタン -->
                    <a href="preview.php?file=<?= urlencode($previewParam) ?>"
                       target="_blank" rel="noopener"
                       class="btn btn-outline-primary mb-3">
                        別タブでプレビューを表示
                    </a>

                    <p class="text-muted small mb-0">
                        プレビューが正しく表示されることを確認のうえ、
                        「住所変更を確定する」を押してください。
                    </p>
                </div>
            </div>
        </div>

    </div>

</div>

</body>
</html>
