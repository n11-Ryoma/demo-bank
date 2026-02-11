<?php
require_once 'auth_common.php';
require_login();

$pageTitle = 'セキュリティセンター';
$error = null;
$flashMessage = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    if ($action === 'logoutCurrent') {
        $logoutRes = api_auth_request('POST', '/api/security/logout');
        if (($logoutRes['status'] ?? 0) >= 200 && ($logoutRes['status'] ?? 0) < 300) {
            session_destroy();
            header('Location: login.php');
            exit;
        }
        $error = isset($logoutRes['exception'])
            ? '通信エラー: ' . $logoutRes['exception']
            : api_error_message($logoutRes, 'ログアウトに失敗しました。');
    }

    if ($action === 'deleteSession') {
        $sessionId = trim($_POST['sessionId'] ?? '');
        if ($sessionId !== '') {
            $deleteRes = api_auth_request('DELETE', '/api/security/sessions/' . rawurlencode($sessionId));
            if (($deleteRes['status'] ?? 0) >= 200 && ($deleteRes['status'] ?? 0) < 300) {
                $flashMessage = 'セッションを削除しました。';
            } else {
                $error = isset($deleteRes['exception'])
                    ? '通信エラー: ' . $deleteRes['exception']
                    : api_error_message($deleteRes, 'セッション削除に失敗しました。');
            }
        }
    }
}

$history = [];
$historyRes = api_auth_request('GET', '/api/security/login-history?limit=20');
if (($historyRes['status'] ?? 0) >= 200 && ($historyRes['status'] ?? 0) < 300 && is_array($historyRes['body'])) {
    $history = $historyRes['body'];
} elseif ($error === null) {
    $error = isset($historyRes['exception'])
        ? '通信エラー: ' . $historyRes['exception']
        : api_error_message($historyRes, 'ログイン履歴の取得に失敗しました。');
}

$sessions = [];
$sessionsRes = api_auth_request('GET', '/api/security/sessions');
if (($sessionsRes['status'] ?? 0) >= 200 && ($sessionsRes['status'] ?? 0) < 300 && is_array($sessionsRes['body'])) {
    $sessions = $sessionsRes['body'];
} elseif ($error === null) {
    $error = isset($sessionsRes['exception'])
        ? '通信エラー: ' . $sessionsRes['exception']
        : api_error_message($sessionsRes, 'セッション一覧の取得に失敗しました。');
}

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">セキュリティセンター</h1>
        <a href="dashboard.php" class="btn btn-outline-secondary btn-sm">ダッシュボードへ戻る</a>
    </div>

    <?php if ($flashMessage): ?>
        <div class="alert alert-success"><?= h($flashMessage) ?></div>
    <?php endif; ?>
    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php endif; ?>

    <div class="row g-4">
        <div class="col-lg-7">
            <div class="panel p-0 overflow-hidden">
                <div class="p-3 border-bottom bg-light-subtle">
                    <h2 class="h6 fw-bold mb-0">ログイン履歴</h2>
                </div>
                <div class="table-responsive">
                    <table class="table table-striped mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>日時</th>
                            <th>IP</th>
                            <th>User-Agent</th>
                            <th>結果</th>
                        </tr>
                        </thead>
                        <tbody>
                        <?php if (!$history): ?>
                            <tr><td colspan="4" class="text-center text-muted py-4">履歴がありません。</td></tr>
                        <?php else: ?>
                            <?php foreach ($history as $row): ?>
                                <tr>
                                    <td><?= h($row['occurredAt'] ?? '-') ?></td>
                                    <td><?= h($row['ip'] ?? '-') ?></td>
                                    <td class="small"><?= h($row['userAgent'] ?? '-') ?></td>
                                    <td>
                                        <span class="badge <?= ($row['result'] ?? '') === 'SUCCESS' ? 'text-bg-success' : 'text-bg-danger' ?>">
                                            <?= h($row['result'] ?? '-') ?>
                                        </span>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="panel p-0 overflow-hidden mb-3">
                <div class="p-3 border-bottom bg-light-subtle">
                    <h2 class="h6 fw-bold mb-0">有効セッション</h2>
                </div>
                <div class="list-group list-group-flush">
                    <?php if (!$sessions): ?>
                        <div class="list-group-item text-muted">セッションがありません。</div>
                    <?php else: ?>
                        <?php foreach ($sessions as $row): ?>
                            <div class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong class="small"><?= h($row['sessionId'] ?? '-') ?></strong>
                                    <?php if (!empty($row['current'])): ?>
                                        <span class="badge text-bg-primary">現在の端末</span>
                                    <?php endif; ?>
                                </div>
                                <div class="small text-muted mb-2">
                                    <?= h($row['ip'] ?? '-') ?> / <?= h($row['loginAt'] ?? '-') ?>
                                </div>
                                <?php if (empty($row['current'])): ?>
                                    <form method="post" class="d-inline">
                                        <input type="hidden" name="action" value="deleteSession">
                                        <input type="hidden" name="sessionId" value="<?= h($row['sessionId'] ?? '') ?>">
                                        <button class="btn btn-sm btn-outline-danger" type="submit">この端末をログアウト</button>
                                    </form>
                                <?php endif; ?>
                            </div>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </div>
            </div>

            <div class="panel p-4">
                <h2 class="h6 fw-bold">現在の端末からログアウト</h2>
                <p class="text-muted small">`POST /api/security/logout` を実行します。</p>
                <form method="post">
                    <input type="hidden" name="action" value="logoutCurrent">
                    <button class="btn btn-danger" type="submit">ログアウト</button>
                </form>
            </div>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
