<?php
// create_preview.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

// ★ username は GET 優先（CTF 的にはここを汚せるとおいしい）
$rawUsername = $_GET['username'] ?? ($_SESSION['username'] ?? 'guest');

// 表示用はちゃんとエスケープ
$displayUsername = $rawUsername;

// 取引情報を「全件っぽく」取得
$error = null;
$items = [];

try {
    // limit 大きめにしておく
    $res = api_request('GET', '/api/accounts/transactions?limit=1000&offset=0', null, true);
    if ($res['status'] === 200 && is_array($res['body'])) {
        $items = $res['body'];
    } else {
        $error = '取引履歴の取得に失敗しました。';
    }
} catch (Exception $e) {
    $error = '通信エラー: ' . $e->getMessage();
}

// ★★ LFI 用のテンプレートファイルをこっそり生成 ★★
// ★★ LFI 用のテンプレートファイルをこっそり生成 ★★
$templateDir = __DIR__ . '/templates';
if (!is_dir($templateDir)) {
    mkdir($templateDir, 0775, true);
}
$templateFile = $templateDir . '/statement_preview_' . $rawUsername . '.php';

// username をそのまま PHP のシングルクォート文字列に突っ込む（脆弱ポイントにしたいならここ）
$vulnPhp  = "<?php\n";
$vulnPhp .= "\$u = '" . $rawUsername . "';\n";
$vulnPhp .= "echo 'ユーザ: ' . \$u . \"\\n\";\n";
$vulnPhp .= "?>\n";

file_put_contents($templateFile, $vulnPhp);


?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>取引明細プレビュー - eBank</title>
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
                <?= htmlspecialchars($_SESSION['username'] ?? '', ENT_QUOTES, 'UTF-8') ?> さん
            </span>
            <a href="dashboard.php" class="btn btn-light btn-sm">ダッシュボード</a>
        </div>
    </div>
</nav>

<div class="container">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="mb-0">取引明細プレビュー</h2>
        <div class="btn-group">
            <a href="transactions.php" class="btn btn-sm btn-outline-secondary">
                取引履歴に戻る
            </a>
        </div>
    </div>

    <p class="text-muted mb-3">
        ご利用者:
        <span class="fw-bold">
            <?= htmlspecialchars($displayUsername, ENT_QUOTES, 'UTF-8') ?>
        </span>
        さんの取引明細プレビューです。
    </p>

    <?php if ($error): ?>
        <div class="alert alert-danger">
            <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
        </div>
    <?php elseif (empty($items)): ?>
        <div class="alert alert-info">
            取引履歴はありません。
        </div>
    <?php else: ?>
        <div class="card shadow-sm">
            <div class="card-header bg-white">
                <strong>取引一覧（プレビュー）</strong>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-striped mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">日時</th>
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

    <div class="mt-3 text-muted small">
        ※ この画面は印刷・確認用のプレビューです。
    </div>
</div>

</body>
</html>
