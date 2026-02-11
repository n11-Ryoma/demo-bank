<?php
header('Content-Type: text/html; charset=UTF-8');
if (!isset($pageTitle) || $pageTitle === '') {
    $pageTitle = '+Acts Bank';
}
if (!isset($extraStyles)) {
    $extraStyles = '';
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= htmlspecialchars($pageTitle, ENT_QUOTES, 'UTF-8') ?> | +Acts Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --bank-navy: #15324a;
            --bank-sky: #2c89c7;
            --bank-bg: #f4f7fb;
        }

        html, body {
            height: 100%;
        }

        body {
            background: var(--bank-bg);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .topbar {
            background: var(--bank-navy);
        }

        .brand-logo {
            height: 42px;
            width: auto;
            display: block;
        }

        .brand-text {
            font-weight: 700;
            font-size: 1.35rem;
            line-height: 1;
            color: #fff;
            margin-left: 10px;
            white-space: nowrap;
        }

        .page-shell {
            padding-top: 2rem;
            padding-bottom: 3rem;
            flex: 1 0 auto;
        }

        .panel {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 8px 16px rgba(18, 50, 74, .08);
        }
    </style>
    <?= $extraStyles ?>
</head>
<body>
<nav class="navbar topbar navbar-dark border-bottom">
    <div class="container">
        <a class="navbar-brand fw-bold d-flex align-items-center" href="index.php" aria-label="+Acts Bank ホーム">
            <img src="images/logo.png" alt="+Acts Bank" class="brand-logo">
            <span class="brand-text">+Acts Bank</span>
        </a>
        <div class="d-flex flex-wrap gap-2">
            <a href="services.php" class="btn btn-outline-light btn-sm">サービス</a>
            <a href="rates.php" class="btn btn-outline-light btn-sm">金利・手数料</a>
            <a href="security.php" class="btn btn-outline-light btn-sm">セキュリティ</a>
            <a href="faq.php" class="btn btn-outline-light btn-sm">FAQ</a>
            <a href="login.php" class="btn btn-light btn-sm">ログイン</a>
        </div>
    </div>
</nav>
