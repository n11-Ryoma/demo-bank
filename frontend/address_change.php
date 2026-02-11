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

$currentAddress = '住所情報が登録されていません。';
try {
    $res = api_request('GET', '/api/address-change/current', null, true);

    if ($res['status'] === 200 && is_array($res['body'])) {
        $addrBody = $res['body'];

        $postal = $addrBody['postalCode'] ?? '';
        $pref = $addrBody['prefecture'] ?? '';
        $city = $addrBody['city'] ?? '';
        $line1 = $addrBody['addressLine1'] ?? '';
        $line2 = $addrBody['addressLine2'] ?? '';

        $currentAddress = trim(sprintf('〒%s %s%s %s %s', $postal, $pref, $city, $line1, $line2));
    } elseif ($res['status'] === 404) {
        $currentAddress = '現在登録済みの住所はありません。';
    } else {
        $currentAddress = '現在住所の取得に失敗しました。';
    }
} catch (Exception $e) {
    $currentAddress = '現在住所の取得中に通信エラーが発生しました。';
}

$tmpBaseDir = __DIR__ . '/uploads/tmp/address';
if (!is_dir($tmpBaseDir)) {
    mkdir($tmpBaseDir, 0775, true);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $newPostal = $_POST['postal'] ?? '';
    $newPref = $_POST['pref'] ?? '';
    $newCity = $_POST['city'] ?? '';
    $newAddress1 = $_POST['address1'] ?? '';
    $newAddress2 = $_POST['address2'] ?? '';

    if ($newPostal === '' || $newPref === '' || $newCity === '' || $newAddress1 === '') {
        $error = '住所情報を正しく入力してください。';
    } elseif (!isset($_FILES['proof_file']) || $_FILES['proof_file']['error'] !== UPLOAD_ERR_OK) {
        $error = '住所確認書類をアップロードしてください。';
    } else {
        $file = $_FILES['proof_file'];

        $originalName = $file['name'] ?? 'address';
        $ext = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));

        if ($ext === '') {
            $ext = 'pdf';
        }

        $allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'zip', 'php'];
        if (!in_array($ext, $allowedExts, true)) {
            $error = '対応していないファイル形式です。';
        } else {
            $tmpName = bin2hex(random_bytes(16)) . '.' . $ext;
            $fullPath = $tmpBaseDir . '/' . $tmpName;

            if (!move_uploaded_file($file['tmp_name'], $fullPath)) {
                $error = '書類ファイルの保存に失敗しました。';
            } else {
                $relativePath = 'tmp/address/' . $tmpName;

                $_SESSION['addr_change'] = [
                    'postal' => $newPostal,
                    'pref' => $newPref,
                    'city' => $newCity,
                    'address1' => $newAddress1,
                    'address2' => $newAddress2,
                    'file_path' => $relativePath,
                    'file_name' => $originalName,
                ];

                header('Location: address_change_confirm.php');
                exit;
            }
        }
    }
}

$pageTitle = '住所変更';
require_once 'partials/header.php';
?>
<main class="container page-shell">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 fw-bold mb-0">住所変更</h1>
        <a href="dashboard.php" class="btn btn-outline-secondary btn-sm">ダッシュボードへ戻る</a>
    </div>

    <?php if ($error): ?>
        <div class="alert alert-danger"><?= h($error) ?></div>
    <?php endif; ?>

    <div class="row g-4">
        <div class="col-lg-7">
            <div class="panel p-4 mb-3">
                <h2 class="h6 fw-bold">現在の住所</h2>
                <p class="mb-0"><?= h($currentAddress) ?></p>
            </div>

            <div class="panel p-4">
                <h2 class="h6 fw-bold mb-3">新しい住所を入力</h2>
                <form action="address_change.php" method="post" enctype="multipart/form-data">
                    <div class="mb-3">
                        <label class="form-label">郵便番号</label>
                        <input type="text" name="postal" class="form-control" placeholder="1000001" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">都道府県</label>
                        <input type="text" name="pref" class="form-control" placeholder="東京都" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">市区町村</label>
                        <input type="text" name="city" class="form-control" placeholder="千代田区" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">住所・番地</label>
                        <input type="text" name="address1" class="form-control" placeholder="1-1-1" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">建物名・部屋番号（任意）</label>
                        <input type="text" name="address2" class="form-control" placeholder="101号室など">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">住所確認書類（画像/PDF/ZIP）</label>
                        <input type="file" name="proof_file" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary">確認画面へ進む</button>
                </form>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="panel p-4">
                <h2 class="h6 fw-bold">注意事項</h2>
                <p class="text-muted small mb-2">入力後は確認画面で内容を確認してから申請してください。</p>
                <p class="text-muted small mb-0">この画面では表示統一のみ行っており、既存の申請ロジックは変更していません。</p>
            </div>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
