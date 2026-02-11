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

$tag = isset($_GET['tag']) ? trim($_GET['tag']) : '';
$limit = isset($_GET['limit']) ? max(1, min(50, (int)$_GET['limit'])) : 10;

$query = http_build_query(array_filter([
    'tag' => $tag !== '' ? $tag : null,
    'limit' => $limit,
]));

$data = api_get('/api/security-alerts?' . $query);
$items = isset($data['items']) && is_array($data['items']) ? $data['items'] : [];
$hasError = $data === null;

$pageTitle = 'セキュリティ';
$extraStyles = <<<CSS
<style>
    .panel { background: #fff; border-radius: 14px; padding: 1.2rem; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-3">
        <div>
            <h1 class="h3 fw-bold mb-1">セキュリティへの取り組み</h1>
            <p class="text-muted mb-0">安全にご利用いただくための対策と注意点です。</p>
        </div>
        <a href="index.php" class="btn btn-outline-secondary btn-sm">トップへ戻る</a>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-md-6">
            <section class="panel h-100">
                <h2 class="h5">多要素認証</h2>
                <p class="text-muted mb-0">ログイン時に追加認証を実施し、第三者の不正利用リスクを低減します。</p>
            </section>
        </div>
        <div class="col-md-6">
            <section class="panel h-100">
                <h2 class="h5">不正検知モニタリング</h2>
                <p class="text-muted mb-0">通常と異なるアクセスや取引を検知し、必要に応じて利用を一時制限します。</p>
            </section>
        </div>
        <div class="col-md-6">
            <section class="panel h-100">
                <h2 class="h5">通信の暗号化</h2>
                <p class="text-muted mb-0">サイトとの通信はTLSで暗号化し、個人情報を保護します。</p>
            </section>
        </div>
        <div class="col-md-6">
            <section class="panel h-100">
                <h2 class="h5">お客様へのお願い</h2>
                <p class="text-muted mb-0">パスワードの使い回しを避け、公共Wi-Fi利用時は特にご注意ください。</p>
            </section>
        </div>
    </div>

    <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
        <h2 class="h5 mb-0">最近の注意喚起</h2>
        <form class="d-flex gap-2" method="get" action="security.php">
            <select class="form-select form-select-sm" name="tag">
                <option value="">すべて</option>
                <option value="phishing" <?= $tag === 'phishing' ? 'selected' : '' ?>>フィッシング</option>
                <option value="voice" <?= $tag === 'voice' ? 'selected' : '' ?>>電話</option>
                <option value="account" <?= $tag === 'account' ? 'selected' : '' ?>>アカウント</option>
                <option value="payment" <?= $tag === 'payment' ? 'selected' : '' ?>>決済</option>
            </select>
            <button class="btn btn-sm btn-outline-primary" type="submit">絞り込み</button>
        </form>
    </div>

    <?php if ($hasError): ?>
        <div class="alert alert-warning">データ取得に失敗しました。時間をおいて再度お試しください。</div>
    <?php endif; ?>

    <div class="row g-3">
        <?php if (count($items) === 0): ?>
            <div class="col-12 text-muted">現在の注意喚起はありません。</div>
        <?php else: ?>
            <?php foreach ($items as $item): ?>
                <div class="col-md-6 col-lg-3">
                    <section class="panel h-100">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="badge text-bg-danger"><?= h($item['riskLevel'] ?? '') ?></span>
                            <span class="text-muted small">件数 <?= h($item['recentCount'] ?? '') ?></span>
                        </div>
                        <h3 class="h6 fw-bold"><?= h($item['title'] ?? '') ?></h3>
                        <p class="text-muted mb-0 small"><?= h($item['tip'] ?? '') ?></p>
                    </section>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
