<?php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

function h($value)
{
    return htmlspecialchars((string)$value, ENT_QUOTES, 'UTF-8');
}

$addr = $_SESSION['addr_change'] ?? null;
if ($addr === null) {
    header('Location: address_change.php');
    exit;
}

$postal = $addr['postal'] ?? '';
$pref = $addr['pref'] ?? '';
$city = $addr['city'] ?? '';
$address1 = $addr['address1'] ?? '';
$address2 = $addr['address2'] ?? '';
$filePath = $addr['file_path'] ?? '';
$fileName = $addr['file_name'] ?? '';

$previewParam = $filePath;

$pageTitle = '住所変更確認';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">住所変更の確認</h1>
        <a href="dashboard.php" class="btn btn-outline-secondary btn-sm">ダッシュボードへ戻る</a>
    </div>

    <div class="row g-4">
        <div class="col-lg-6">
            <div class="panel p-4">
                <h2 class="h6 fw-bold mb-3">入力内容</h2>
                <dl class="row mb-0">
                    <dt class="col-sm-4">郵便番号</dt>
                    <dd class="col-sm-8"><?= h($postal) ?></dd>

                    <dt class="col-sm-4">都道府県</dt>
                    <dd class="col-sm-8"><?= h($pref) ?></dd>

                    <dt class="col-sm-4">市区町村</dt>
                    <dd class="col-sm-8"><?= h($city) ?></dd>

                    <dt class="col-sm-4">住所・番地</dt>
                    <dd class="col-sm-8"><?= h($address1) ?></dd>

                    <dt class="col-sm-4">建物名など</dt>
                    <dd class="col-sm-8"><?= h($address2 !== '' ? $address2 : '(なし)') ?></dd>
                </dl>
            </div>

            <form action="address_change_commit.php" method="post" class="mt-3">
                <button type="submit" class="btn btn-primary w-100 mb-2">この内容で申請する</button>
                <a href="address_change.php" class="btn btn-outline-secondary w-100">入力画面へ戻る</a>
            </form>
        </div>

        <div class="col-lg-6">
            <div class="panel p-4">
                <h2 class="h6 fw-bold mb-3">住所確認書類</h2>
                <p class="small mb-2">アップロードファイル: <code><?= h($fileName) ?></code></p>
                <a href="preview.php?file=<?= urlencode($previewParam) ?>" target="_blank" rel="noopener" class="btn btn-outline-primary mb-3">別タブでプレビュー</a>
                <p class="text-muted small mb-0">内容を確認して問題なければ申請してください。</p>
            </div>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
