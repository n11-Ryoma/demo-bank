<?php
// register.php
require_once 'config.php';

$error   = null;
$success = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username        = $_POST['username'] ?? '';
    $password        = $_POST['password'] ?? '';
    $passwordConfirm = $_POST['password_confirm'] ?? '';
    $email           = $_POST['email'] ?? '';

    $nameKanji   = $_POST['name_kanji'] ?? '';
    $nameKana    = $_POST['name_kana'] ?? '';
    $birthDate   = $_POST['birth_date'] ?? '';
    $gender      = $_POST['gender'] ?? '';
    $phone       = $_POST['phone'] ?? '';
    $postalCode  = $_POST['postal_code'] ?? '';
    $address     = $_POST['address'] ?? '';
    $myNumber    = $_POST['my_number'] ?? '';

    if ($password !== $passwordConfirm) {
        $error = 'パスワードが一致しません。';
    } elseif ($username === '' || $password === '' || $email === '' || $nameKanji === '' || $nameKana === '' || $birthDate === '' || $gender === '' || $phone === '' || $postalCode === '' || $address === '') {
        $error = '必須項目を入力してください。';
    } else {
        try {
            // Spring Boot 側の /api/onboarding/open-account を呼ぶ
            $res = api_request('POST', '/api/onboarding/open-account', [
                'username'   => $username,
                'password'   => $password,
                'email'      => $email,
                'nameKanji'  => $nameKanji,
                'nameKana'   => $nameKana,
                'birthDate'  => $birthDate,
                'gender'     => $gender,
                'phone'      => $phone,
                'postalCode' => $postalCode,
                'address'    => $address,
                'myNumber'   => $myNumber,
            ]);

            if ($res['status'] === 200) {
                $_SESSION['flash_message'] = '口座開設が完了しました。ログインしてください。';
                header('Location: login.php');
                exit;
            } else {
                $error = '口座開設に失敗しました。入力内容をご確認ください。';
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
    <title>eBank 口座開設</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card shadow-sm" style="max-width: 640px; width: 100%;">
        <div class="card-header text-center bg-primary text-white">
            <h4 class="my-2">eBank 口座開設</h4>
        </div>
        <div class="card-body">
            <?php if ($error): ?>
                <div class="alert alert-danger" role="alert">
                    <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>
            <form method="post" class="mt-2">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">ユーザ名</label>
                        <input type="text" name="username" class="form-control" required autocomplete="username">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">メール</label>
                        <input type="email" name="email" class="form-control" required autocomplete="email">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">パスワード</label>
                        <input type="password" name="password" class="form-control" required autocomplete="new-password">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">パスワード（確認）</label>
                        <input type="password" name="password_confirm" class="form-control" required autocomplete="new-password">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">氏名（漢字）</label>
                        <input type="text" name="name_kanji" class="form-control" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">氏名（カナ）</label>
                        <input type="text" name="name_kana" class="form-control" required>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label class="form-label">生年月日</label>
                        <input type="date" name="birth_date" class="form-control" required>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">性別</label>
                        <select name="gender" class="form-select" required>
                            <option value="">選択してください</option>
                            <option value="M">男性</option>
                            <option value="F">女性</option>
                            <option value="O">その他</option>
                        </select>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">電話番号</label>
                        <input type="text" name="phone" class="form-control" required>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label class="form-label">郵便番号</label>
                        <input type="text" name="postal_code" class="form-control" required>
                    </div>
                    <div class="col-md-8 mb-3">
                        <label class="form-label">住所</label>
                        <input type="text" name="address" class="form-control" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">マイナンバー（任意）</label>
                    <input type="text" name="my_number" class="form-control">
                </div>
                <button type="submit" class="btn btn-primary w-100">口座開設する</button>
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
