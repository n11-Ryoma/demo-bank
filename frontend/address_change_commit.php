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

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: address_change.php');
    exit;
}

$addr = $_SESSION['addr_change'] ?? null;
if ($addr === null) {
    $status = 'error';
    $message = '住所変更情報が見つかりません。もう一度入力してください。';
} else {
    $postal = $addr['postal'] ?? '';
    $pref = $addr['pref'] ?? '';
    $city = $addr['city'] ?? '';
    $address1 = $addr['address1'] ?? '';
    $address2 = $addr['address2'] ?? '';
    $filePath = $addr['file_path'] ?? '';

    $tempFullPath = __DIR__ . '/uploads/' . $filePath;

    if ($filePath === '' || !is_readable($tempFullPath)) {
        $status = 'error';
        $message = 'アップロードされた住所確認書類が見つかりません。もう一度アップロードしてください。';
    } else {
        $fileData = file_get_contents($tempFullPath);
        $fileBase64 = base64_encode($fileData);

        $body = [
            'postalCode' => $postal,
            'prefecture' => $pref,
            'city' => $city,
            'addressLine1' => $address1,
            'addressLine2' => $address2,
            'fileName' => basename($tempFullPath),
            'fileBase64' => $fileBase64,
        ];

        try {
            $res = api_request('POST', '/api/address-change/commit', $body, true);

            if ($res['status'] === 200) {
                unset($_SESSION['addr_change']);
                $status = 'success';
                $message = '住所変更の申請を受け付けました。審査結果をお待ちください。';
            } else {
                $status = 'error';
                if ((int)$res['status'] === 401) {
                    $message = 'ログインの有効期限が切れた可能性があります。再ログインしてください。（コード: 401）';
                } else {
                    $message = '住所変更の申請に失敗しました。（コード: ' . (int)$res['status'] . '）';
                }
            }
        } catch (Exception $e) {
            $status = 'error';
            $message = '通信エラー: ' . $e->getMessage();
        }
    }
}

$pageTitle = '住所変更結果';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="col-md-8 mx-auto">
        <?php if ($status === 'success'): ?>
            <div class="alert alert-success"><?= h($message) ?></div>
        <?php else: ?>
            <div class="alert alert-danger"><?= h($message) ?></div>
        <?php endif; ?>

        <div class="text-center mt-4">
            <a href="dashboard.php" class="btn btn-primary btn-lg">ダッシュボードへ戻る</a>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
