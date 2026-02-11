<?php
require_once 'auth_common.php';
require_login();

$pageTitle = 'プロフィール概要';
$meRes = api_auth_request('GET', '/api/me');
$error = null;
$me = [];

if (($meRes['status'] ?? 0) >= 200 && ($meRes['status'] ?? 0) < 300 && is_array($meRes['body'])) {
    $me = $meRes['body'];
} else {
    $error = isset($meRes['exception'])
        ? '通信エラー: ' . $meRes['exception']
        : api_error_message($meRes, 'プロフィール情報の取得に失敗しました。');
}

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">プロフィール概要</h1>
        <a href="dashboard.php" class="btn btn-outline-secondary btn-sm">ダッシュボードへ戻る</a>
    </div>

    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php else: ?>
        <div class="panel p-4">
            <dl class="row mb-0">
                <dt class="col-sm-3">ユーザーID</dt><dd class="col-sm-9"><?= h($me['userId'] ?? '-') ?></dd>
                <dt class="col-sm-3">ユーザー名</dt><dd class="col-sm-9"><?= h($me['username'] ?? '-') ?></dd>
                <dt class="col-sm-3">メール</dt><dd class="col-sm-9"><?= h($me['email'] ?? '-') ?></dd>
                <dt class="col-sm-3">氏名（漢字）</dt><dd class="col-sm-9"><?= h($me['nameKanji'] ?? '-') ?></dd>
                <dt class="col-sm-3">氏名（カナ）</dt><dd class="col-sm-9"><?= h($me['nameKana'] ?? '-') ?></dd>
                <dt class="col-sm-3">電話番号</dt><dd class="col-sm-9"><?= h($me['phone'] ?? '-') ?></dd>
                <dt class="col-sm-3">郵便番号</dt><dd class="col-sm-9"><?= h($me['postalCode'] ?? '-') ?></dd>
                <dt class="col-sm-3">住所</dt><dd class="col-sm-9"><?= h($me['address'] ?? '-') ?></dd>
            </dl>
        </div>
    <?php endif; ?>
</main>
<?php require_once 'partials/footer.php'; ?>
