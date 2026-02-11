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
$service = isset($_GET['service']) ? trim($_GET['service']) : 'transfer';

$rateQuery = $category !== '' ? ('?category=' . urlencode($category)) : '';
$rates = api_get('/api/rates' . $rateQuery);
$serviceAlias = [
    'transfer' => '振込',
    'atm' => 'ATM',
    'card' => 'カード',
    'account' => '口座',
];
$fees = api_get('/api/fees?service=' . urlencode($service));
if ((!is_array($fees) || !isset($fees['items']) || !is_array($fees['items']) || count($fees['items']) === 0) && isset($serviceAlias[$service])) {
    $fees = api_get('/api/fees?service=' . rawurlencode($serviceAlias[$service]));
}
if (!is_array($fees) || !isset($fees['items']) || !is_array($fees['items']) || count($fees['items']) === 0) {
    $fees = api_get('/api/fees');
}

$rateItems = isset($rates['items']) && is_array($rates['items']) ? $rates['items'] : [];
$feeItems = isset($fees['items']) && is_array($fees['items']) ? $fees['items'] : [];

if (is_array($feeItems) && count($feeItems) > 0) {
    $serviceMap = [
        'transfer' => '振込',
        'atm' => 'ATM',
        'card' => 'カード',
        'account' => '口座',
    ];
    $channelMap = [
        'online' => 'オンライン',
        'branch' => '窓口',
        'other-bank' => '他行',
        'app' => 'アプリ',
        'reissue' => '再発行',
    ];
    foreach ($feeItems as &$item) {
        $serviceKey = strtolower((string)($item['service'] ?? ''));
        $channelKey = strtolower((string)($item['channel'] ?? ''));
        if (isset($serviceMap[$serviceKey])) {
            $item['service'] = $serviceMap[$serviceKey];
        }
        if (isset($channelMap[$channelKey])) {
            $item['channel'] = $channelMap[$channelKey];
        }
    }
    unset($item);
}

$ratesAsOf = isset($rates['asOf']) ? $rates['asOf'] : null;
$feesAsOf = isset($fees['asOf']) ? $fees['asOf'] : null;
$hasError = $rates === null || $fees === null;

$pageTitle = '金利・手数料';
$extraStyles = <<<CSS
<style>
    .table-wrap { background: #fff; border-radius: 14px; padding: 1rem; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-3">
        <div>
            <h1 class="h3 fw-bold mb-1">金利・手数料</h1>
            <p class="text-muted mb-0">最新データを表示します。</p>
        </div>
        <a href="index.php" class="btn btn-outline-secondary btn-sm">トップへ戻る</a>
    </div>

    <form class="row g-2 mb-3" method="get" action="rates.php">
        <div class="col-md-4">
            <select class="form-select" name="category">
                <option value="">すべての金利カテゴリ</option>
                <option value="deposit" <?= $category === 'deposit' ? 'selected' : '' ?>>預金</option>
                <option value="loan" <?= $category === 'loan' ? 'selected' : '' ?>>ローン</option>
            </select>
        </div>
        <div class="col-md-4">
            <select class="form-select" name="service">
                <option value="transfer" <?= $service === 'transfer' ? 'selected' : '' ?>>振込</option>
                <option value="atm" <?= $service === 'atm' ? 'selected' : '' ?>>ATM</option>
                <option value="card" <?= $service === 'card' ? 'selected' : '' ?>>カード</option>
            </select>
        </div>
        <div class="col-md-4">
            <button class="btn btn-primary w-100" type="submit">表示</button>
        </div>
    </form>

    <?php if ($hasError): ?>
        <div class="alert alert-warning">データ取得に失敗しました。時間をおいて再度お試しください。</div>
    <?php endif; ?>

    <section class="table-wrap mb-4">
        <div class="d-flex justify-content-between align-items-center mb-2">
            <h2 class="h5 mb-0">金利一覧</h2>
            <span class="text-muted small"><?= h($ratesAsOf ? str_replace('T', ' ', substr($ratesAsOf, 0, 16)) : '-') ?></span>
        </div>
        <table class="table align-middle mb-0">
            <thead><tr><th>商品</th><th>期間</th><th class="text-end">金利(%)</th><th>メモ</th></tr></thead>
            <tbody>
                <?php if (count($rateItems) === 0): ?>
                    <tr><td colspan="4" class="text-muted">データがありません。</td></tr>
                <?php else: ?>
                    <?php foreach ($rateItems as $item): ?>
                        <tr>
                            <td><?= h($item['product'] ?? '') ?></td>
                            <td><?= h($item['term'] ?? '-') ?></td>
                            <td class="text-end"><?= h(number_format((float)($item['ratePercent'] ?? 0), 3)) ?></td>
                            <td><?= h($item['note'] ?? '') ?></td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </section>

    <section class="table-wrap">
        <div class="d-flex justify-content-between align-items-center mb-2">
            <h2 class="h5 mb-0">主な手数料</h2>
            <span class="text-muted small"><?= h($feesAsOf ? str_replace('T', ' ', substr($feesAsOf, 0, 16)) : '-') ?></span>
        </div>
        <table class="table align-middle mb-0">
            <thead><tr><th>サービス</th><th>チャネル</th><th class="text-end">手数料(円)</th><th>メモ</th></tr></thead>
            <tbody>
                <?php if (count($feeItems) === 0): ?>
                    <tr><td colspan="4" class="text-muted">データがありません。</td></tr>
                <?php else: ?>
                    <?php foreach ($feeItems as $item): ?>
                        <tr>
                            <td><?= h($item['service'] ?? '') ?></td>
                            <td><?= h($item['channel'] ?? '') ?></td>
                            <td class="text-end"><?= h($item['amountYen'] ?? '') ?></td>
                            <td><?= h($item['note'] ?? '') ?></td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </section>
</main>
<?php require_once 'partials/footer.php'; ?>
