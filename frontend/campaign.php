<?php
$pageTitle = 'キャンペーン';
$extraStyles = '<style>
body { background: #f4f7fb; }
        .topbar { background: #12324a; }
        .panel { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
        .badge-new { background: #2c89c7; }
</style>';
require_once 'partials/header.php';
?>
<main class="container py-5">
    <h1 class="h3 fw-bold mb-4">キャンペーン</h1>
    <div class="row g-3">
        <div class="col-md-6">
            <section class="panel p-4 h-100">
                <span class="badge badge-new mb-2">NEW</span>
                <h2 class="h5">はじめての口座開設プログラム</h2>
                <p class="text-muted mb-0">条件達成で最大3,000円相当の特典を進呈します。</p>
            </section>
        </div>
        <div class="col-md-6">
            <section class="panel p-4 h-100">
                <h2 class="h5">お友だち紹介キャンペーン</h2>
                <p class="text-muted mb-0">紹介した方・された方それぞれに特典があります。</p>
            </section>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
