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

$category = isset($_GET['category']) ? trim($_GET['category']) : '';
$q = isset($_GET['q']) ? trim($_GET['q']) : '';
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;
$size = isset($_GET['size']) ? max(1, min(50, (int)$_GET['size'])) : 10;

$query = http_build_query(array_filter([
    'category' => $category !== '' ? $category : null,
    'q' => $q !== '' ? $q : null,
    'page' => $page,
    'size' => $size,
]));

$data = api_get('/api/news?' . $query);
$items = isset($data['items']) && is_array($data['items']) ? $data['items'] : [];
$hasError = $data === null;

$pageTitle = 'お知らせ';
$extraStyles = <<<CSS
<style>
    .news-item { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
    .date { color: #2c89c7; font-weight: 700; font-size: .9rem; }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-3">
        <div>
            <h1 class="h3 fw-bold mb-1">お知らせ</h1>
            <p class="text-muted mb-0">最新情報とメンテナンス・障害情報を掲載しています。</p>
        </div>
        <a href="index.php" class="btn btn-outline-secondary btn-sm">トップへ戻る</a>
    </div>

    <form class="row g-2 mb-4" method="get" action="news.php">
        <div class="col-md-4">
            <input type="text" class="form-control" name="q" placeholder="キーワード" value="<?= h($q) ?>">
        </div>
        <div class="col-md-3">
            <select class="form-select" name="category">
                <option value="">すべてのカテゴリ</option>
                <option value="maintenance" <?= $category === 'maintenance' ? 'selected' : '' ?>>メンテナンス</option>
                <option value="announcement" <?= $category === 'announcement' ? 'selected' : '' ?>>お知らせ</option>
                <option value="outage" <?= $category === 'outage' ? 'selected' : '' ?>>障害</option>
            </select>
        </div>
        <div class="col-md-2">
            <select class="form-select" name="size">
                <?php foreach ([5,10,20,30,50] as $opt): ?>
                    <option value="<?= $opt ?>" <?= $size === $opt ? 'selected' : '' ?>><?= $opt ?>件</option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-md-3">
            <button class="btn btn-primary w-100" type="submit">検索</button>
        </div>
    </form>

    <?php if ($hasError): ?>
        <div class="alert alert-warning">データ取得に失敗しました。時間をおいて再度お試しください。</div>
    <?php endif; ?>

    <div class="d-grid gap-3">
        <?php if (count($items) === 0): ?>
            <div class="text-muted">該当するお知らせはありません。</div>
        <?php else: ?>
            <?php foreach ($items as $item): ?>
                <article class="news-item p-4">
                    <p class="date mb-2"><?= h(substr((string)($item['publishedAt'] ?? ''), 0, 10)) ?></p>
                    <h2 class="h5"><?= h($item['title'] ?? '') ?></h2>
                    <p class="text-muted mb-0"><?= h($item['summary'] ?? '') ?></p>
                </article>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
