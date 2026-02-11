<?php
require_once 'config.php';

function h($value)
{
    return htmlspecialchars((string)$value, ENT_QUOTES, 'UTF-8');
}

$error = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username        = trim($_POST['username'] ?? '');
    $password        = $_POST['password'] ?? '';
    $passwordConfirm = $_POST['password_confirm'] ?? '';
    $email           = trim($_POST['email'] ?? '');

    $nameKanji   = trim($_POST['name_kanji'] ?? '');
    $nameKana    = trim($_POST['name_kana'] ?? '');
    $birthDate   = trim($_POST['birth_date'] ?? '');
    $gender      = trim($_POST['gender'] ?? '');
    $phone       = trim($_POST['phone'] ?? '');
    $postalCode  = trim($_POST['postal_code'] ?? '');
    $address     = trim($_POST['address'] ?? '');
    $myNumber    = trim($_POST['my_number'] ?? '');

    if ($password !== $passwordConfirm) {
        $error = 'パスワードが一致しません。';
    } elseif (
        $username === '' || $password === '' || $email === '' ||
        $nameKanji === '' || $nameKana === '' || $birthDate === '' ||
        $gender === '' || $phone === '' || $postalCode === '' || $address === ''
    ) {
        $error = '必須項目を入力してください。';
    } else {
        try {
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

            if (($res['status'] ?? 0) === 200) {
                $_SESSION['flash_message'] = '口座開設が完了しました。ログインしてください。';
                header('Location: login.php');
                exit;
            }

            if (is_array($res['body'] ?? null)) {
                $error = $res['body']['message'] ?? $res['body']['error'] ?? '口座開設に失敗しました。入力内容をご確認ください。';
            } elseif (is_string($res['raw'] ?? null) && trim($res['raw']) !== '') {
                $error = trim($res['raw']);
            } else {
                $error = '口座開設に失敗しました。入力内容をご確認ください。';
            }
        } catch (Exception $e) {
            $error = '通信エラー: ' . $e->getMessage();
        }
    }
}

$pageTitle = '口座開設';
$extraStyles = <<<'CSS'
<style>
    .register-wrap {
        min-height: calc(100vh - 160px);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .register-card {
        width: 100%;
        max-width: 900px;
        border: 0;
        box-shadow: 0 16px 28px rgba(18, 50, 74, .12);
        border-radius: 14px;
    }
</style>
CSS;

require_once 'partials/header.php';
?>
<main class="container register-wrap py-4">
    <div class="card register-card">
        <div class="card-body p-4 p-md-5">
            <h1 class="h4 fw-bold mb-2">+Acts Bank 口座開設</h1>
            <p class="text-muted small mb-4">必要事項をご入力ください。入力内容は暗号化通信で送信されます。</p>

            <?php if ($error): ?>
                <div class="alert alert-danger"><?= h($error) ?></div>
            <?php endif; ?>

            <form method="post">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">ユーザー名</label>
                        <input type="text" name="username" class="form-control" required autocomplete="username" value="<?= h($_POST['username'] ?? '') ?>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">メール</label>
                        <input type="email" name="email" class="form-control" required autocomplete="email" value="<?= h($_POST['email'] ?? '') ?>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">パスワード</label>
                        <input type="password" name="password" class="form-control" required autocomplete="new-password">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">パスワード（確認）</label>
                        <input type="password" name="password_confirm" class="form-control" required autocomplete="new-password">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">氏名（漢字）</label>
                        <input type="text" name="name_kanji" class="form-control" required value="<?= h($_POST['name_kanji'] ?? '') ?>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">氏名（カナ）</label>
                        <input type="text" name="name_kana" class="form-control" required value="<?= h($_POST['name_kana'] ?? '') ?>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">生年月日</label>
                        <input type="date" name="birth_date" class="form-control" required value="<?= h($_POST['birth_date'] ?? '') ?>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">性別</label>
                        <select name="gender" class="form-select" required>
                            <option value="">選択してください</option>
                            <option value="M" <?= (($_POST['gender'] ?? '') === 'M') ? 'selected' : '' ?>>男性</option>
                            <option value="F" <?= (($_POST['gender'] ?? '') === 'F') ? 'selected' : '' ?>>女性</option>
                            <option value="O" <?= (($_POST['gender'] ?? '') === 'O') ? 'selected' : '' ?>>その他</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">電話番号</label>
                        <input type="text" name="phone" class="form-control" required value="<?= h($_POST['phone'] ?? '') ?>">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">郵便番号</label>
                        <input type="text" name="postal_code" class="form-control" required value="<?= h($_POST['postal_code'] ?? '') ?>">
                    </div>
                    <div class="col-md-8">
                        <label class="form-label">住所</label>
                        <input type="text" name="address" class="form-control" required value="<?= h($_POST['address'] ?? '') ?>">
                    </div>
                    <div class="col-12">
                        <label class="form-label">マイナンバー（任意）</label>
                        <input type="text" name="my_number" class="form-control" value="<?= h($_POST['my_number'] ?? '') ?>">
                    </div>
                </div>

                <div class="d-grid mt-4">
                    <button type="submit" class="btn btn-primary">口座開設する</button>
                </div>
            </form>

            <div class="small text-muted mt-3">
                既にアカウントをお持ちの方は <a href="login.php">ログイン</a>
            </div>
        </div>
    </div>
</main>
<?php require_once 'partials/footer.php'; ?>
