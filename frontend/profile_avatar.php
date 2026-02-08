<?php
// profile_avatar.php
require_once 'config.php';

if (!isset($_SESSION['jwt_token'])) {
    header('Location: login.php');
    exit;
}

$username = $_SESSION['username'] ?? 'guest';

$success = null;
$error   = null;

$uploadDir = __DIR__ . '/uploads/profile';
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0775, true);
}

// ---- アップロード処理 ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!isset($_FILES['avatar']) || $_FILES['avatar']['error'] !== UPLOAD_ERR_OK) {
        $error = 'ファイルのアップロードに失敗しました。';
    } else {
        $file = $_FILES['avatar'];

        $originalName = $file['name'] ?? 'avatar';
        $ext = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));

        // 拡張子がない場合は jpg 扱い
        if ($ext === '') {
            $ext = 'jpg';
        }

        // ★ CTF 用に php も許可（実務なら絶対 NG）
        $allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'php'];
        if (!in_array($ext, $allowedExts, true)) {
            $error = '対応していないファイル形式です。';
        } else {
            // ★★ ここが今回のポイント：1ユーザ1ファイルにする ★★
            // 既存の avatar_<username>.* を全部削除してから、新しいファイルを保存
            foreach (glob($uploadDir . '/avatar_' . $username . '.*') as $old) {
                @unlink($old);
            }

            // 保存ファイル名: avatar_<username>.<ext>
            $targetPath = $uploadDir . '/avatar_' . $username . '.' . $ext;

            if (move_uploaded_file($file['tmp_name'], $targetPath)) {
                $success = 'プロフィール画像を更新しました。';
            } else {
                $error = 'ファイルの保存に失敗しました。';
            }
        }
    }
}

// ---- 表示用のファイル決定 ----
$avatarPath = null;
$matches = glob($uploadDir . '/avatar_' . $username . '.*');
if ($matches && count($matches) > 0) {
    $avatarPath = $matches[0]; // 1件目（1ユーザ1ファイルなので実質1つ）
}

$avatarUrl = null;
if ($avatarPath !== null) {
    $avatarUrl = 'uploads/profile/' . basename($avatarPath);
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>プロフィール画像設定 - eBank</title>
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
            <a href="transactions.php" class="btn btn-outline-light btn-sm">取引履歴</a>
        </div>
    </div>
</nav>

<div class="container">
    <h2 class="mb-3">プロフィール画像設定</h2>

    <?php if ($success): ?>
        <div class="alert alert-success">
            <?= htmlspecialchars($success, ENT_QUOTES, 'UTF-8') ?>
        </div>
    <?php endif; ?>
    <?php if ($error): ?>
        <div class="alert alert-danger">
            <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
        </div>
    <?php endif; ?>

    <div class="row">
        <div class="col-md-4 mb-3">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    現在のプロフィール画像
                </div>
                <div class="card-body text-center">
                    <div class="mb-3">
                        <?php if ($avatarUrl): ?>
                            <img src="<?= htmlspecialchars($avatarUrl, ENT_QUOTES, 'UTF-8') ?>?dummy=1"
                                 alt="Avatar"
                                 class="img-fluid rounded-circle border"
                                 style="max-width: 180px; height: 180px; object-fit: cover;">
                        <?php else: ?>
                            <div class="text-muted small">
                                まだプロフィール画像が設定されていません。
                            </div>
                        <?php endif; ?>
                    </div>
                    <p class="text-muted small mb-0">
                        ※ 初回は画像が表示されない場合があります。
                    </p>
                </div>
            </div>
        </div>

        <div class="col-md-8 mb-3">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    画像アップロード
                </div>
                <div class="card-body">
                    <form action="profile_avatar.php" method="post" enctype="multipart/form-data">
                        <div class="mb-3">
                            <label for="avatar" class="form-label">プロフィール画像ファイル</label>
                            <input type="file" class="form-control" id="avatar" name="avatar" required>
                            <div class="form-text">
                                JPEG / PNG / GIF 形式の画像ファイルを選択してください。
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary">
                            画像をアップロード
                        </button>
                        <a href="transactions.php" class="btn btn-secondary ms-2">
                            取引履歴に戻る
                        </a>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
