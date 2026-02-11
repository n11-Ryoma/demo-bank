<?php
$pageTitle = 'サービス一覧';
$extraStyles = '<style>
body { background: #f4f7fb; }
        .topbar { background: #12324a; }
        .card { border: 0; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>';
require_once 'partials/header.php';
?>
<main class="container py-5">
    <h1 class="h3 fw-bold mb-3">サービス一覧</h1>
    <p class="text-muted mb-4">+Acts Bankで利用できる主なサービスをご紹介します。</p>
    <div class="row g-3">
        <div class="col-md-6">
            <article class="card h-100"><div class="card-body"><h2 class="h5">普通預金口座</h2><p class="mb-0 text-muted">入出金・振込・口座振替に対応。スマホから残高確認も可能です。</p></div></article>
        </div>
        <div class="col-md-6">
            <article class="card h-100"><div class="card-body"><h2 class="h5">振込・送金</h2><p class="mb-0 text-muted">宛先登録や予約振込に対応。24時間いつでも操作できます。</p></div></article>
        </div>
        <div class="col-md-6">
            <article class="card h-100"><div class="card-body"><h2 class="h5">デビットカード</h2><p class="mb-0 text-muted">買い物と同時に口座から引き落とし。利用通知で安心管理。</p></div></article>
        </div>
        <div class="col-md-6">
            <article class="card h-100"><div class="card-body"><h2 class="h5">各種手続き</h2><p class="mb-0 text-muted">住所変更・利用制限設定・明細ダウンロードなどをオンラインで。</p></div></article>
        </div>
    </div>
    <div class="mt-4 d-flex gap-2">
        <a href="rates.php" class="btn btn-outline-primary">金利・手数料</a>
        <a href="security.php" class="btn btn-outline-primary">セキュリティ</a>
        <a href="faq.php" class="btn btn-outline-primary">FAQ</a>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
