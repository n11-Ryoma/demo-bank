<?php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$error = null;
$items = [];
$findStr = trim($_GET['findStr'] ?? '');

try {
    $query = [
        'limit' => 50,
        'offset' => 0,
    ];
    if ($findStr !== '') {
        $query['findStr'] = $findStr;
    }
    $res = api_request('GET', '/api/accounts/transactions?' . http_build_query($query), null, true);
    if ($res['status'] === 200 && is_array($res['body'])) {
        $items = $res['body'];
    } else {
        $apiError = null;
        if (is_string($res['body'] ?? null)) {
            $apiError = $res['body'];
        } elseif (is_array($res['body'] ?? null)) {
            $apiError = $res['body']['message']
                ?? $res['body']['error']
                ?? json_encode($res['body'], JSON_UNESCAPED_UNICODE);
        }
        $status = $res['status'] ?? 'unknown';
        $error = 'APIエラー (' . $status . '): ' . ($apiError ?: '取引履歴の取得に失敗しました、E');
    }
} catch (Exception $e) {
    $error = '通信エラー: ' . $e->getMessage();
}

$username = $_SESSION['username'] ?? '';
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>取引履歴 - eBank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-4">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="dashboard.php">
            <img src="images/logo.png" alt="eBank Logo"
                 style="height:100px; margin-right:8px;">
            <span class="fw-bold">+Acts Bank</span>
        </a>
        <div class="d-flex">
            <span class="navbar-text me-3">
                <?= htmlspecialchars($username, ENT_QUOTES, 'UTF-8') ?> さん
            </span>
            <a href="dashboard.php" class="btn btn-light btn-sm me-2">ダッシュボード</a>
        </div>
    </div>
</nav>

<div class="container">
    <h2 class="mb-3 d-flex justify-content-between align-items-center">
        <span>取引履歴</span>
        <div class="d-flex align-items-center gap-2">
            <form class="d-flex" method="get" action="transactions.php">
                <input type="text" name="findStr" class="form-control form-control-sm" placeholder="メモ検索"
                       value="<?= htmlspecialchars($findStr, ENT_QUOTES, 'UTF-8') ?>">
                <button type="submit" class="btn btn-sm btn-outline-primary ms-2">検索</button>
            </form>
            <!-- ☁E明細プレビュー�E�E�E�Ereview.php を�E然に呼ぶ�E�E�E�E-->
            <a href="preview.php?tpl=statement.php" class="btn btn-sm btn-outline-primary">
                明細プレビュー
            </a>
            <!-- 既存�E CSV ダウンローチE-->
            <a href="transactions_export.php" class="btn btn-sm btn-outline-secondary">
                CSVダウンロード
            </a>
        </div>
    </h2>

    <?php if ($error): ?>
        <div class="alert alert-danger"><?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?></div>
    <?php elseif (empty($items)): ?>
        <div class="alert alert-info">取引履歴はありません、E</div>
    <?php else: ?>
        <div class="card shadow-sm">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-striped mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">日晁E</th>
                            <th scope="col">口座番号</th>
                            <th scope="col">種別</th>
                            <th scope="col" class="text-end">金額</th>
                            <th scope="col" class="text-end">残高（取引後）</th>
                            <th scope="col">相手口座</th>
                            <th scope="col">メモ</th>
                        </tr>
                        </thead>
                        <tbody>
                        <?php foreach ($items as $row): ?>
                            <?php
                            $type = $row['type'] ?? '';
                            $badgeClass = match ($type) {
                                'DEPOSIT'       => 'bg-success',
                                'WITHDRAW'      => 'bg-danger',
                                'TRANSFER_IN'   => 'bg-primary',
                                'TRANSFER_OUT'  => 'bg-warning text-dark',
                                default         => 'bg-secondary'
                            };
                            ?>
                            <tr>
                                <td><?= htmlspecialchars($row['createdAt'] ?? '', ENT_QUOTES, 'UTF-8') ?></td>
                                <td><?= htmlspecialchars($row['accountNumber'] ?? '', ENT_QUOTES, 'UTF-8') ?></td>
                                <td>
                                    <span class="badge <?= $badgeClass ?>">
                                        <?= htmlspecialchars($type, ENT_QUOTES, 'UTF-8') ?>
                                    </span>
                                </td>
                                <td class="text-end">
                                    <?= isset($row['amount']) ? number_format($row['amount']) . ' 円' : '' ?>
                                </td>
                                <td class="text-end">
                                    <?= isset($row['balanceAfter']) ? number_format($row['balanceAfter']) . ' 円' : '' ?>
                                </td>
                                <td><?= htmlspecialchars($row['relatedAccountNumber'] ?? '', ENT_QUOTES, 'UTF-8') ?></td>
                                <td><?= htmlspecialchars($row['description'] ?? '', ENT_QUOTES, 'UTF-8') ?></td>
                            </tr>
                        <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    <?php endif; ?>

</div>

</body>
</html>




