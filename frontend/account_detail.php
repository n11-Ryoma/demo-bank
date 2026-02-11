<?php
require_once 'auth_common.php';
require_login();

$accountId = isset($_GET['accountId']) ? (int)$_GET['accountId'] : 0;
if ($accountId <= 0) {
    header('Location: accounts_list.php');
    exit;
}

$pageTitle = '口座詳細';
$res = api_auth_request('GET', '/api/accounts/' . rawurlencode((string)$accountId));
$error = null;
$detail = [];

if (($res['status'] ?? 0) >= 200 && ($res['status'] ?? 0) < 300 && is_array($res['body'])) {
    $detail = $res['body'];
} else {
    $error = isset($res['exception'])
        ? '通信エラー: ' . $res['exception']
        : api_error_message($res, '口座詳細の取得に失敗しました。');
}

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">口座詳細</h1>
        <a href="accounts_list.php" class="btn btn-outline-secondary btn-sm">口座一覧へ戻る</a>
    </div>

    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php else: ?>
        <div class="panel p-4">
            <dl class="row mb-0">
                <dt class="col-sm-3">口座ID</dt><dd class="col-sm-9"><?= h($detail['accountId'] ?? '-') ?></dd>
                <dt class="col-sm-3">支店コード</dt><dd class="col-sm-9"><?= h($detail['branchCode'] ?? '-') ?></dd>
                <dt class="col-sm-3">種別</dt><dd class="col-sm-9"><?= h($detail['accountType'] ?? '-') ?></dd>
                <dt class="col-sm-3">口座番号（マスク）</dt><dd class="col-sm-9"><?= h($detail['accountNumberMasked'] ?? '-') ?></dd>
                <dt class="col-sm-3">ステータス</dt><dd class="col-sm-9"><?= h($detail['status'] ?? '-') ?></dd>
                <dt class="col-sm-3">開設日時</dt><dd class="col-sm-9"><?= h($detail['openedAt'] ?? '-') ?></dd>
                <dt class="col-sm-3">残高</dt><dd class="col-sm-9"><?= isset($detail['balance']) ? h(number_format((float)$detail['balance'])) . ' 円' : '-' ?></dd>
            </dl>
        </div>
    <?php endif; ?>
</main>
<?php require_once 'partials/footer.php'; ?>
