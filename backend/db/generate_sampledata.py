from faker import Faker
import csv
import random
import secrets
import string
from datetime import datetime, timedelta, timezone

fake = Faker("ja_JP")

# ===== 設定 =====
NUM_USERS = 10000               # ユーザー数（admin含む）
TX_PER_USER_MIN = 5            # 1口座あたり取引件数（最小）
TX_PER_USER_MAX = 30           # 1口座あたり取引件数（最大）
ADDR_REQ_RATE = 0.15           # 住所変更申請を出す割合（0.0～1.0）
TRANSFER_RATE = 0.15           # 取引のうち振込系を混ぜる割合

BRANCH_MIN = 1000
BRANCH_MAX = 9999

START_BAL_MIN = 10_000
START_BAL_MAX = 1_000_000

TX_AMOUNT_MIN = 100
TX_AMOUNT_MAX = 200_000

# 文字列の長さ制約に合わせる
TX_TYPE_MAXLEN = 16
BRANCH_CODE_MAXLEN = 8
ACCOUNT_NUMBER_MAXLEN = 16

# ===== 共通関数 =====
def random_password(length=12):
    alphabet = string.ascii_letters + string.digits + "!@#$%^&*"
    return ''.join(secrets.choice(alphabet) for _ in range(length))

def iso(dt: datetime) -> str:
    return dt.astimezone(timezone.utc).isoformat()

def rand_dt_within_years(years=5) -> datetime:
    now = datetime.now(timezone.utc)
    start = now - timedelta(days=365 * years)
    # ランダム秒でバラす
    delta_sec = int((now - start).total_seconds())
    return start + timedelta(seconds=random.randint(0, delta_sec))

def clamp_len(s: str, maxlen: int) -> str:
    if s is None:
        return ""
    return s[:maxlen]

# ===== 重複回避（username/email）=====
used_usernames = set()
used_emails = set()

def unique_username():
    while True:
        u = fake.user_name()
        if u not in used_usernames:
            used_usernames.add(u)
            return u

def unique_email():
    while True:
        e = fake.email()
        if e not in used_emails:
            used_emails.add(e)
            return e

# ===== 生成データ格納 =====
users_rows = []
profiles_rows = []
accounts_rows = []            # id, user_id, branch_code, account_number, balance, opened_at
transactions_rows = []        # account_id, type, amount, balance_after, description, related_branch_code, related_account_number, created_at
addrreq_rows = []             # user_id, postal_code, prefecture, city, address_line1, address_line2, proof_file_path, proof_file_data, status, created_at

# 口座情報をメモ（振込先選択用）
account_info = {}  # user_id -> (branch_code, account_number, opened_at)

# ===== 1) users / user_profile / accounts（ただしbalanceはあとで上書き）=====
for user_id in range(1, NUM_USERS + 1):
    if user_id == 1:
        username = "admin"
        password = "Admin@123"
        email = "admin@example.com"
    else:
        username = unique_username()
        password = random_password()
        email = unique_email()

    created_at = rand_dt_within_years(3)
    users_rows.append([user_id, username, password, email, iso(created_at)])

    name_kanji = fake.name()
    name_kana = fake.kana_name()
    birth_date = fake.date_of_birth(minimum_age=20, maximum_age=75).isoformat()
    gender = random.choice(["M", "F"])
    phone = fake.phone_number()
    postal = fake.postcode()
    address = fake.address().replace("\n", " ")
    my_number = f"{random.randint(1000,9999)}-{random.randint(1000,9999)}-{random.randint(1000,9999)}"

    profiles_rows.append([user_id, name_kanji, name_kana, birth_date, gender, phone, postal, address, my_number])

    # 口座（1ユーザー1口座）
    # accounts.id は transactions の FK 参照先なので、ここでは id = user_id にして安定させる
    account_id = user_id
    branch_code = str(random.randint(BRANCH_MIN, BRANCH_MAX))
    account_number = str(random.randint(1000000, 9999999))
    opened_at = rand_dt_within_years(3)

    # 初期残高（後でtransactions生成結果の最終残高で上書きする）
    start_balance = random.randint(START_BAL_MIN, START_BAL_MAX)

    accounts_rows.append([account_id, user_id, clamp_len(branch_code, BRANCH_CODE_MAXLEN),
                          clamp_len(account_number, ACCOUNT_NUMBER_MAXLEN),
                          start_balance, iso(opened_at)])

    account_info[user_id] = (branch_code, account_number, opened_at)

# ===== 2) transactions（残高が矛盾しないように時系列で作る）=====
# 最終残高をここに確定させる
final_balances = {row[0]: row[4] for row in accounts_rows}  # account_id -> current balance（開始）

tx_descriptions = [
    "ATM出金", "ATM入金", "振込", "振込受取", "給与", "家賃", "光熱費", "食費", "EC決済", "交通費"
]

# 振込先選択用に user_id リスト
user_ids = list(range(1, NUM_USERS + 1))

for user_id in user_ids:
    account_id = user_id
    balance = final_balances[account_id]

    tx_count = random.randint(TX_PER_USER_MIN, TX_PER_USER_MAX)

    # opened_at 以降の時刻で作る（口座開設前に取引が起きないように）
    opened_at = account_info[user_id][2]
    base_time = max(opened_at, datetime.now(timezone.utc) - timedelta(days=365*3))

    # 時系列を作る（昇順で作って最後に挿入順はそのままでもOK）
    times = [base_time + timedelta(minutes=random.randint(1, 60*24*365)) for _ in range(tx_count)]
    times.sort()

    for t in times:
        is_transfer = random.random() < TRANSFER_RATE

        if is_transfer and NUM_USERS >= 2:
            # 振込：TRANSFER_OUT（送金） or TRANSFER_IN（受取）を混ぜる
            # ※デモなので簡易的に「同一テーブルに両者を記録」する
            # 実装に合わせて片側だけにしたいならここを単純化できる
            if random.random() < 0.5:
                # TRANSFER_OUT（送金）
                # 送金できる程度の残高を確保
                if balance <= TX_AMOUNT_MIN:
                    # 残高不足なら入金にフォールバック
                    tx_type = "DEPOSIT"
                    amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                    balance += amount
                    related_branch = ""
                    related_accnum = ""
                    desc = "ATM入金"
                else:
                    tx_type = "TRANSFER_OUT"
                    amount = random.randint(TX_AMOUNT_MIN, min(TX_AMOUNT_MAX, balance))
                    # 送金先は自分以外
                    to_user = random.choice(user_ids)
                    while to_user == user_id:
                        to_user = random.choice(user_ids)
                    rb, ra, _ = account_info[to_user]
                    related_branch = clamp_len(rb, BRANCH_CODE_MAXLEN)
                    related_accnum = clamp_len(ra, ACCOUNT_NUMBER_MAXLEN)
                    desc = "振込"
                    balance -= amount
            else:
                # TRANSFER_IN（受取）＝入金扱い
                tx_type = "TRANSFER_IN"
                amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                # 相手情報は適当に1つ
                from_user = random.choice(user_ids)
                while from_user == user_id:
                    from_user = random.choice(user_ids)
                rb, ra, _ = account_info[from_user]
                related_branch = clamp_len(rb, BRANCH_CODE_MAXLEN)
                related_accnum = clamp_len(ra, ACCOUNT_NUMBER_MAXLEN)
                desc = "振込受取"
                balance += amount

        else:
            # 入出金
            if random.random() < 0.55:
                tx_type = "DEPOSIT"
                amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                balance += amount
                related_branch = ""
                related_accnum = ""
                desc = random.choice(["ATM入金", "給与", "返金"])
            else:
                tx_type = "WITHDRAWAL"
                # 出金は残高を超えない
                if balance <= TX_AMOUNT_MIN:
                    # 出金できないなら入金にフォールバック
                    tx_type = "DEPOSIT"
                    amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                    balance += amount
                    related_branch = ""
                    related_accnum = ""
                    desc = "ATM入金"
                else:
                    amount = random.randint(TX_AMOUNT_MIN, min(TX_AMOUNT_MAX, balance))
                    balance -= amount
                    related_branch = ""
                    related_accnum = ""
                    desc = random.choice(["ATM出金", "家賃", "光熱費", "食費", "交通費", "EC決済"])

        tx_type = clamp_len(tx_type, TX_TYPE_MAXLEN)
        # description は nullable なので空でもOK
        description = random.choice(tx_descriptions) if random.random() < 0.2 else desc

        transactions_rows.append([
            account_id,
            tx_type,
            amount,
            balance,
            description,
            related_branch if related_branch != "" else "",
            related_accnum if related_accnum != "" else "",
            iso(t)
        ])

    final_balances[account_id] = balance

# accounts の balance を最終残高に上書き
for row in accounts_rows:
    account_id = row[0]
    row[4] = final_balances[account_id]

# ===== 3) address_change_requests =====
statuses = ["PENDING", "APPROVED", "REJECTED"]

for user_id in user_ids:
    if random.random() > ADDR_REQ_RATE:
        continue

    # 1～3件くらい申請を作る
    req_count = random.randint(1, 3)
    times = [rand_dt_within_years(2) for _ in range(req_count)]
    times.sort()

    for t in times:
        postal = fake.postcode()
        pref = fake.prefecture()
        city = fake.city()
        line1 = fake.street_address()
        line2 = fake.building_name() if random.random() < 0.5 else ""
        # proof は今回は空（bytea列はnullable）
        proof_path = ""
        proof_data = ""  # CSVでは空文字にする（\copy時にNULL扱いにしたいなら COPYオプションでNULL指定）

        status = random.choice(statuses)

        addrreq_rows.append([
            user_id,
            postal,
            pref,
            city,
            clamp_len(line1, 255),
            clamp_len(line2, 255) if line2 != "" else "",
            proof_path,
            proof_data,
            status,
            iso(t)
        ])

# ===== 4) CSV 出力 =====
with open("users.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["id", "username", "password", "email", "created_at"])
    w.writerows(users_rows)

with open("user_profile.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "name_kanji", "name_kana", "birth_date", "gender", "phone", "postal_code", "address", "my_number"])
    w.writerows(profiles_rows)

with open("accounts.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["id", "user_id", "branch_code", "account_number", "balance", "opened_at"])
    w.writerows(accounts_rows)

with open("transactions.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["account_id", "type", "amount", "balance_after", "description", "related_branch_code", "related_account_number", "created_at"])
    w.writerows(transactions_rows)

with open("address_change_requests.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "postal_code", "prefecture", "city", "address_line1", "address_line2",
                "proof_file_path", "proof_file_data", "status", "created_at"])
    w.writerows(addrreq_rows)

print("done: users.csv, user_profile.csv, accounts.csv, transactions.csv, address_change_requests.csv")
