from faker import Faker
import csv
import random
import secrets
import string
from datetime import datetime, timedelta, timezone

fake = Faker("ja_JP")

# generation size
NUM_USERS = 10000
TX_PER_USER_MIN = 5
TX_PER_USER_MAX = 30
ADDR_REQ_RATE = 0.15
TRANSFER_RATE = 0.15

BRANCH_MIN = 1000
BRANCH_MAX = 9999
START_BAL_MIN = 10_000
START_BAL_MAX = 1_000_000
TX_AMOUNT_MIN = 100
TX_AMOUNT_MAX = 200_000

TX_TYPE_MAXLEN = 16
BRANCH_CODE_MAXLEN = 8
ACCOUNT_NUMBER_MAXLEN = 16

# additional dataset sizes for smaller tables
POST_LOGIN_SAMPLE_USERS = 3500
PUBLIC_ATM_EXTRA = 120
PUBLIC_NEWS_EXTRA = 140
PUBLIC_ALERT_EXTRA = 120
PUBLIC_FAQ_EXTRA = 160


def random_password(length=12):
    alphabet = string.ascii_letters + string.digits + "!@#$%^&*"
    return "".join(secrets.choice(alphabet) for _ in range(length))


def iso(dt: datetime) -> str:
    return dt.astimezone(timezone.utc).isoformat()


def rand_dt_within_years(years=5) -> datetime:
    now = datetime.now(timezone.utc)
    start = now - timedelta(days=365 * years)
    delta_sec = int((now - start).total_seconds())
    return start + timedelta(seconds=random.randint(0, delta_sec))


def clamp_len(s: str, maxlen: int) -> str:
    if s is None:
        return ""
    return s[:maxlen]


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


# core tables
users_rows = []
profiles_rows = []
accounts_rows = []
transactions_rows = []
addrreq_rows = []

# post-login tables
beneficiaries_rows = []
login_history_rows = []
user_sessions_rows = []
notifications_rows = []
user_limits_rows = []
service_requests_rows = []
cards_rows = []

# public info tables
atm_locations_rows = []
rate_items_rows = []
fee_items_rows = []
fx_rates_rows = []
news_items_rows = []
security_alerts_rows = []
faq_items_rows = []

account_info = {}  # user_id -> (branch_code, account_number, opened_at)

# 1) users / user_profile / accounts
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

    account_id = user_id
    branch_code = str(random.randint(BRANCH_MIN, BRANCH_MAX))
    account_number = str(random.randint(1000000, 9999999))
    opened_at = rand_dt_within_years(3)
    start_balance = random.randint(START_BAL_MIN, START_BAL_MAX)

    accounts_rows.append([
        account_id,
        user_id,
        clamp_len(branch_code, BRANCH_CODE_MAXLEN),
        clamp_len(account_number, ACCOUNT_NUMBER_MAXLEN),
        start_balance,
        iso(opened_at),
    ])
    account_info[user_id] = (branch_code, account_number, opened_at)

# 2) transactions
final_balances = {row[0]: row[4] for row in accounts_rows}
user_ids = list(range(1, NUM_USERS + 1))

deposit_desc = ["ATM入金", "給与", "振込入金"]
withdraw_desc = ["ATM出金", "買い物", "公共料金", "EC決済"]

for user_id in user_ids:
    account_id = user_id
    balance = final_balances[account_id]

    tx_count = random.randint(TX_PER_USER_MIN, TX_PER_USER_MAX)
    opened_at = account_info[user_id][2]
    base_time = max(opened_at, datetime.now(timezone.utc) - timedelta(days=365 * 3))

    times = [base_time + timedelta(minutes=random.randint(1, 60 * 24 * 365)) for _ in range(tx_count)]
    times.sort()

    for t in times:
        is_transfer = random.random() < TRANSFER_RATE

        if is_transfer and NUM_USERS >= 2:
            if random.random() < 0.5:
                if balance <= TX_AMOUNT_MIN:
                    tx_type = "DEPOSIT"
                    amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                    balance += amount
                    related_branch = ""
                    related_accnum = ""
                    desc = "ATM入金"
                else:
                    tx_type = "TRANSFER_OUT"
                    amount = random.randint(TX_AMOUNT_MIN, min(TX_AMOUNT_MAX, balance))
                    to_user = random.choice(user_ids)
                    while to_user == user_id:
                        to_user = random.choice(user_ids)
                    rb, ra, _ = account_info[to_user]
                    related_branch = clamp_len(rb, BRANCH_CODE_MAXLEN)
                    related_accnum = clamp_len(ra, ACCOUNT_NUMBER_MAXLEN)
                    desc = "振込"
                    balance -= amount
            else:
                tx_type = "TRANSFER_IN"
                amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                from_user = random.choice(user_ids)
                while from_user == user_id:
                    from_user = random.choice(user_ids)
                rb, ra, _ = account_info[from_user]
                related_branch = clamp_len(rb, BRANCH_CODE_MAXLEN)
                related_accnum = clamp_len(ra, ACCOUNT_NUMBER_MAXLEN)
                desc = "振込入金"
                balance += amount
        else:
            if random.random() < 0.55:
                tx_type = "DEPOSIT"
                amount = random.randint(TX_AMOUNT_MIN, TX_AMOUNT_MAX)
                balance += amount
                related_branch = ""
                related_accnum = ""
                desc = random.choice(deposit_desc)
            else:
                tx_type = "WITHDRAWAL"
                if balance <= TX_AMOUNT_MIN:
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
                    desc = random.choice(withdraw_desc)

        tx_type = clamp_len(tx_type, TX_TYPE_MAXLEN)
        transactions_rows.append([
            account_id,
            tx_type,
            amount,
            balance,
            desc,
            related_branch,
            related_accnum,
            iso(t),
        ])

    final_balances[account_id] = balance

for row in accounts_rows:
    row[4] = final_balances[row[0]]

# 3) address_change_requests
statuses = ["PENDING", "APPROVED", "REJECTED"]

for user_id in user_ids:
    if random.random() > ADDR_REQ_RATE:
        continue

    req_count = random.randint(1, 3)
    times = [rand_dt_within_years(2) for _ in range(req_count)]
    times.sort()

    for t in times:
        postal = fake.postcode()
        pref = fake.prefecture()
        city = fake.city()
        line1 = fake.street_address()
        line2 = fake.building_name() if random.random() < 0.5 else ""

        addrreq_rows.append([
            user_id,
            postal,
            pref,
            city,
            clamp_len(line1, 255),
            clamp_len(line2, 255),
            "",
            "",
            random.choice(statuses),
            iso(t),
        ])

# 4) post-login fixed samples (Japanese-friendly)
now = datetime.now(timezone.utc)

beneficiaries_rows.extend([
    [1, "+Acts Bank", "渋谷", "ORDINARY", "1234567", "ヤマダ タロウ", "家賃", iso(now - timedelta(days=30))],
    [1, "+Acts Bank", "丸の内", "SAVINGS", "2345678", "ヤマダ ハナコ", "貯金", iso(now - timedelta(days=20))],
    [2, "+Acts Bank", "梅田", "ORDINARY", "3456789", "スズキ ケン", "家族", iso(now - timedelta(days=10))],
])

login_history_rows.extend([
    [1, "SUCCESS", "203.0.113.10", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", iso(now - timedelta(days=2))],
    [1, "FAILED", "203.0.113.10", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", iso(now - timedelta(days=2) + timedelta(minutes=1))],
    [1, "SUCCESS", "203.0.113.11", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)", iso(now - timedelta(days=1))],
    [2, "SUCCESS", "198.51.100.20", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)", iso(now - timedelta(hours=3))],
])

user_sessions_rows.extend([
    ["sess-001", 1, "dummy-token-1", "203.0.113.10", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", iso(now - timedelta(days=2))],
    ["sess-002", 1, "dummy-token-2", "203.0.113.11", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)", iso(now - timedelta(days=1))],
    ["sess-003", 2, "dummy-token-3", "198.51.100.20", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)", iso(now - timedelta(hours=3))],
])

notifications_rows.extend([
    [1, "住所変更を受け付けました", "住所変更の申請を受け付けました。審査完了までお待ちください。", "INFO", "address", "false", iso(now - timedelta(days=1))],
    [1, "振込受付", "振込依頼を受け付けました。", "INFO", "transfer", "true", iso(now - timedelta(days=3))],
    [2, "セキュリティ注意喚起", "不審なログインが検出されました。パスワード変更をご検討ください。", "WARN", "security", "false", iso(now - timedelta(hours=5))],
])

user_limits_rows.extend([
    [1, 500000, 100000, iso(now - timedelta(days=7))],
    [2, 300000, 50000, iso(now - timedelta(days=2))],
])

service_requests_rows.extend([
    [1, "ADDRESS_CHANGE", "REVIEWING", "住所変更申請", "本人確認書類を確認中です。", iso(now - timedelta(days=2)), iso(now - timedelta(days=1))],
    [1, "CARD_REISSUE", "RECEIVED", "カード再発行申請", "カード再発行を受け付けました。", iso(now - timedelta(hours=6)), iso(now - timedelta(hours=6))],
    [2, "NAME_CHANGE", "COMPLETED", "名義変更申請", "名義変更が完了しました。", iso(now - timedelta(days=10)), iso(now - timedelta(days=9))],
])

cards_rows.extend([
    [1, "DEBIT", "**** **** **** 1234", "ACTIVE", "false", iso(now - timedelta(days=200)), iso(now - timedelta(days=2))],
    [1, "CASH", "**** **** **** 5678", "LOCKED", "true", iso(now - timedelta(days=400)), iso(now - timedelta(days=1))],
    [2, "DEBIT", "**** **** **** 9012", "ACTIVE", "false", iso(now - timedelta(days=100)), iso(now - timedelta(hours=3))],
])

# 5) public-info fixed samples
atm_locations_rows.extend([
    ["atm-001", "+Acts Bank 渋谷支店", "tokyo", "Shibuya", "東京都渋谷区道玄坂1-1-1", 35.6581, 139.7017, "true", "true", "withdraw,deposit,transfer", "07:00-23:00", "2026-02-10T09:00:00+09:00"],
    ["atm-002", "+Acts Bank 丸の内支店", "tokyo", "Chiyoda", "東京都千代田区丸の内2-4-1", 35.6812, 139.7671, "false", "true", "withdraw,deposit", "08:00-22:00", "2026-02-09T10:30:00+09:00"],
    ["atm-003", "+Acts Bank 梅田支店", "osaka", "Kita", "大阪府大阪市北区梅田3-1-1", 34.7025, 135.4959, "true", "false", "withdraw", "24H", "2026-02-08T14:10:00+09:00"],
    ["atm-004", "+Acts Bank 札幌支店", "hokkaido", "Chuo", "北海道札幌市中央区北4条西2-1", 43.0687, 141.3507, "true", "true", "withdraw,deposit,loan", "09:00-21:00", "2026-02-07T17:20:00+09:00"],
])

rate_items_rows.extend([
    ["deposit", "普通預金", 0.020, "", "通常金利"],
    ["deposit", "定期預金", 0.180, "6ヶ月", "キャンペーン: 2026-03-31まで"],
    ["deposit", "定期預金", 0.220, "12ヶ月", "キャンペーン: 2026-03-31まで"],
    ["loan", "住宅ローン", 1.100, "変動", "半年ごとに見直し"],
    ["loan", "マイカーローン", 1.800, "固定", "全期間固定"],
])

fee_items_rows.extend([
    ["transfer", "online", 110, "当行宛"],
    ["transfer", "online", 220, "他行宛"],
    ["transfer", "branch", 330, "窓口取扱い"],
    ["atm", "other-bank", 220, "他行ATM"],
    ["card", "reissue", 1100, "カード再発行"],
])

fx_rates_rows.extend([
    ["JPY", 1.0],
    ["USD", 150.25],
    ["EUR", 162.80],
    ["GBP", 189.45],
    ["AUD", 98.70],
])

news_items_rows.extend([
    ["20260211-001", "maintenance", "+Acts Bank モバイルアプリ メンテナンスのお知らせ", "モバイルバンキングアプリの計画メンテナンスを実施します。", "2026-02-12 01:00-03:00（JST）に計画メンテナンスを実施します。一部機能がご利用いただけない場合があります。", "2026-02-11T09:00:00+09:00", "2026-02-11T09:00:00+09:00"],
    ["20260210-002", "announcement", "振込手数料改定のお知らせ", "振込手数料の改定についてご案内します。", "2026-03-01よりオンラインチャネルの振込手数料が変更となります。", "2026-02-10T15:30:00+09:00", "2026-02-10T15:30:00+09:00"],
    ["20260209-003", "outage", "ATMネットワーク障害 復旧のお知らせ", "一部地域で発生していたATM接続障害が復旧しました。", "一部地域で発生していたATMネットワーク障害は復旧しました。ご不便をおかけしました。", "2026-02-09T12:15:00+09:00", "2026-02-09T14:00:00+09:00"],
])

security_alerts_rows.extend([
    ["sec-001", "銀行サポートを装ったフィッシングSMS", "phishing", "high", 128, "差出人不明のリンクは開かず、公式アプリで確認してください。", "2026-02-10T09:30:00+09:00"],
    ["sec-002", "融資可決を装う偽の電話", "voice", "medium", 64, "当行が電話で暗証番号を伺うことはありません。", "2026-02-08T11:20:00+09:00"],
    ["sec-003", "アカウントへの不正ログイン試行", "account", "high", 92, "パスワードの使い回しを避け、MFAを有効にしてください。", "2026-02-09T18:10:00+09:00"],
    ["sec-004", "不正なQR決済依頼", "payment", "medium", 51, "支払先情報を確認してから承認してください。", "2026-02-07T16:05:00+09:00"],
])

faq_items_rows.extend([
    ["faq-001", "account", "住所変更はどうすればいいですか？", "プロフィール設定から住所変更が可能です。お近くの店舗でも承ります。", "address,profile"],
    ["faq-002", "transfer", "振込はいつ着金しますか？", "国内振込は通常、当日中（締切時刻まで）に着金します。", "transfer,schedule"],
    ["faq-003", "security", "フィッシングが疑われる場合は？", "セキュリティ窓口へご連絡のうえ、パスワードを直ちに変更してください。", "phishing,security"],
    ["faq-004", "loan", "ローンの繰上返済はできますか？", "商品によって条件は異なりますが、繰上返済は可能です。", "loan,repayment"],
])

# 5b) expand smaller tables for practical CSV size
sample_users = min(NUM_USERS, POST_LOGIN_SAMPLE_USERS)
pref_codes = ["tokyo", "osaka", "kanagawa", "saitama", "chiba", "aichi", "fukuoka", "hokkaido", "hyogo", "kyoto"]

for user_id in range(1, sample_users + 1):
    # beneficiaries: 0..2 per user
    for _ in range(random.randint(0, 2)):
        acct_num = str(random.randint(1000000, 9999999))
        holder_name = f"{fake.last_name()} {fake.first_name()}"
        beneficiaries_rows.append([
            user_id,
            "+Acts Bank",
            random.choice(["渋谷", "丸の内", "梅田", "札幌", "横浜", "名古屋"]),
            random.choice(["ORDINARY", "SAVINGS"]),
            acct_num,
            holder_name,
            random.choice(["家賃", "貯金", "家族", "学費", "仕送り", ""]),
            iso(rand_dt_within_years(1)),
        ])

    # login_history: 1..4 per user
    for _ in range(random.randint(1, 4)):
        login_history_rows.append([
            user_id,
            random.choice(["SUCCESS", "FAILED"]),
            fake.ipv4_public(),
            random.choice([
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
                "Mozilla/5.0 (Linux; Android 14)",
            ]),
            iso(rand_dt_within_years(1)),
        ])

    # sessions: 0..2 per user
    sess_count = random.randint(0, 2)
    for idx in range(sess_count):
        user_sessions_rows.append([
            f"sess-{user_id}-{idx}-{random.randint(1000,9999)}",
            user_id,
            f"dummy-token-{user_id}-{idx}-{random.randint(10000,99999)}",
            fake.ipv4_public(),
            random.choice([
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
            ]),
            iso(rand_dt_within_years(1)),
        ])

    # notifications: 1..3 per user
    for _ in range(random.randint(1, 3)):
        title, message, severity, category = random.choice([
            ("住所変更を受け付けました", "住所変更の申請を受け付けました。", "INFO", "address"),
            ("振込受付", "振込依頼を受け付けました。", "INFO", "transfer"),
            ("セキュリティ注意喚起", "不審なログインが検出されました。", "WARN", "security"),
            ("カード利用通知", "カード利用を確認しました。", "INFO", "card"),
        ])
        notifications_rows.append([
            user_id,
            title,
            message,
            severity,
            category,
            random.choice(["true", "false"]),
            iso(rand_dt_within_years(1)),
        ])

    # user_limits: one row per user
    user_limits_rows.append([
        user_id,
        random.choice([100000, 200000, 300000, 500000, 1000000]),
        random.choice([30000, 50000, 100000, 200000]),
        iso(rand_dt_within_years(1)),
    ])

    # service requests: 0..2 per user
    for _ in range(random.randint(0, 2)):
        req_type, title = random.choice([
            ("ADDRESS_CHANGE", "住所変更申請"),
            ("CARD_REISSUE", "カード再発行申請"),
            ("NAME_CHANGE", "名義変更申請"),
            ("LIMIT_CHANGE", "限度額変更申請"),
        ])
        status = random.choice(["RECEIVED", "REVIEWING", "COMPLETED", "REJECTED"])
        created = rand_dt_within_years(1)
        updated = created + timedelta(hours=random.randint(1, 240))
        service_requests_rows.append([
            user_id,
            req_type,
            status,
            title,
            f"{title}の処理状況: {status}",
            iso(created),
            iso(updated),
        ])

    # cards: 1..2 per user
    for _ in range(random.randint(1, 2)):
        locked = random.choice(["true", "false"])
        status = "LOCKED" if locked == "true" else random.choice(["ACTIVE", "ACTIVE", "REISSUE_REQUESTED"])
        cards_rows.append([
            user_id,
            random.choice(["DEBIT", "CASH"]),
            f"**** **** **** {random.randint(1000, 9999)}",
            status,
            locked,
            iso(rand_dt_within_years(2)),
            iso(rand_dt_within_years(1)),
        ])

# public tables expansion
for i in range(1, PUBLIC_ATM_EXTRA + 1):
    atm_locations_rows.append([
        f"atm-{1000 + i}",
        f"+Acts Bank {fake.city()}支店",
        random.choice(pref_codes),
        fake.city(),
        fake.address().replace("\n", " "),
        round(float(fake.latitude()), 6),
        round(float(fake.longitude()), 6),
        random.choice(["true", "false"]),
        random.choice(["true", "false"]),
        random.choice(["withdraw", "withdraw,deposit", "withdraw,deposit,transfer"]),
        random.choice(["07:00-23:00", "08:00-22:00", "09:00-21:00", "24H"]),
        iso(rand_dt_within_years(1)),
    ])

rate_combo = set((r[0], r[1], r[2]) for r in rate_items_rows)
for category, product, terms in [
    ("deposit", "普通預金", [""]),
    ("deposit", "定期預金", ["1ヶ月", "3ヶ月", "6ヶ月", "12ヶ月", "24ヶ月"]),
    ("loan", "住宅ローン", ["変動", "固定3年", "固定5年", "固定10年"]),
    ("loan", "マイカーローン", ["固定", "変動"]),
    ("loan", "教育ローン", ["固定", "変動"]),
]:
    for term in terms:
        key = (category, product, term)
        if key in rate_combo:
            continue
        rate_combo.add(key)
        rate_items_rows.append([category, product, round(random.uniform(0.01, 4.8), 3), term, "参考金利"])

fee_combo = set((r[0], r[1], r[2]) for r in fee_items_rows)
for service in ["transfer", "atm", "card", "account"]:
    for channel in ["online", "branch", "other-bank", "app"]:
        amount = random.choice([0, 110, 220, 330, 440, 550, 1100])
        key = (service, channel, amount)
        if key in fee_combo:
            continue
        fee_combo.add(key)
        fee_items_rows.append([service, channel, amount, "標準手数料"])

fx_map = {
    "JPY": 1.0, "USD": 150.25, "EUR": 162.80, "GBP": 189.45, "AUD": 98.70, "CAD": 109.10,
    "CHF": 171.20, "NZD": 91.35, "CNY": 20.75, "KRW": 0.11, "HKD": 19.20, "SGD": 111.60,
    "THB": 4.15, "TWD": 4.70, "PHP": 2.65, "INR": 1.82, "IDR": 0.0096, "VND": 0.0062,
}
fx_rates_rows[:] = [[k, v] for k, v in fx_map.items()]

for i in range(1, PUBLIC_NEWS_EXTRA + 1):
    nid = f"news-{20260000 + i}"
    category = random.choice(["maintenance", "announcement", "outage"])
    title = random.choice(["システムメンテナンスのお知らせ", "手数料改定のお知らせ", "障害復旧のお知らせ", "機能追加のお知らせ"])
    summary = random.choice(["重要なお知らせを掲載します。", "サービス品質向上のため実施します。", "影響範囲をご確認ください。"])
    body = random.choice([
        "対象時間中は一部サービスがご利用いただけない場合があります。",
        "詳細はオンラインバンキングのお知らせをご確認ください。",
        "ご不便をおかけしますが、何卒ご理解ください。",
    ])
    published = rand_dt_within_years(1)
    updated = published + timedelta(hours=random.randint(0, 72))
    news_items_rows.append([nid, category, title, summary, body, iso(published), iso(updated)])

for i in range(1, PUBLIC_ALERT_EXTRA + 1):
    aid = f"sec-{1000 + i}"
    tag = random.choice(["phishing", "voice", "account", "payment", "malware"])
    title = random.choice([
        "フィッシングSMSへの注意",
        "不審な電話への注意",
        "アカウント不正ログイン試行",
        "不審な決済依頼への注意",
    ])
    security_alerts_rows.append([
        aid,
        title,
        tag,
        random.choice(["low", "medium", "high"]),
        random.randint(1, 250),
        "不審な連絡は無視し、公式窓口へ確認してください。",
        iso(rand_dt_within_years(1)),
    ])

for i in range(1, PUBLIC_FAQ_EXTRA + 1):
    fid = f"faq-{1000 + i}"
    category = random.choice(["account", "transfer", "security", "loan", "card"])
    question = random.choice([
        "住所変更の方法を教えてください。",
        "振込の反映時間はいつですか？",
        "ログインできない場合の対処は？",
        "カード再発行の手数料はいくらですか？",
    ])
    answer = random.choice([
        "アプリまたはWebの設定画面から申請できます。",
        "通常は当日中に反映されますが、時間帯により翌営業日となる場合があります。",
        "パスワード再設定後も解決しない場合はサポートへご連絡ください。",
        "手数料は商品により異なります。最新情報をご確認ください。",
    ])
    tags = random.choice(["address,profile", "transfer,schedule", "phishing,security", "loan,repayment", "card,reissue"])
    faq_items_rows.append([fid, category, question, answer, tags])

# normalize rows for PK/unique-sensitive tables
user_limits_map = {}
for row in user_limits_rows:
    user_limits_map[row[0]] = row
user_limits_rows = [user_limits_map[k] for k in sorted(user_limits_map.keys())]

# 6) CSV output
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
    w.writerow(["user_id", "postal_code", "prefecture", "city", "address_line1", "address_line2", "proof_file_path", "proof_file_data", "status", "created_at"])
    w.writerows(addrreq_rows)

with open("beneficiaries.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "bank_name", "branch_name", "account_type", "account_number", "account_holder_name", "nickname", "created_at"])
    w.writerows(beneficiaries_rows)

with open("login_history.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "result", "ip", "user_agent", "occurred_at"])
    w.writerows(login_history_rows)

with open("user_sessions.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["session_id", "user_id", "jwt_token", "ip", "user_agent", "login_at"])
    w.writerows(user_sessions_rows)

with open("notifications.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "title", "message", "severity", "category", "is_read", "created_at"])
    w.writerows(notifications_rows)

with open("user_limits.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "transfer_limit_yen", "atm_withdraw_limit_yen", "updated_at"])
    w.writerows(user_limits_rows)

with open("service_requests.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "request_type", "status", "title", "detail", "created_at", "updated_at"])
    w.writerows(service_requests_rows)

with open("cards.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["user_id", "card_type", "masked_number", "status", "locked", "created_at", "updated_at"])
    w.writerows(cards_rows)

with open("atm_locations.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["id", "name", "pref", "city", "address", "lat", "lng", "open_now", "cash", "services", "hours", "updated_at"])
    w.writerows(atm_locations_rows)

with open("rate_items.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["category", "product", "rate_percent", "term", "note"])
    w.writerows(rate_items_rows)

with open("fee_items.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["service", "channel", "amount_yen", "note"])
    w.writerows(fee_items_rows)

with open("fx_rates.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["currency", "rate_to_jpy"])
    w.writerows(fx_rates_rows)

with open("news_items.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["id", "category", "title", "summary", "body", "published_at", "updated_at"])
    w.writerows(news_items_rows)

with open("security_alerts.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["id", "title", "tag", "risk_level", "recent_count", "tip", "updated_at"])
    w.writerows(security_alerts_rows)

with open("faq_items.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["id", "category", "question", "answer", "tags"])
    w.writerows(faq_items_rows)

print(
    "done: users.csv, user_profile.csv, accounts.csv, transactions.csv, address_change_requests.csv, "
    "beneficiaries.csv, login_history.csv, user_sessions.csv, notifications.csv, user_limits.csv, "
    "service_requests.csv, cards.csv, atm_locations.csv, rate_items.csv, fee_items.csv, fx_rates.csv, "
    "news_items.csv, security_alerts.csv, faq_items.csv"
)
