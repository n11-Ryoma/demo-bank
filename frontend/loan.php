<?php
$pageTitle = 'ローン商品のご案内';
$extraStyles = '<style>
body { background: #f4f7fb; }
.topbar { background: #12324a; }
.plan-card { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <h1 class="h3 fw-bold mb-3">ローン商品のご案内</h1>
    <p class="text-muted mb-4">ライフプランに合わせて選べるローン商品をご用意しています。</p>
    <div class="row g-3">
        <div class="col-md-6">
            <section class="plan-card p-4 h-100">
                <h2 class="h5">住宅ローン</h2>
                <p class="text-muted mb-2">長期返済に対応した、安心設計の住宅ローンです。</p>
                <p class="mb-0">年利: 0.79%〜（審査結果により異なります）</p>
            </section>
        </div>
        <div class="col-md-6">
            <section class="plan-card p-4 h-100">
                <h2 class="h5">カードローン</h2>
                <p class="text-muted mb-2">急な出費に対応できる、限度額設定型のローンです。</p>
                <p class="mb-0">年利: 2.8%〜14.0%</p>
            </section>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
