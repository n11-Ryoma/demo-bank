<?php
require_once 'config.php';

function h($value)
{
    return htmlspecialchars((string)$value, ENT_QUOTES, 'UTF-8');
}

function api_get($path)
{
    try {
        $res = api_request('GET', $path);
        if ($res['status'] >= 200 && $res['status'] < 300 && is_array($res['body'])) {
            return $res['body'];
        }
    } catch (Exception $e) {
        return null;
    }
    return null;
}

$q = isset($_GET['q']) ? trim($_GET['q']) : '';
$category = isset($_GET['category']) ? trim($_GET['category']) : '';
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;
$size = isset($_GET['size']) ? max(1, min(50, (int)$_GET['size'])) : 10;

$query = http_build_query(array_filter([
    'q' => $q !== '' ? $q : null,
    'category' => $category !== '' ? $category : null,
    'page' => $page,
    'size' => $size,
]));

$data = api_get('/api/faq?' . $query);
$items = isset($data['items']) && is_array($data['items']) ? $data['items'] : [];
$hasError = $data === null;

$pageTitle = 'よくある質問';
$extraStyles = <<<CSS
<style>
    .faq-wrap { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-3">
        <div>
            <h1 class="h3 fw-bold mb-1">よくある質問</h1>
            <p class="text-muted mb-0">お問い合わせ前にご確認ください。</p>
        </div>
        <a href="index.php" class="btn btn-outline-secondary btn-sm">トップへ戻る</a>
    </div>

    <form class="row g-2 mb-4" method="get" action="faq.php">
        <div class="col-md-5">
            <input type="text" class="form-control" name="q" placeholder="キーワード" value="<?= h($q) ?>">
        </div>
        <div class="col-md-4">
            <select class="form-select" name="category">
                <option value="">すべてのカテゴリ</option>
                <option value="account" <?= $category === 'account' ? 'selected' : '' ?>>口座</option>
                <option value="transfer" <?= $category === 'transfer' ? 'selected' : '' ?>>振込</option>
                <option value="security" <?= $category === 'security' ? 'selected' : '' ?>>セキュリティ</option>
                <option value="loan" <?= $category === 'loan' ? 'selected' : '' ?>>ローン</option>
            </select>
        </div>
        <div class="col-md-3">
            <button class="btn btn-primary w-100" type="submit">検索</button>
        </div>
    </form>

    <?php if ($hasError): ?>
        <div class="alert alert-warning">データ取得に失敗しました。時間をおいて再度お試しください。</div>
    <?php endif; ?>

    <div class="faq-wrap p-3 p-md-4">
        <?php if (count($items) === 0): ?>
            <div class="text-muted">該当するFAQはありません。</div>
        <?php else: ?>
            <?php foreach ($items as $item): ?>
                <div class="mb-3">
                    <h2 class="h6 fw-bold">Q. <?= h($item['question'] ?? '') ?></h2>
                    <p class="mb-0 text-muted">A. <?= h($item['answer'] ?? '') ?></p>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
