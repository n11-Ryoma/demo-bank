<?php
// templates/statement.php
// preview.php で config.php とセッションチェックは済んでいる想定

$error = null;
$items = [];

try {
    // 明細プレビュー用に、直近100件くらい取ってくる
    $res = api_request('GET', '/api/accounts/transactions?limit=100&offset=0', null, true);
    if ($res['status'] === 200 && is_array($res['body'])) {
        $items = $res['body'];
    } else {
        $error = '明細情報の取得に失敗しました。';
    }
} catch (Exception $e) {
    $error = '通信エラー: ' . $e->getMessage();
}

// 画面上部に表示する情報をざっくり整形
$holderName    = $_SESSION['username'] ?? 'お客さま';
$accountNumber = $items[0]['accountNumber'] ?? '************';

$today = new DateTime('now');

$totalIn  = 0;
$totalOut = 0;
$latestBalance = null;

// 金額サマリ
foreach ($items as $row) {
    $type   = $row['type']   ?? '';
    $amount = isset($row['amount']) ? (int)$row['amount'] : 0;

    if (in_array($type, ['DEPOSIT', 'TRANSFER_IN'], true)) {
        $totalIn += $amount;
    } elseif (in_array($type, ['WITHDRAW', 'TRANSFER_OUT'], true)) {
        $totalOut += $amount;
    }

    if ($latestBalance === null && isset($row['balanceAfter'])) {
        // 一番最初のレコードを「最新残高」として扱う（APIが降順前提）
        $latestBalance = (int)$row['balanceAfter'];
    }
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>入出金明細プレビュー - +Acts Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f5f5f5;
        }
        .statement-header {
            border-bottom: 2px solid #0d6efd;
            margin-bottom: 1.5rem;
            padding-bottom: 0.5rem;
        }
        .statement-box {
            background: #ffffff;
            border-radius: 0.5rem;
            padding: 1.5rem;
            box-shadow: 0 0.25rem 0.75rem rgba(0,0,0,0.05);
        }
        .statement-summary {
            background: #f8f9fa;
            border-radius: 0.5rem;
            padding: 1rem;
            margin-bottom: 1.5rem;
        }
        .statement-summary dt {
            font-size: 0.9rem;
            color: #6c757d;
        }
        .statement-summary dd {
            font-weight: 600;
            font-size: 1rem;
        }
        @media print {
            body {
                background: #ffffff !important;
            }
            .no-print {
                display: none !important;
            }
            .statement-box {
                box-shadow: none;
                border: none;
                padding: 0;
            }
        }
    </style>
</head>
<body class="bg-light">

<!-- 他画面と同じナビバー（パスは preview.php 基準なので ../ は不要） -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-4">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="dashboard.php">
            <img src="images/logo.png" alt="+Acts Bank Logo"
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

<div class="container py-4">

    <div class="statement-header d-flex justify-content-between align-items-center mb-3">
        <div>
            <h2 class="h4 mb-1">ご利用明細書</h2>
            <small class="text-muted">
                作成日: <?= htmlspecialchars($today->format('Y年n月j日'), ENT_QUOTES, 'UTF-8') ?>
            </small>
        </div>
        <div class="text-end">
            <div class="fw-bold">
                <?= htmlspecialchars($holderName, ENT_QUOTES, 'UTF-8') ?> 様
            </div>
            <div class="text-muted">
                口座番号: <?= htmlspecialchars($accountNumber, ENT_QUOTES, 'UTF-8') ?>
            </div>
        </div>
    </div>


    <div class="statement-box">

        <?php if ($error): ?>
            <div class="alert alert-danger mb-0">
                <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
            </div>
        <?php elseif (empty($items)): ?>
            <div class="alert alert-info mb-0">
                対象期間内の取引はありません。
            </div>
        <?php else: ?>

            <!-- サマリ情報 -->
            <div class="statement-summary">
                <div class="row">
                    <div class="col-md-4 mb-3 mb-md-0">
                        <dl class="mb-0">
                            <dt>期間内入金合計</dt>
                            <dd><?= number_format($totalIn) ?> 円</dd>
                        </dl>
                    </div>
                    <div class="col-md-4 mb-3 mb-md-0">
                        <dl class="mb-0">
                            <dt>期間内出金合計</dt>
                            <dd><?= number_format($totalOut) ?> 円</dd>
                        </dl>
                    </div>
                    <div class="col-md-4">
                        <dl class="mb-0">
                            <dt>最新残高</dt>
                            <dd><?= $latestBalance !== null ? number_format($latestBalance) . ' 円' : '―' ?></dd>
                        </dl>
                    </div>
                </div>
            </div>

            <!-- 明細テーブル -->
            <div class="table-responsive">
                <table class="table table-sm table-striped align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">日時</th>
                        <th scope="col">種別</th>
                        <th scope="col" class="text-end">金額</th>
                        <th scope="col" class="text-end">残高（取引後）</th>
                        <th scope="col">お取引内容</th>
                        <th scope="col">お相手口座</th>
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
                            <td><?= htmlspecialchars($row['description'] ?? '', ENT_QUOTES, 'UTF-8') ?></td>
                            <td><?= htmlspecialchars($row['relatedAccountNumber'] ?? '', ENT_QUOTES, 'UTF-8') ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>

            <div class="mt-3 text-muted small">
                本画面は +Acts Bank の取引データをもとに作成したご利用明細のプレビューです。<br>
                正式な残高証明や証憑が必要な場合は、CSVダウンロード機能や別途発行される書面をご利用ください。
            </div>

        <?php endif; ?>

    </div>

    <div class="mt-3 no-print">
        <a href="transactions.php" class="btn btn-outline-secondary btn-sm">
            &laquo; 取引履歴に戻る
        </a>
        <button onclick="window.print();" class="btn btn-primary btn-sm ms-2">
            この明細を印刷
        </button>
    </div>

</div>
</body>
</html>
