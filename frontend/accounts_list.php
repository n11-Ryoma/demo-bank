<?php
require_once 'auth_common.php';
require_login();

$pageTitle = '口座一覧';
$res = api_auth_request('GET', '/api/accounts');
$error = null;
$accounts = [];
$accountTypeMap = [
    'ORDINARY' => '普通預金',
    'SAVINGS' => '貯蓄預金',
    'CHECKING' => '当座預金',
];
$statusMap = [
    'ACTIVE' => '有効',
    'INACTIVE' => '無効',
    'SUSPENDED' => '停止',
    'CLOSED' => '解約済み',
];

if (($res['status'] ?? 0) >= 200 && ($res['status'] ?? 0) < 300 && is_array($res['body'])) {
    $accounts = $res['body'];
} else {
    $error = isset($res['exception'])
        ? '通信エラー: ' . $res['exception']
        : api_error_message($res, '口座一覧の取得に失敗しました。');
}

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">口座一覧</h1>
        <a href="dashboard.php" class="btn btn-outline-secondary btn-sm">ダッシュボードへ戻る</a>
    </div>

    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php elseif (!$accounts): ?>
        <div class="alert alert-info">口座がありません。</div>
    <?php else: ?>
        <div class="panel p-0 overflow-hidden">
            <div class="table-responsive">
                <table class="table table-striped mb-0 align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>種別</th>
                        <th>口座番号（マスク）</th>
                        <th class="text-end">残高</th>
                        <th>状態</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php foreach ($accounts as $row): ?>
                        <?php
                        $accountTypeRaw = (string)($row['accountType'] ?? '-');
                        $statusRaw = (string)($row['status'] ?? '-');
                        $accountTypeLabel = $accountTypeMap[$accountTypeRaw] ?? $accountTypeRaw;
                        $statusLabel = $statusMap[$statusRaw] ?? $statusRaw;
                        ?>
                        <tr>
                            <td><?= h($row['accountId'] ?? '-') ?></td>
                            <td><?= h($accountTypeLabel) ?></td>
                            <td><?= h($row['accountNumberMasked'] ?? '-') ?></td>
                            <td class="text-end"><?= isset($row['balance']) ? h(number_format((float)$row['balance'])) . ' 円' : '-' ?></td>
                            <td><?= h($statusLabel) ?></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary" href="account_detail.php?accountId=<?= urlencode((string)($row['accountId'] ?? '')) ?>">詳細</a>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        </div>
    <?php endif; ?>
</main>
<?php require_once 'partials/footer.php'; ?>
