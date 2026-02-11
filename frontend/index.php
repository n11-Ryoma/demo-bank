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

$news = api_get('/api/news?category=maintenance&page=1&size=3');
$rates = api_get('/api/rates');
$fees = api_get('/api/fees?service=transfer');
if (!is_array($fees) || !isset($fees['items']) || !is_array($fees['items']) || count($fees['items']) === 0) {
    $fees = api_get('/api/fees?service=' . rawurlencode('振込'));
}
if (!is_array($fees) || !isset($fees['items']) || !is_array($fees['items']) || count($fees['items']) === 0) {
    $fees = api_get('/api/fees');
}
$alerts = api_get('/api/security-alerts?limit=4');
$faqs = api_get('/api/faq?page=1&size=3');
$atms = api_get('/api/atm?pref=tokyo&open_now=1&cash=1&page=1&size=3');

$newsItems = isset($news['items']) && is_array($news['items']) ? $news['items'] : [];
$rateItems = isset($rates['items']) && is_array($rates['items']) ? $rates['items'] : [];
$feeItems = isset($fees['items']) && is_array($fees['items']) ? $fees['items'] : [];
$alertItems = isset($alerts['items']) && is_array($alerts['items']) ? $alerts['items'] : [];
$faqItems = isset($faqs['items']) && is_array($faqs['items']) ? $faqs['items'] : [];
$atmItems = isset($atms['items']) && is_array($atms['items']) ? $atms['items'] : [];

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
        $service = strtolower((string)($item['service'] ?? ''));
        $channel = strtolower((string)($item['channel'] ?? ''));
        if (isset($serviceMap[$service])) {
            $item['service'] = $serviceMap[$service];
        }
        if (isset($channelMap[$channel])) {
            $item['channel'] = $channelMap[$channel];
        }
    }
    unset($item);
}

$ratesAsOf = isset($rates['asOf']) ? $rates['asOf'] : null;
$feesAsOf = isset($fees['asOf']) ? $fees['asOf'] : null;

$hasError = $news === null || $rates === null || $fees === null || $alerts === null || $faqs === null || $atms === null;

$pageTitle = '+Acts Bank 公式サイト';
$extraStyles = <<<CSS
<style>
    html, body { height: 100%; }

    body {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
    }

    .hero {
        position: relative;
        min-height: 420px;
        color: #fff;
        display: flex;
        align-items: center;
        overflow: hidden;
    }

    .hero-slide {
        position: absolute;
        inset: 0;
        background-size: cover;
        background-position: center;
        opacity: 0;
        transition: opacity 1.2s ease;
    }

    .hero-slide.is-active { opacity: 1; }

    .hero-overlay {
        position: absolute;
        inset: 0;
        background: linear-gradient(120deg, rgba(21, 50, 74, 0.88), rgba(44, 137, 199, 0.72));
    }

    .hero-panel {
        position: relative;
        z-index: 2;
        background: rgba(255, 255, 255, 0.08);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 16px;
        backdrop-filter: blur(3px);
    }

    .menu-card { transition: transform 0.2s ease, box-shadow 0.2s ease; }

    .menu-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(21, 50, 74, 0.12);
    }

    main { flex: 1 0 auto; }
</style>
CSS;

require_once 'partials/header.php';
?>
<header class="hero">
    <div class="hero-slide is-active" style="background-image:url('images/hero-city.jpg');"></div>
    <div class="hero-slide" style="background-image:url('images/abstract-view-urban-scene-skyscrapers.jpg');"></div>
    <div class="hero-slide" style="background-image:url('images/top-view-coins-paper-money-laptop.jpg');"></div>
    <div class="hero-overlay"></div>
    <div class="container">
        <div class="hero-panel p-4 p-md-5 col-lg-8">
            <h1 class="fw-bold mb-3">毎日の資産管理を、もっと安心でシンプルに。</h1>
            <p class="mb-4 fs-5">振込、残高確認、各種手続きまで。+Acts Bankはオンラインで完結する銀行サービスを提供します。</p>
            <div class="d-flex flex-wrap gap-2">
                <a href="login.php" class="btn btn-light px-4">ログイン</a>
                <a href="register.php" class="btn btn-outline-light px-4">はじめての方へ</a>
            </div>
        </div>
    </div>
</header>

<main class="container my-5">
    <section class="mb-5">
        <p class="text-muted mb-4">サービス情報やサポート情報をご確認いただけます。</p>
        <div class="row g-3">
            <div class="col-md-6 col-lg-3">
                <a href="services.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">サービス一覧</h3>
                            <p class="text-muted small mb-0">口座・振込・デビットなど主要サービスを紹介します。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="rates.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">金利・手数料</h3>
                            <p class="text-muted small mb-0">普通預金金利や振込手数料の目安を確認できます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="security.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">セキュリティ</h3>
                            <p class="text-muted small mb-0">不正アクセス対策と安全に使うための注意点です。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="faq.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">よくある質問</h3>
                            <p class="text-muted small mb-0">口座開設やログインに関する質問をまとめています。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="news.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">お知らせ</h3>
                            <p class="text-muted small mb-0">新機能やメンテナンス情報を掲載しています。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="loan.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">ローン案内</h3>
                            <p class="text-muted small mb-0">住宅ローン・カードローンの概要をご覧いただけます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="support.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">お問い合わせ</h3>
                            <p class="text-muted small mb-0">電話窓口やサポート時間をご案内しています。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="company.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">会社情報</h3>
                            <p class="text-muted small mb-0">企業概要・沿革・サステナビリティ情報です。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="campaign.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">キャンペーン</h3>
                            <p class="text-muted small mb-0">口座開設・紹介プログラムなどの特典情報です。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="investment.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">資産運用</h3>
                            <p class="text-muted small mb-0">積立・投信・NISAの基本情報を掲載しています。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="branch.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">店舗・ATM</h3>
                            <p class="text-muted small mb-0">提携ATMや相談窓口の場所をご確認いただけます。</p>
                        </div>
                    </article>
                </a>
            </div>
            <div class="col-md-6 col-lg-3">
                <a href="legal.php" class="text-decoration-none text-dark">
                    <article class="card menu-card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <h3 class="h6 fw-bold">規約・方針</h3>
                            <p class="text-muted small mb-0">利用規約、個人情報保護方針、重要事項説明です。</p>
                        </div>
                    </article>
                </a>
            </div>
        </div>
    </section>

    <?php if ($hasError): ?>
        <div class="alert alert-warning">一部の情報取得に失敗しました。時間をおいて再度お試しください。</div>
    <?php endif; ?>

    <section class="mb-5" id="news-section">
        <div class="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
            <div>
                <h2 class="h5 fw-bold mb-1">最新のお知らせ</h2>
                <p class="text-muted small mb-0">メンテナンス・障害・重要なお知らせを更新しています。</p>
            </div>
            <a href="news.php" class="btn btn-outline-secondary btn-sm">一覧を見る</a>
        </div>
        <div class="row g-3">
            <?php if (count($newsItems) === 0): ?>
                <div class="col-12 text-muted small">現在お知らせはありません。</div>
            <?php else: ?>
                <?php foreach ($newsItems as $item): ?>
                    <div class="col-md-6 col-lg-4">
                        <article class="card border-0 shadow-sm h-100">
                            <div class="card-body">
                                <span class="badge text-bg-secondary mb-2"><?= h($item['category'] ?? '') ?></span>
                                <h3 class="h6 fw-bold"><?= h($item['title'] ?? '') ?></h3>
                                <p class="text-muted small mb-2"><?= h($item['summary'] ?? '') ?></p>
                                <div class="d-flex justify-content-between align-items-center small text-muted">
                                    <span><?= h(substr((string)($item['publishedAt'] ?? ''), 0, 10)) ?></span>
                                    <a href="news.php" class="text-decoration-none">詳細</a>
                                </div>
                            </div>
                        </article>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </section>

    <section class="mb-5" id="rates-section">
        <div class="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
            <div>
                <h2 class="h5 fw-bold mb-1">金利・手数料</h2>
                <p class="text-muted small mb-0">最新の金利と主要手数料の目安です。</p>
            </div>
            <a href="rates.php" class="btn btn-outline-secondary btn-sm">詳細を見る</a>
        </div>
        <div class="row g-3">
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h3 class="h6 fw-bold mb-0">金利一覧</h3>
                            <span class="text-muted small"><?= h($ratesAsOf ? str_replace('T', ' ', substr($ratesAsOf, 0, 16)) : '-') ?></span>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-sm mb-0">
                                <thead>
                                <tr class="text-muted small">
                                    <th>商品</th>
                                    <th>期間</th>
                                    <th class="text-end">金利(%)</th>
                                </tr>
                                </thead>
                                <tbody>
                                <?php if (count($rateItems) === 0): ?>
                                    <tr><td colspan="3" class="text-muted small">データがありません。</td></tr>
                                <?php else: ?>
                                    <?php foreach (array_slice($rateItems, 0, 6) as $item): ?>
                                        <tr>
                                            <td><?= h($item['product'] ?? '') ?></td>
                                            <td><?= h($item['term'] ?? '-') ?></td>
                                            <td class="text-end"><?= h(number_format((float)($item['ratePercent'] ?? 0), 3)) ?></td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h3 class="h6 fw-bold mb-0">主要手数料</h3>
                            <span class="text-muted small"><?= h($feesAsOf ? str_replace('T', ' ', substr($feesAsOf, 0, 16)) : '-') ?></span>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-sm mb-0">
                                <thead>
                                <tr class="text-muted small">
                                    <th>サービス</th>
                                    <th>チャネル</th>
                                    <th class="text-end">手数料(円)</th>
                                </tr>
                                </thead>
                                <tbody>
                                <?php if (count($feeItems) === 0): ?>
                                    <tr><td colspan="3" class="text-muted small">データがありません。</td></tr>
                                <?php else: ?>
                                    <?php foreach (array_slice($feeItems, 0, 6) as $item): ?>
                                        <tr>
                                            <td><?= h($item['service'] ?? '') ?></td>
                                            <td><?= h($item['channel'] ?? '') ?></td>
                                            <td class="text-end"><?= h($item['amountYen'] ?? '') ?></td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="mb-5" id="security-section">
        <div class="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
            <div>
                <h2 class="h5 fw-bold mb-1">セキュリティ注意喚起</h2>
                <p class="text-muted small mb-0">最近多い手口と対策を確認できます。</p>
            </div>
            <a href="security.php" class="btn btn-outline-secondary btn-sm">詳しく見る</a>
        </div>
        <div class="row g-3">
            <?php if (count($alertItems) === 0): ?>
                <div class="col-12 text-muted small">現在の注意喚起はありません。</div>
            <?php else: ?>
                <?php foreach ($alertItems as $item): ?>
                    <div class="col-md-6 col-lg-3">
                        <article class="card border-0 shadow-sm h-100">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="badge text-bg-danger"><?= h($item['riskLevel'] ?? '') ?></span>
                                    <span class="text-muted small">件数 <?= h($item['recentCount'] ?? '') ?></span>
                                </div>
                                <h3 class="h6 fw-bold"><?= h($item['title'] ?? '') ?></h3>
                                <p class="text-muted small mb-0"><?= h($item['tip'] ?? '') ?></p>
                            </div>
                        </article>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </section>

    <section class="mb-5" id="faq-section">
        <div class="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
            <div>
                <h2 class="h5 fw-bold mb-1">よくある質問</h2>
                <p class="text-muted small mb-0">お問い合わせ前にご確認ください。</p>
            </div>
            <a href="faq.php" class="btn btn-outline-secondary btn-sm">FAQ一覧</a>
        </div>
        <div class="row g-3">
            <?php if (count($faqItems) === 0): ?>
                <div class="col-12 text-muted small">FAQがありません。</div>
            <?php else: ?>
                <?php foreach ($faqItems as $item): ?>
                    <div class="col-md-6 col-lg-4">
                        <article class="card border-0 shadow-sm h-100">
                            <div class="card-body">
                                <span class="badge text-bg-light text-muted mb-2"><?= h($item['category'] ?? '') ?></span>
                                <h3 class="h6 fw-bold"><?= h($item['question'] ?? '') ?></h3>
                                <p class="text-muted small mb-0"><?= h($item['answer'] ?? '') ?></p>
                            </div>
                        </article>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </section>

    <section class="mb-5" id="atm-section">
        <div class="d-flex flex-wrap align-items-end justify-content-between gap-2 mb-3">
            <div>
                <h2 class="h5 fw-bold mb-1">近くの店舗・ATM</h2>
                <p class="text-muted small mb-0">現在営業中のATMをピックアップしています。</p>
            </div>
            <a href="branch.php" class="btn btn-outline-secondary btn-sm">店舗・ATM一覧</a>
        </div>
        <div class="row g-3">
            <?php if (count($atmItems) === 0): ?>
                <div class="col-12 text-muted small">該当するATMがありません。</div>
            <?php else: ?>
                <?php foreach ($atmItems as $item): ?>
                    <div class="col-md-6 col-lg-4">
                        <article class="card border-0 shadow-sm h-100">
                            <div class="card-body">
                                <h3 class="h6 fw-bold"><?= h($item['name'] ?? '') ?></h3>
                                <p class="text-muted small mb-2"><?= h($item['address'] ?? '') ?></p>
                                <div class="d-flex justify-content-between align-items-center small">
                                    <span class="text-muted"><?= h($item['hours'] ?? '') ?></span>
                                    <a href="<?= h($item['mapLink'] ?? '#') ?>" target="_blank" rel="noopener" class="text-decoration-none">地図</a>
                                </div>
                            </div>
                        </article>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </section>
</main>

<?php require_once 'partials/footer.php'; ?>
<script>
    (function () {
        const slides = document.querySelectorAll(".hero-slide");
        if (slides.length < 2) return;
        let activeIndex = 0;
        setInterval(function () {
            slides[activeIndex].classList.remove("is-active");
            activeIndex = (activeIndex + 1) % slides.length;
            slides[activeIndex].classList.add("is-active");
        }, 5000);
    })();
</script>
