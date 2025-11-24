<?php
session_start();

$apiBaseUrl = "http://192.168.1.59:8080";

$error = null;
$balanceResult = null;   
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $username = $_POST['username'] ?? '';
    $password = $_POST['password'] ?? '';
    $accountNumber = $_POST['accountNumber'] ?? '';

    // ログイン API
    $loginUrl = $apiBaseUrl . '/api/auth/login';
    $loginPayload = json_encode([
        'username' => $username,
        'password' => $password
    ]);

    $ch = curl_init($loginUrl);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json'
    ]);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $loginPayload);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $loginResponse = curl_exec($ch);
    $loginHttpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($loginResponse === false) {
        $error = "ログインAPIと通信できませんでした。";
    } else {
        $loginData = json_decode($loginResponse, true);

        if ($loginHttpCode === 200 && isset($loginData['token'])) {
            // ★ セッションに保存
            $_SESSION['token'] = $loginData['token'];
            $_SESSION['accountNumber'] = $accountNumber;

            // ★ 別ページに移動
            header('Location: home.php');
            exit;
        } else {
            $error = "ログイン失敗: ユーザまたはパスワードが不正。";
        }
    }
}
?>
<!-- ここから先は login.php の HTML 部分はそのままでいい -->

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>EBank ログイン</title>
    <style>
        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            background: #f5f7fb;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
            padding-top: 40px;
        }
        .container {
            background: #fff;
            padding: 24px 28px;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            width: 380px;
        }
        h1 {
            margin-top: 0;
            font-size: 20px;
            margin-bottom: 8px;
        }
        .subtitle {
            margin: 0 0 20px;
            color: #666;
            font-size: 13px;
        }
        label {
            display: block;
            margin-bottom: 4px;
            font-size: 13px;
        }
        input[type="text"],
        input[type="password"] {
            width: 100%;
            padding: 8px 10px;
            margin-bottom: 14px;
            border-radius: 6px;
            border: 1px solid #ccc;
            box-sizing: border-box;
            font-size: 13px;
        }
        button {
            width: 100%;
            padding: 9px;
            border-radius: 6px;
            border: none;
            background: #1976d2;
            color: #fff;
            font-size: 14px;
            cursor: pointer;
        }
        button:hover {
            background: #145ca5;
        }
        .error {
            margin-bottom: 14px;
            padding: 8px;
            border-radius: 6px;
            background: #ffebee;
            color: #c62828;
            font-size: 13px;
        }
        .balance-card {
            margin-top: 18px;
            padding: 12px 14px;
            border-radius: 8px;
            background: #e3f2fd;
            font-size: 14px;
        }
        .balance-card .label {
            color: #555;
            font-size: 12px;
        }
        .balance-card .value {
            font-size: 18px;
            font-weight: bold;
            margin-top: 4px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>EBank ログイン</h1>
    <p class="subtitle">ログインして口座残高を確認します。</p>

    <?php if ($error): ?>
        <div class="error"><?php echo htmlspecialchars($error, ENT_QUOTES, 'UTF-8'); ?></div>
    <?php endif; ?>

    <form method="post" action="login.php">
        <label for="username">ユーザー名</label>
        <input type="text" id="username" name="username"
               value="<?php echo isset($username) ? htmlspecialchars($username, ENT_QUOTES, 'UTF-8') : ''; ?>"
               required>

        <label for="password">パスワード</label>
        <input type="password" id="password" name="password" required>

        <button type="submit">ログイン</button>
    </form>
</div>
</body>
</html>
