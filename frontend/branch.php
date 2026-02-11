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

$pref = isset($_GET['pref']) ? trim($_GET['pref']) : 'tokyo';
$openNow = isset($_GET['open_now']) ? (int)$_GET['open_now'] : 1;
$cash = isset($_GET['cash']) ? (int)$_GET['cash'] : 1;
$q = isset($_GET['q']) ? trim($_GET['q']) : '';
$service = isset($_GET['service']) ? trim($_GET['service']) : '';
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;
$size = isset($_GET['size']) ? max(1, min(50, (int)$_GET['size'])) : 10;

$queryParams = array_filter([
    'pref' => $pref !== '' ? $pref : null,
    'open_now' => $openNow,
    'cash' => $cash,
    'q' => $q !== '' ? $q : null,
    'service' => $service !== '' ? $service : null,
    'page' => $page,
    'size' => $size,
], static fn($v) => $v !== null);
$query = http_build_query($queryParams);

$data = api_get('/api/atm?' . $query);
$items = isset($data['items']) && is_array($data['items']) ? $data['items'] : [];
$total = isset($data['total']) ? (int)$data['total'] : 0;
$hasError = $data === null;
$totalPages = max(1, (int)ceil($total / max(1, $size)));
$page = min($page, $totalPages);
$pageBaseParams = [
    'pref' => $pref,
    'open_now' => $openNow,
    'cash' => $cash,
    'q' => $q,
    'service' => $service,
    'size' => $size,
];
$prefOptions = [
    'hokkaido' => '北海道',
    'aomori' => '青森県',
    'iwate' => '岩手県',
    'miyagi' => '宮城県',
    'akita' => '秋田県',
    'yamagata' => '山形県',
    'fukushima' => '福島県',
    'ibaraki' => '茨城県',
    'tochigi' => '栃木県',
    'gunma' => '群馬県',
    'saitama' => '埼玉県',
    'chiba' => '千葉県',
    'tokyo' => '東京都',
    'kanagawa' => '神奈川県',
    'niigata' => '新潟県',
    'toyama' => '富山県',
    'ishikawa' => '石川県',
    'fukui' => '福井県',
    'yamanashi' => '山梨県',
    'nagano' => '長野県',
    'gifu' => '岐阜県',
    'shizuoka' => '静岡県',
    'aichi' => '愛知県',
    'mie' => '三重県',
    'shiga' => '滋賀県',
    'kyoto' => '京都府',
    'osaka' => '大阪府',
    'hyogo' => '兵庫県',
    'nara' => '奈良県',
    'wakayama' => '和歌山県',
    'tottori' => '鳥取県',
    'shimane' => '島根県',
    'okayama' => '岡山県',
    'hiroshima' => '広島県',
    'yamaguchi' => '山口県',
    'tokushima' => '徳島県',
    'kagawa' => '香川県',
    'ehime' => '愛媛県',
    'kochi' => '高知県',
    'fukuoka' => '福岡県',
    'saga' => '佐賀県',
    'nagasaki' => '長崎県',
    'kumamoto' => '熊本県',
    'oita' => '大分県',
    'miyazaki' => '宮崎県',
    'kagoshima' => '鹿児島県',
    'okinawa' => '沖縄県',
];

$pageTitle = '店舗・ATM';
$extraStyles = <<<CSS
<style>
    .panel { background: #fff; border-radius: 14px; box-shadow: 0 8px 16px rgba(18, 50, 74, .08); }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-3">
        <div>
            <h1 class="h3 fw-bold mb-1">店舗・ATM検索</h1>
            <p class="text-muted mb-0">現在営業中のATMや対応サービスで絞り込みできます。</p>
        </div>
        <a href="index.php" class="btn btn-outline-secondary btn-sm">トップへ戻る</a>
    </div>

    <form class="row g-2 mb-4" method="get" action="branch.php">
        <div class="col-md-3">
            <select class="form-select" name="pref">
                <option value="">都道府県</option>
                <?php foreach ($prefOptions as $prefCode => $prefLabel): ?>
                    <option value="<?= h($prefCode) ?>" <?= $pref === $prefCode ? 'selected' : '' ?>><?= h($prefLabel) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-md-3">
            <input type="text" class="form-control" name="q" placeholder="キーワード" value="<?= h($q) ?>">
        </div>
        <div class="col-md-2">
            <select class="form-select" name="service">
                <option value="">サービス</option>
                <option value="withdraw" <?= $service === 'withdraw' ? 'selected' : '' ?>>出金</option>
                <option value="deposit" <?= $service === 'deposit' ? 'selected' : '' ?>>入金</option>
                <option value="transfer" <?= $service === 'transfer' ? 'selected' : '' ?>>振込</option>
                <option value="loan" <?= $service === 'loan' ? 'selected' : '' ?>>ローン</option>
            </select>
        </div>
        <div class="col-md-2">
            <select class="form-select" name="open_now">
                <option value="0" <?= $openNow === 0 ? 'selected' : '' ?>>営業時間指定なし</option>
                <option value="1" <?= $openNow === 1 ? 'selected' : '' ?>>営業中のみ</option>
            </select>
        </div>
        <div class="col-md-2">
            <select class="form-select" name="cash">
                <option value="0" <?= $cash === 0 ? 'selected' : '' ?>>現金指定なし</option>
                <option value="1" <?= $cash === 1 ? 'selected' : '' ?>>現金対応</option>
            </select>
        </div>
        <div class="col-md-2">
            <button class="btn btn-primary w-100" type="submit">検索</button>
        </div>
    </form>

    <?php if ($hasError): ?>
        <div class="alert alert-warning">データ取得に失敗しました。時間をおいて再度お試しください。</div>
    <?php endif; ?>

    <p class="text-muted small mb-3">該当件数: <?= h($total) ?> 件</p>

    <div class="row g-3">
        <?php if (count($items) === 0): ?>
            <div class="col-12 text-muted">該当するATMがありません。</div>
        <?php else: ?>
            <?php foreach ($items as $item): ?>
                <div class="col-md-6 col-lg-4">
                    <section class="panel p-4 h-100">
                        <h2 class="h6 fw-bold mb-1"><?= h($item['name'] ?? '') ?></h2>
                        <p class="text-muted mb-2 small"><?= h($item['address'] ?? '') ?></p>
                        <div class="d-flex justify-content-between align-items-center small">
                            <span class="text-muted"><?= h($item['hours'] ?? '') ?></span>
                            <a href="<?= h($item['mapLink'] ?? '#') ?>" target="_blank" rel="noopener" class="text-decoration-none">地図</a>
                        </div>
                    </section>
                </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>

    <?php if ($totalPages > 1): ?>
        <nav class="mt-4" aria-label="検索結果ページ">
            <ul class="pagination justify-content-center mb-0">
                <?php $prevPage = max(1, $page - 1); ?>
                <li class="page-item <?= $page <= 1 ? 'disabled' : '' ?>">
                    <a class="page-link" href="branch.php?<?= h(http_build_query($pageBaseParams + ['page' => $prevPage])) ?>" aria-label="前へ">前へ</a>
                </li>
                <?php for ($p = 1; $p <= $totalPages; $p++): ?>
                    <li class="page-item <?= $p === $page ? 'active' : '' ?>">
                        <a class="page-link" href="branch.php?<?= h(http_build_query($pageBaseParams + ['page' => $p])) ?>"><?= h($p) ?></a>
                    </li>
                <?php endfor; ?>
                <?php $nextPage = min($totalPages, $page + 1); ?>
                <li class="page-item <?= $page >= $totalPages ? 'disabled' : '' ?>">
                    <a class="page-link" href="branch.php?<?= h(http_build_query($pageBaseParams + ['page' => $nextPage])) ?>" aria-label="次へ">次へ</a>
                </li>
            </ul>
        </nav>
    <?php endif; ?>
</main>
<?php require_once 'partials/footer.php'; ?>
