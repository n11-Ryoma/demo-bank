<?php
$pageTitle = 'お問い合わせ';
$extraStyles = '<style>
body { background: #f4f7fb; }
.topbar { background: #12324a; }
.panel { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <h1 class="h3 fw-bold mb-4">お問い合わせ</h1>
    <div class="row g-3">
        <div class="col-md-6">
            <section class="panel p-4 h-100">
                <h2 class="h5">カスタマーサポート</h2>
                <p class="mb-1">0120-111-222</p>
                <p class="text-muted mb-0">受付時間: 平日 9:00〜18:00</p>
            </section>
        </div>
        <div class="col-md-6">
            <section class="panel p-4 h-100">
                <h2 class="h5">紛失・不正利用のご相談</h2>
                <p class="mb-1">0120-333-999</p>
                <p class="text-muted mb-0">24時間365日受付</p>
            </section>
        </div>
        <div class="col-12">
            <section class="panel p-4">
                <h2 class="h5">メールでのお問い合わせ</h2>
                <p class="text-muted mb-0">お問い合わせフォームから受け付けています。通常2営業日以内にご返信します。</p>
            </section>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
