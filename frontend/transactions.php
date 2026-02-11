<?php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

function h($value)
{
    return htmlspecialchars((string)$value, ENT_QUOTES, 'UTF-8');
}

$error = null;
$items = [];
$findStr = trim($_GET['findStr'] ?? '');
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;
$size = isset($_GET['size']) ? max(5, min(50, (int)$_GET['size'])) : 10;
$offset = ($page - 1) * $size;
$hasNext = false;
$hasPrev = $page > 1;

try {
    $query = [
        'limit' => $size + 1,
        'offset' => $offset,
    ];
    if ($findStr !== '') {
        $query['findStr'] = $findStr;
    }
    $res = api_request('GET', '/api/accounts/transactions?' . http_build_query($query), null, true);
    if ($res['status'] === 200 && is_array($res['body'])) {
        $items = $res['body'];
        if (count($items) > $size) {
            $hasNext = true;
            $items = array_slice($items, 0, $size);
        }
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
        $rawBody = is_string($res['raw'] ?? null) ? trim($res['raw']) : '';
        $errorDetail = $rawBody !== '' ? $rawBody : ($apiError ?: '取引履歴の取得に失敗しました。');
        $error = 'APIエラー (' . $status . '): ' . $errorDetail;
    }
} catch (Exception $e) {
    $error = '通信エラー: ' . $e->getMessage();
}

$pageTitle = '取引明細';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 class="h4 fw-bold mb-0">取引明細</h1>
        <div class="d-flex align-items-center gap-2">
            <form class="d-flex" method="get" action="transactions.php">
                <input type="text" name="findStr" class="form-control form-control-sm" placeholder="メモ検索"
                       value="<?= h($findStr) ?>">
                <input type="hidden" name="size" value="<?= h($size) ?>">
                <button type="submit" class="btn btn-sm btn-outline-primary ms-2">検索</button>
            </form>
            <form method="get" action="transactions.php" class="d-flex align-items-center">
                <input type="hidden" name="findStr" value="<?= h($findStr) ?>">
                <input type="hidden" name="page" value="1">
                <label for="size" class="form-label mb-0 small text-muted me-2">表示件数</label>
                <select id="size" name="size" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="10" <?= $size === 10 ? 'selected' : '' ?>>10件</option>
                    <option value="20" <?= $size === 20 ? 'selected' : '' ?>>20件</option>
                    <option value="50" <?= $size === 50 ? 'selected' : '' ?>>50件</option>
                </select>
            </form>
            <a href="preview.php?tpl=statement.php" class="btn btn-sm btn-outline-primary">明細プレビュー</a>
            <a href="transactions_export.php" class="btn btn-sm btn-outline-secondary">CSVダウンロード</a>
            <a href="dashboard.php" class="btn btn-sm btn-outline-secondary">戻る</a>
        </div>
    </div>

    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php elseif (empty($items)): ?>
        <div class="alert alert-info">取引履歴はありません。</div>
    <?php else: ?>
        <div class="panel p-0 overflow-hidden">
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
                        $typeLabel = match ($type) {
                            'DEPOSIT'      => '入金',
                            'WITHDRAWAL'   => '出金',
                            'TRANSFER_IN'  => '振込入金',
                            'TRANSFER_OUT' => '振込出金',
                            'OPEN'         => '口座開設',
                            default        => $type,
                        };
                        $badgeClass = match ($type) {
                            'DEPOSIT'      => 'bg-success',
                            'WITHDRAWAL'   => 'bg-danger',
                            'TRANSFER_IN'  => 'bg-primary',
                            'TRANSFER_OUT' => 'bg-warning text-dark',
                            default        => 'bg-secondary'
                        };
                        ?>
                        <tr>
                            <td><?= h($row['createdAt'] ?? '') ?></td>
                            <td><?= h($row['accountNumber'] ?? '') ?></td>
                            <td><span class="badge <?= $badgeClass ?>"><?= h($typeLabel) ?></span></td>
                            <td class="text-end"><?= isset($row['amount']) ? number_format((float)$row['amount']) . ' 円' : '' ?></td>
                            <td class="text-end"><?= isset($row['balanceAfter']) ? number_format((float)$row['balanceAfter']) . ' 円' : '' ?></td>
                            <td><?= h($row['relatedAccountNumber'] ?? '') ?></td>
                            <td><?= h($row['description'] ?? '') ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        </div>
        <nav class="mt-3" aria-label="取引履歴ページ">
            <ul class="pagination justify-content-center mb-0">
                <?php
                $baseParams = ['findStr' => $findStr, 'size' => $size];
                $prevPage = max(1, $page - 1);
                $firstPage = max(1, $page - 2);
                $lastPage = $page + ($hasNext ? 1 : 0);
                ?>
                <li class="page-item <?= $hasPrev ? '' : 'disabled' ?>">
                    <a class="page-link" href="transactions.php?<?= h(http_build_query($baseParams + ['page' => $prevPage])) ?>">前へ</a>
                </li>
                <?php for ($p = $firstPage; $p <= $lastPage; $p++): ?>
                    <li class="page-item <?= $p === $page ? 'active' : '' ?>">
                        <a class="page-link" href="transactions.php?<?= h(http_build_query($baseParams + ['page' => $p])) ?>"><?= h($p) ?></a>
                    </li>
                <?php endfor; ?>
                <li class="page-item <?= $hasNext ? '' : 'disabled' ?>">
                    <a class="page-link" href="transactions.php?<?= h(http_build_query($baseParams + ['page' => $page + 1])) ?>">次へ</a>
                </li>
            </ul>
        </nav>
    <?php endif; ?>
</main>
<?php require_once 'partials/footer.php'; ?>
