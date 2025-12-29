<?php
// address_change.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$username = $_SESSION['username'] ?? 'guest';

$error = null;

// 本当は API から現在住所を取得するのが自然だけど、まだエンドポイントがない想定なのでダミー
// ---- 現在住所を API から取得 ----
$currentAddress = '住所情報が登録されていません。';
try {
    $res = api_request('GET', '/api/address-change/current', null, true);

    if ($res['status'] === 200 && is_array($res['body'])) {
        $addrBody = $res['body'];

        $postal   = $addrBody['postalCode']   ?? '';
        $pref     = $addrBody['prefecture']   ?? '';
        $city     = $addrBody['city']         ?? '';
        $line1    = $addrBody['addressLine1'] ?? '';
        $line2    = $addrBody['addressLine2'] ?? '';

        // 好きなフォーマットで整形
        $currentAddress = trim(sprintf(
            '〒%s %s%s %s %s',
            $postal,
            $pref,
            $city,
            $line1,
            $line2
        ));
    } elseif ($res['status'] === 404) {
        $currentAddress = '現在登録されている住所はありません。';
    } else {
        $currentAddress = '現在の住所情報の取得に失敗しました。';
    }
} catch (Exception $e) {
    $currentAddress = '現在の住所情報の取得中にエラーが発生しました。';
}


// 一時アップロード用ディレクトリ（後で Java からも読む想定）
$tmpBaseDir = __DIR__ . '/uploads/tmp/address';
if (!is_dir($tmpBaseDir)) {
    mkdir($tmpBaseDir, 0775, true);
}

// ---- POST：入力チェック & 一時保存 & 確認画面へリダイレクト ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $newPostal   = $_POST['postal']   ?? '';
    $newPref     = $_POST['pref']     ?? '';
    $newCity     = $_POST['city']     ?? '';
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

        // ★ CTF 用に php も許可（実務だったら絶対にNG）
        $allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'zip', 'php'];
        if (!in_array($ext, $allowedExts, true)) {
            $error = '対応していないファイル形式です。(jpg, png, gif, pdf, zip)';
        } else {
            // ランダムファイル名で一時保存
            $tmpName = bin2hex(random_bytes(16)) . '.' . $ext;
            $fullPath = $tmpBaseDir . '/' . $tmpName;

            if (!move_uploaded_file($file['tmp_name'], $fullPath)) {
                $error = '書類ファイルの保存に失敗しました。';
            } else {
                // Java 側に渡す用の相対パス（/var/www/html/uploads/ をベースにする想定）
                // 例: tmp/address/xxxxxx.png
                $relativePath = 'tmp/address/' . $tmpName;

                // 確認画面・commit で使う情報をセッションに詰める
                $_SESSION['addr_change'] = [
                    'postal'      => $newPostal,
                    'pref'        => $newPref,
                    'city'        => $newCity,
                    'address1'    => $newAddress1,
                    'address2'    => $newAddress2,
                    'file_path'   => $relativePath,
                    'file_name'   => $originalName,
                ];

                // 確認画面へ
                header('Location: address_change_confirm.php');
                exit;
            }
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>住所変更 - eBank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-0">
    <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center" href="dashboard.php">
            <img src="images/logo.png" alt="eBank Logo"
                 style="height:100px; margin-right:8px;">
            <span class="fw-bold">+Acts Bank</span>
        </a>
        <div class="d-flex">
            <span class="navbar-text me-3 text-white">
                <?= htmlspecialchars($username, ENT_QUOTES, 'UTF-8') ?> さん
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
                    <a class="nav-link" href="dashboard.php">
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
                <li class="nav-item">
                    <a class="nav-link active fw-bold" href="address_change.php">
                        住所変更
                    </a>
                </li>
            </ul>
        </nav>

        <!-- メイン領域 -->
        <main class="col-md-9 col-lg-10 p-4">
            <h2 class="mb-3">住所変更</h2>

            <?php if ($error): ?>
                <div class="alert alert-danger">
                    <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>

            <div class="row g-4">
                <!-- 左：住所変更フォーム -->
                <div class="col-lg-7">
                    <div class="card shadow-sm mb-3">
                        <div class="card-header bg-white">
                            現在のご住所
                        </div>
                        <div class="card-body">
                            <p class="mb-0">
                                <?= htmlspecialchars($currentAddress, ENT_QUOTES, 'UTF-8') ?>
                            </p>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-header bg-white">
                            新しいご住所
                        </div>
                        <div class="card-body">
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
                                    <label class="form-label">番地・建物名</label>
                                    <input type="text" name="address1" class="form-control" placeholder="丸の内1-1-1 〇〇ビル 10F" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">部屋番号・その他（任意）</label>
                                    <input type="text" name="address2" class="form-control" placeholder="1001号室など">
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">住所確認書類（必須）</label>
                                    <input type="file" name="proof_file" class="form-control" required>
                                    <div class="form-text">
                                        公共料金の領収書、住民票、請求書などの画像（JPG / PNG / GIF）または PDF / ZIP をアップロードしてください。
                                    </div>
                                </div>

                                <button type="submit" class="btn btn-primary">
                                    確認画面へ進む
                                </button>
                                <a href="dashboard.php" class="btn btn-secondary ms-2">
                                    ダッシュボードに戻る
                                </a>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- 右：備考だけ（最終書類は別システム想定） -->
                <div class="col-lg-5">
                    <div class="card shadow-sm">
                        <div class="card-header bg-white">
                            ご案内
                        </div>
                        <div class="card-body">
                            <p class="text-muted small mb-2">
                                この画面では、住所と確認書類の入力のみを行います。
                            </p>
                            <p class="text-muted small mb-0">
                                次の確認画面で内容を確認のうえ「確定」すると、住所変更申請がシステムに登録されます。
                                （デモ環境では審査フローなどは省略されています）
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

</body>
</html>
