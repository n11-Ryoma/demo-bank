<?php
require_once 'config.php';

function h($value)
{
    return htmlspecialchars((string)$value, ENT_QUOTES, 'UTF-8');
}

$error = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = trim($_POST['username'] ?? '');
    $password = trim($_POST['password'] ?? '');

    try {
        $res = api_request('POST', '/api/auth/login', [
            'username' => $username,
            'password' => $password,
        ]);

        if (($res['status'] ?? 0) === 200 && is_array($res['body']) && isset($res['body']['token'])) {
            $_SESSION['jwt_token'] = $res['body']['token'];
            $_SESSION['username'] = $username;
            header('Location: dashboard.php');
            exit;
        }

        if (is_string($res['raw'] ?? null) && trim($res['raw']) !== '') {
            $error = trim($res['raw']);
        } else {
            $error = 'ログインに失敗しました。ユーザー名とパスワードを確認してください。';
        }
    } catch (Exception $e) {
        $error = '通信エラー: ' . $e->getMessage();
    }
}

$pageTitle = 'ログイン';
$extraStyles = <<<'CSS'
<style>
    .login-wrap {
        min-height: calc(100vh - 160px);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .login-card {
        width: 100%;
        max-width: 420px;
        border: 0;
        box-shadow: 0 16px 28px rgba(18, 50, 74, .12);
        border-radius: 14px;
    }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container login-wrap">
    <div class="card login-card">
        <div class="card-body p-4 p-md-5">
            <h1 class="h4 fw-bold mb-3">インターネットバンキング<br> ログイン</h1>

            <?php if ($error): ?>
                <div class="alert alert-danger"><?= h($error) ?></div>
            <?php endif; ?>

            <form method="post">
                <div class="mb-3">
                    <label class="form-label">ユーザー名</label>
                    <input type="text" name="username" class="form-control" required autocomplete="username">
                </div>
                <div class="mb-4">
                    <label class="form-label">パスワード</label>
                    <input type="password" name="password" class="form-control" required autocomplete="current-password">
                </div>
                <button type="submit" class="btn btn-primary w-100">ログイン</button>
            </form>

            <div class="small text-muted mt-3">
                初めての方は <a href="register.php">口座開設</a>
            </div>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
