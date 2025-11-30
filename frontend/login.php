<?php
require_once 'config.php';

$error = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'] ?? '';
    $password = $_POST['password'] ?? '';

    try {
        $res = api_request('POST', '/api/auth/login', [
            'username' => $username,
            'password' => $password,
        ]);

        if ($res['status'] === 200 && isset($res['body']['token'])) {
            $_SESSION['jwt_token'] = $res['body']['token'];
            $_SESSION['username']  = $username;
            header('Location: dashboard.php');
            exit;
        } else {
            $error = 'ログインに失敗しました。ユーザ名/パスワードを確認してください。';
        }
    } catch (Exception $e) {
        $error = '通信エラー: ' . $e->getMessage();
    }
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>+Acts Bank ログイン</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card shadow-sm" style="max-width: 420px; width: 100%;">
        <div class="card-header text-center bg-primary text-white">
            <h4 class="my-2">+Acts Bank ログイン</h4>
        </div>
        <div class="card-body">
            <?php if ($error): ?>
                <div class="alert alert-danger" role="alert">
                    <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>
            <form method="post" class="mt-2">
                <div class="mb-3">
                    <label class="form-label">ユーザ名</label>
                    <input type="text" name="username" class="form-control" required autocomplete="username">
                </div>
                <div class="mb-3">
                    <label class="form-label">パスワード</label>
                    <input type="password" name="password" class="form-control" required autocomplete="current-password">
                </div>
                <button type="submit" class="btn btn-primary w-100">ログイン</button>
            </form>
        </div>
        <div class="card-footer text-center text-muted small">
            初めての方は
            <a href="register.php">口座開設</a>
        </div>

    </div>
</div>

</body>
</html>
