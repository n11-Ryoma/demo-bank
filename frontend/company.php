<?php
$pageTitle = '会社情報';
$extraStyles = '<style>
body { background: #f4f7fb; }
.topbar { background: #12324a; }
.table-wrap { background: #fff; border-radius: 14px; padding: 1rem; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <h1 class="h3 fw-bold mb-4">会社情報</h1>
    <section class="table-wrap mb-4">
        <table class="table mb-0">
            <tbody>
                <tr><th style="width:30%">社名</th><td>株式会社 +Acts Bank</td></tr>
                <tr><th>設立</th><td>2015年4月</td></tr>
                <tr><th>本社所在地</th><td>東京都千代田区丸の内1-2-3</td></tr>
                <tr><th>事業内容</th><td>インターネット銀行サービス、決済関連事業</td></tr>
            </tbody>
        </table>
    </section>
    <section class="table-wrap">
        <h2 class="h5 mb-3">経営方針</h2>
        <p class="mb-0 text-muted">「だれでも安心して使える金融インフラ」を追求し、透明性の高いサービス提供を目指しています。</p>
    </section>
</main>
<?php require_once 'partials/footer.php'; ?>
