<?php
$pageTitle = '資産運用のご案内';
$extraStyles = '<style>
body { background: #f4f7fb; }
        .topbar { background: #12324a; }
        .card-panel { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>';
require_once 'partials/header.php';
?>
<main class="container py-5">
    <h1 class="h3 fw-bold mb-4">資産運用のご案内</h1>
    <div class="row g-3">
        <div class="col-md-4"><section class="card-panel p-4 h-100"><h2 class="h5">投資信託</h2><p class="text-muted mb-0">厳選ファンドを少額から購入できます。</p></section></div>
        <div class="col-md-4"><section class="card-panel p-4 h-100"><h2 class="h5">つみたて</h2><p class="text-muted mb-0">毎月自動で積立設定。長期運用をサポートします。</p></section></div>
        <div class="col-md-4"><section class="card-panel p-4 h-100"><h2 class="h5">NISA</h2><p class="text-muted mb-0">非課税制度を活用した運用の基本をご確認ください。</p></section></div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
