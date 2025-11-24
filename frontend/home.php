<?php
session_start();

$apiBaseUrl = "http://192.168.1.59:8080";

// ログインしてなければログインページへ
if (!isset($_SESSION['token'])) {
    header('Location: login.php');
    exit;
}

$token         = $_SESSION['token'];
$accountNumber = $_SESSION['accountNumber'] ?? '不明な口座';
$username      = $_SESSION['username'] ?? 'ゲスト';

$error = null;
$balanceData = null;

// 残高API呼び出し
$balanceUrl = $apiBaseUrl . '/api/accounts/balance';
$payload = json_encode(['accountNumber' => $accountNumber]);

$ch = curl_init($balanceUrl);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'Authorization: Bearer ' . $token,
    'Accept: application/json'
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($response === false || $httpCode !== 200) {
    $error = "残高情報を取得できませんでした。（HTTP {$httpCode}）";
} else {
    $balanceData = json_decode($response, true);
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>EBank ホーム</title>
    <style>
        :root {
            --bg: #f5f7fb;
            --card-bg: #ffffff;
            --primary: #1976d2;
            --primary-dark: #145ca5;
            --text-main: #1f2933;
            --text-sub: #6b7280;
            --danger-bg: #ffebee;
            --danger-text: #c62828;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            background: var(--bg);
            color: var(--text-main);
        }

        .navbar {
            height: 56px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 24px;
            background: #ffffff;
            box-shadow: 0 2px 6px rgba(0,0,0,0.06);
            position: sticky;
            top: 0;
            z-index: 10;
        }

        .navbar-logo {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            font-size: 18px;
        }
        .navbar-logo-icon {
            width: 24px;
            height: 24px;
            border-radius: 8px;
            background: var(--primary);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-size: 14px;
        }

        .navbar-right {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 13px;
        }

        .badge-user {
            padding: 4px 10px;
            border-radius: 999px;
            background: #e5f0ff;
            color: var(--primary-dark);
        }

        .btn-logout {
            border: none;
            border-radius: 999px;
            padding: 6px 12px;
            font-size: 12px;
            cursor: pointer;
            background: #f1f5f9;
        }
        .btn-logout:hover {
            background: #e2e8f0;
        }

        .page-container {
            max-width: 960px;
            margin: 24px auto 40px;
            padding: 0 16px;
        }

        .page-title {
            font-size: 20px;
            margin-bottom: 4px;
        }
        .page-subtitle {
            font-size: 13px;
            color: var(--text-sub);
            margin-bottom: 20px;
        }

        .layout {
            display: grid;
            grid-template-columns: 2.1fr 1.2fr;
            gap: 16px;
        }

        .card {
            background: var(--card-bg);
            border-radius: 16px;
            padding: 18px 20px;
            box-shadow: 0 8px 20px rgba(15,23,42,0.06);
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }

        .card-title {
            font-size: 15px;
            font-weight: 600;
        }

        .card-caption {
            font-size: 12px;
            color: var(--text-sub);
        }

        .account-label {
            font-size: 12px;
            color: var(--text-sub);
        }

        .account-number {
            font-size: 14px;
            margin-top: 2px;
        }

        .balance-amount {
            margin-top: 14px;
            font-size: 30px;
            font-weight: 700;
        }

        .balance-amount span.unit {
            font-size: 15px;
            font-weight: 500;
            margin-left: 4px;
            color: var(--text-sub);
        }

        .quick-actions {
            display: flex;
            gap: 8px;
            margin-top: 20px;
        }

        .btn-primary,
        .btn-outline {
            flex: 1;
            padding: 9px 0;
            border-radius: 999px;
            font-size: 13px;
            border: none;
            cursor: pointer;
        }

        .btn-primary {
            background: var(--primary);
            color: #fff;
        }
        .btn-primary:hover {
            background: var(--primary-dark);
        }

        .btn-outline {
            background: transparent;
            border: 1px solid #d1d5db;
            color: #374151;
        }
        .btn-outline:hover {
            background: #f9fafb;
        }

        .error {
            margin-bottom: 16px;
            padding: 10px 12px;
            border-radius: 10px;
            background: var(--danger-bg);
            color: var(--danger-text);
            font-size: 13px;
        }

        .card-secondary-title {
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 12px;
        }

        .list {
            list-style: none;
            margin: 0;
            padding: 0;
            font-size: 13px;
            color: var(--text-sub);
        }
        .list li {
            padding: 6px 0;
            display: flex;
            justify-content: space-between;
        }
        .badge-muted {
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 999px;
            background: #e5e7eb;
            color: #374151;
        }

        @media (max-width: 720px) {
            .layout {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<header class="navbar">
    <div class="navbar-logo">
        <div class="navbar-logo-icon">₿</div>
        <span>EBank</span>
    </div>
    <div class="navbar-right">
        <span class="badge-user">ようこそ、<?php echo htmlspecialchars($username, ENT_QUOTES, 'UTF-8'); ?> さん</span>
        <form method="post" action="logout.php" style="margin:0;">
            <button class="btn-logout" type="submit">ログアウト</button>
        </form>
    </div>
</header>

<div class="page-container">
    <h2 class="page-title">ホーム</h2>
    <p class="page-subtitle">メイン口座の残高と、よく使うメニューを表示します。</p>

    <?php if ($error): ?>
        <div class="error"><?php echo htmlspecialchars($error, ENT_QUOTES, 'UTF-8'); ?></div>
    <?php endif; ?>

    <div class="layout">
        <!-- メインカード：残高 -->
        <section class="card">
            <div class="card-header">
                <div>
                    <div class="card-title">普通預金口座</div>
                    <div class="card-caption">メイン口座</div>
                </div>
            </div>

            <div>
                <div class="account-label">口座番号</div>
                <div class="account-number">
                    <?php echo htmlspecialchars($accountNumber, ENT_QUOTES, 'UTF-8'); ?>
                </div>

                <?php if ($balanceData): ?>
                    <div class="balance-amount">
                        <?php echo number_format((int)$balanceData['balance']); ?>
                        <span class="unit">円</span>
                    </div>
                <?php else: ?>
                    <div class="balance-amount">
                        ---,---
                        <span class="unit">円</span>
                    </div>
                <?php endif; ?>
            </div>

            <div class="quick-actions">
                <button class="btn-primary" type="button" disabled>振込（準備中）</button>
                <button class="btn-outline" type="button" disabled>入出金明細（準備中）</button>
            </div>
        </section>

        <!-- サイドカード：ダミー情報とか -->
        <aside class="card">
            <div class="card-secondary-title">お知らせ</div>
            <ul class="list">
                <li>
                    <span>システムメンテナンス</span>
                    <span class="badge-muted">ダミー</span>
                </li>
                <li>
                    <span>セキュリティに関するご案内</span>
                    <span class="badge-muted">ダミー</span>
                </li>
                <li>
                    <span>新しい振込画面（演習用）</span>
                    <span class="badge-muted">準備中</span>
                </li>
            </ul>
        </aside>
    </div>
</div>
</body>
</html>
