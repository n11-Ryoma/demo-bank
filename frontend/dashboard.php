<?php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$balance = null;
$accountNumber = null;
$error = null;

try {
    $res = api_request('GET', '/api/accounts/balance', null, true);
    if ($res['status'] === 200) {
        $balance = $res['body']['balance'];
        $accountNumber = $res['body']['accountNumber'];
    } else {
        $error = '残高取得に失敗しました。';
    }
} catch (Exception $e) {
    $error = '通信エラー: ' . $e->getMessage();
}

$flashMessage = flash('flash_message');
$flashError   = flash('flash_error');
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>eBank ダッシュボード</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<!-- 上部ナビバー -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-0">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="dashboard.php">
        <img src="images/logo.png" alt="eBank Logo"
            style="height:100px; margin-right:8px;">

            <span class="fw-bold">+Acts Bank</span>
        </a>
        <div class="d-flex">
            <span class="navbar-text me-3 text-white">
                <?= htmlspecialchars($_SESSION['username'] ?? '', ENT_QUOTES, 'UTF-8') ?> さん
            </span>
            <a href="logout.php" class="btn btn-light btn-sm">ログアウト</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row">

        <!-- 左サイドメニュー -->
        <nav class="col-md-3 col-lg-2 d-md-block bg-white border-end vh-100 p-3">
            <h6 class="text-muted small">メニュー</h6>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link active fw-bold" href="dashboard.php">
                        残高照会
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="transactions.php">
                        取引明細
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#" onclick="document.getElementById('transfer').scrollIntoView();">
                        振込
                    </a>
                </li>

            </ul>
        </nav>

        <!-- メイン領域 -->
        <main class="col-md-9 col-lg-10 p-4">

            <?php if ($flashMessage): ?>
                <div class="alert alert-success"><?= htmlspecialchars($flashMessage) ?></div>
            <?php endif; ?>

            <?php if ($flashError): ?>
                <div class="alert alert-danger"><?= htmlspecialchars($flashError) ?></div>
            <?php endif; ?>

            <?php if ($error): ?>
                <div class="alert alert-danger"><?= htmlspecialchars($error) ?></div>
            <?php endif; ?>

            <!-- 残高カード -->
            <div class="card shadow-sm mb-4">
                <div class="card-body">
                    <h5 class="card-title mb-3">メイン口座</h5>

                    <p class="text-muted mb-1">口座番号</p>
                    <p class="fs-6 fw-semibold"><?= htmlspecialchars($accountNumber) ?></p>

                    <p class="text-muted mb-1">現在残高</p>
                    <p class="fs-2 fw-bold text-primary"><?= number_format($balance) ?> 円</p>

                    <!-- 🔥 取引明細ボタン（遷移できる版） -->
                    <div class="col-12 mt-4">
                        <a href="transactions.php" class="btn btn-primary w-100">
                            取引明細を見る
                        </a>
                    </div>
                </div>
            </div>


            <!-- 振込フォーム -->
            <div id="transfer" class="card shadow-sm">
                <div class="card-header bg-white">
                    <h5 class="mb-0">振込</h5>
                </div>
                <div class="card-body">
                    <form method="post" action="do_transfer.php" class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label">振込先口座番号</label>
                            <input type="text" name="toAccountNumber" class="form-control" required>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">金額</label>
                            <div class="input-group">
                                <input type="number" name="amount" class="form-control" min="1" required>
                                <span class="input-group-text">円</span>
                            </div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">メモ（任意）</label>
                            <input type="text" name="description" class="form-control" placeholder="家賃など">
                        </div>

                        <div class="col-12">
                            <button class="btn btn-primary w-100" type="submit">振込する</button>
                        </div>
                    </form>
                </div>
            </div>

        </main>

    </div>
</div>

</body>
</html>
