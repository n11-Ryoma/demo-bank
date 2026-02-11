<?php
require_once 'auth_common.php';
require_login();

$pageTitle = '振込先管理';
$flashMessage = null;
$error = null;
$accountTypeToApi = [
    '普通' => 'ORDINARY',
    '貯蓄' => 'SAVINGS',
    '当座' => 'CHECKING',
];
$accountTypeToLabel = [
    'ORDINARY' => '普通',
    'SAVINGS' => '貯蓄',
    'CHECKING' => '当座',
    '普通' => '普通',
    '貯蓄' => '貯蓄',
    '当座' => '当座',
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $selectedAccountTypeLabel = trim($_POST['accountType'] ?? '普通');
    $accountTypeForApi = $accountTypeToApi[$selectedAccountTypeLabel] ?? 'ORDINARY';
    $payload = [
        'bankName' => trim($_POST['bankName'] ?? ''),
        'branchName' => trim($_POST['branchName'] ?? ''),
        'accountType' => $accountTypeForApi,
        'accountNumber' => trim($_POST['accountNumber'] ?? ''),
        'accountHolderName' => trim($_POST['accountHolderName'] ?? ''),
        'nickname' => trim($_POST['nickname'] ?? ''),
    ];

    $createRes = api_auth_request('POST', '/api/beneficiaries', $payload);
    if (($createRes['status'] ?? 0) >= 200 && ($createRes['status'] ?? 0) < 300) {
        $flashMessage = '振込先を登録しました。';
    } else {
        $error = isset($createRes['exception'])
            ? '通信エラー: ' . $createRes['exception']
            : api_error_message($createRes, '振込先の登録に失敗しました。');
    }
}

if (isset($_GET['deleteId']) && ctype_digit($_GET['deleteId'])) {
    $deleteRes = api_auth_request('DELETE', '/api/beneficiaries/' . $_GET['deleteId']);
    if (($deleteRes['status'] ?? 0) >= 200 && ($deleteRes['status'] ?? 0) < 300) {
        $flashMessage = '振込先を削除しました。';
    } else {
        $error = isset($deleteRes['exception'])
            ? '通信エラー: ' . $deleteRes['exception']
            : api_error_message($deleteRes, '振込先の削除に失敗しました。');
    }
}

$listRes = api_auth_request('GET', '/api/beneficiaries');
$items = [];
if (($listRes['status'] ?? 0) >= 200 && ($listRes['status'] ?? 0) < 300 && is_array($listRes['body'])) {
    $items = $listRes['body'];
} elseif ($error === null) {
    $error = isset($listRes['exception'])
        ? '通信エラー: ' . $listRes['exception']
        : api_error_message($listRes, '振込先一覧の取得に失敗しました。');
}

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">振込先管理</h1>
        <a href="dashboard.php" class="btn btn-outline-secondary btn-sm">ダッシュボードへ戻る</a>
    </div>

    <?php if ($flashMessage): ?>
        <div class="alert alert-success"><?= h($flashMessage) ?></div>
    <?php endif; ?>
    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php endif; ?>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="panel p-4">
                <h2 class="h6 fw-bold mb-3">振込先を追加</h2>
                <form method="post">
                    <div class="mb-3">
                        <label class="form-label">銀行名</label>
                        <input name="bankName" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">支店名</label>
                        <input name="branchName" class="form-control">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">口座種別</label>
                        <select name="accountType" class="form-select">
                            <?php $selectedType = $_POST['accountType'] ?? '普通'; ?>
                            <option value="普通" <?= $selectedType === '普通' ? 'selected' : '' ?>>普通</option>
                            <option value="貯蓄" <?= $selectedType === '貯蓄' ? 'selected' : '' ?>>貯蓄</option>
                            <option value="当座" <?= $selectedType === '当座' ? 'selected' : '' ?>>当座</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">口座番号</label>
                        <input name="accountNumber" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">口座名義</label>
                        <input name="accountHolderName" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">ニックネーム</label>
                        <input name="nickname" class="form-control">
                    </div>
                    <button class="btn btn-primary w-100" type="submit">登録</button>
                </form>
            </div>
        </div>
        <div class="col-lg-7">
            <div class="panel p-0 overflow-hidden">
                <div class="table-responsive">
                    <table class="table table-striped mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>銀行・支店</th>
                            <th>口座</th>
                            <th>名義</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <?php if (!$items): ?>
                            <tr><td colspan="5" class="text-center text-muted py-4">振込先がありません。</td></tr>
                        <?php else: ?>
                            <?php foreach ($items as $row): ?>
                                <tr>
                                    <td><?= h($row['id'] ?? '-') ?></td>
                                    <td><?= h(($row['bankName'] ?? '') . ' / ' . ($row['branchName'] ?? '')) ?></td>
                                    <td><?php $rawType = (string)($row['accountType'] ?? ''); $typeLabel = $accountTypeToLabel[$rawType] ?? $rawType; ?><?= h($typeLabel . ' ' . ($row['accountNumberMasked'] ?? '')) ?></td>
                                    <td><?= h($row['accountHolderName'] ?? '-') ?></td>
                                    <td class="text-end">
                                        <a class="btn btn-sm btn-outline-danger" href="beneficiaries.php?deleteId=<?= urlencode((string)($row['id'] ?? '')) ?>" onclick="return confirm('削除しますか？');">削除</a>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
