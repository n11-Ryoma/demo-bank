<?php
// register.php
require_once 'config.php';

$error   = null;
$success = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username        = $_POST['username']        ?? '';
    $password        = $_POST['password']        ?? '';
    $passwordConfirm = $_POST['password_confirm'] ?? '';

    if ($password !== $passwordConfirm) {
        $error = 'パスワードが一致しません。';
    } else {
        try {
            // Spring Boot 側の /api/auth/register を想定
            $res = api_request('POST', '/api/auth/register', [
                'username' => $username,
                'password' => $password,
            ]);

            if ($res['status'] === 200) {
                // 成功したらログイン画面へ
                $_SESSION['flash_message'] = 'ユーザ登録が完了しました。ログインしてください。';
                header('Location: login.php');
                exit;
            } else {
                $error = 'ユーザ登録に失敗しました。既に使われているユーザ名の可能性があります。';
            }
        } catch (Exception $e) {
            $error = '通信エラー: ' . $e->getMessage();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>eBank 新規登録</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card shadow-sm" style="max-width: 420px; width: 100%;">
        <div class="card-header text-center bg-primary text-white">
            <h4 class="my-2">eBank 新規登録</h4>
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
                    <input type="password" name="password" class="form-control" required autocomplete="new-password">
                </div>
                <div class="mb-3">
                    <label class="form-label">パスワード（確認）</label>
                    <input type="password" name="password_confirm" class="form-control" required autocomplete="new-password">
                </div>
                <button type="submit" class="btn btn-primary w-100">登録する</button>
            </form>
        </div>
        <div class="card-footer text-center text-muted small">
            既にアカウントをお持ちの方は
            <a href="login.php">ログイン</a>
        </div>
    </div>
</div>

</body>
</html>
