<?php
require_once 'auth_common.php';
require_login();

$pageTitle = 'ダッシュボード';
$extraStyles = <<<'CSS'
<style>
    .quick-card {
        transition: transform .2s ease, box-shadow .2s ease;
        border: 0;
    }

    .quick-card:hover {
        transform: translateY(-3px);
        box-shadow: 0 12px 24px rgba(18, 50, 74, .12);
    }
</style>
CSS;

$balanceRes = api_auth_request('GET', '/api/accounts/balance');
$balance = null;
$accountNumber = null;
$error = null;

if (($balanceRes['status'] ?? 0) >= 200 && ($balanceRes['status'] ?? 0) < 300 && is_array($balanceRes['body'])) {
    $balance = $balanceRes['body']['balance'] ?? null;
    $accountNumber = $balanceRes['body']['accountNumber'] ?? null;
} else {
    $error = isset($balanceRes['exception'])
        ? '通信エラー: ' . $balanceRes['exception']
        : api_error_message($balanceRes, '残高の取得に失敗しました。');
}

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="panel p-4 p-md-5 mb-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
            <div>
                <p class="text-muted mb-1">ログインユーザー</p>
                <h1 class="h4 fw-bold mb-2"><?= h($_SESSION['username'] ?? '') ?> さん</h1>
                <p class="text-muted mb-0">口座番号: <?= h($accountNumber ?: '-') ?></p>
            </div>
            <div class="text-md-end">
                <p class="text-muted mb-1">現在残高</p>
                <p class="fs-3 fw-bold text-primary mb-0">
                    <?= $balance !== null ? h(number_format((float)$balance)) . ' 円' : '-' ?>
                </p>
            </div>
        </div>
        <?php if ($error): ?>
            <div class="alert alert-warning mt-3 mb-0"><?= h($error) ?></div>
        <?php endif; ?>
    </div>

    <section class="mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 fw-bold mb-0">ログイン後メニュー</h2>
            <a href="logout.php" class="btn btn-outline-secondary btn-sm">ログアウト</a>
        </div>
        <div class="row g-3">
            <div class="col-md-6 col-lg-4">
                <a href="profile_me.php" class="text-decoration-none text-dark">
                    <article class="card quick-card h-100 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">プロフィール概要</h3>
                            <p class="text-muted small mb-0">お客さま情報と連絡先の最新内容を確認できます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-4">
                <a href="accounts_list.php" class="text-decoration-none text-dark">
                    <article class="card quick-card h-100 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">口座一覧・詳細</h3>
                            <p class="text-muted small mb-0">保有口座の一覧、残高、口座詳細を確認できます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-4">
                <a href="beneficiaries.php" class="text-decoration-none text-dark">
                    <article class="card quick-card h-100 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">振込先管理</h3>
                            <p class="text-muted small mb-0">よく使う振込先の登録、確認、削除ができます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-4">
                <a href="security_center.php" class="text-decoration-none text-dark">
                    <article class="card quick-card h-100 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">セキュリティ</h3>
                            <p class="text-muted small mb-0">ログイン履歴の確認とセッション管理ができます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-4">
                <a href="transactions.php" class="text-decoration-none text-dark">
                    <article class="card quick-card h-100 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">取引明細</h3>
                            <p class="text-muted small mb-0">入出金・振込を含む取引履歴を確認できます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-4">
                <a href="address_change.php" class="text-decoration-none text-dark">
                    <article class="card quick-card h-100 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">住所変更</h3>
                            <p class="text-muted small mb-0">住所変更の申請と受付状況の確認ができます。</p>
                        </div>
                    </article>
                </a>
            </div>
        </div>
    </section>
</main>
<?php require_once 'partials/footer.php'; ?>
