<?php
$pageTitle = '規約・方針';
$extraStyles = '<style>
body { background: #f4f7fb; }
        .topbar { background: #12324a; }
        .list-panel { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
        .rule-link { text-decoration: none; color: #12324a; display: block; padding: .65rem .75rem; border-radius: .5rem; }
        .rule-link:hover { background: #eef5fb; }
</style>';
require_once 'partials/header.php';
?>
<main class="container py-5">
    <h1 class="h3 fw-bold mb-4">規約・方針</h1>
    <section class="list-panel p-3 p-md-4">
        <ul class="list-unstyled mb-0">
            <li><a class="rule-link" href="terms-deposit.php">普通預金規定</a></li>
            <li><a class="rule-link" href="terms-online.php">オンラインバンキング利用規約</a></li>
            <li><a class="rule-link" href="privacy-policy.php">個人情報保護方針</a></li>
            <li><a class="rule-link" href="disclosure.php">金融商品に関する重要事項説明</a></li>
        </ul>
    </section>
</main>
<?php require_once 'partials/footer.php'; ?>
