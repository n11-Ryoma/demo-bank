sudo apt update
sudo apt install -y postgresql postgresql-contrib
=========================================
sudo systemctl status postgresql
active (running) になっていればOK。
=========================================
起動していなければ：
sudo systemctl start postgresql
sudo systemctl enable postgresql
psql --version
=========================================

sudo vi /etc/postgresql/16/main/postgresql.conf
→いかになるように追記
listen_addresses = '*'
port = 5432

psql -c "SHOW hba_file;"
sudo vim /etc/postgresql/16/main/pg_hba.conf
→いかになるように追記
# TEMP: allow any (DO NOT USE IN PROD)
host    all     all     0.0.0.0/0     scram-sha-256
host    all     all     ::/0          scram-sha-256
host  bank_app  bank_user  10.0.2.2/32  scram-sha-256
sudo systemctl restart postgresql

=========================================
sudo -i -u postgres
psql
=========================================
-- 1) アプリ用ユーザー作成（パスワードは任意に変更）
CREATE USER bank_user WITH PASSWORD 'bank_pass';
-- 2) DB作成（オーナーをアプリユーザーに）
CREATE DATABASE bank_app OWNER bank_user;
\q
=========================================
psql -h 127.0.0.1 -U bank_user -d bank_app
=========================================
-- 3) 権限（基本はオーナーなので不要だが明示）
GRANT ALL PRIVILEGES ON DATABASE bank_app TO bank_user;
=========================================
BEGIN;

-- users（テスト用：平文パスワード）
CREATE TABLE IF NOT EXISTS users (
  id           BIGSERIAL PRIMARY KEY,
  username     VARCHAR(64) NOT NULL UNIQUE,
  password     TEXT NOT NULL,                 -- テスト限定（平文）
  email        VARCHAR(254),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- accounts（1ユーザー1口座：user_id UNIQUE）
CREATE TABLE IF NOT EXISTS accounts (
  id             BIGSERIAL PRIMARY KEY,
  user_id        BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
  branch_code    VARCHAR(8) NOT NULL,
  account_number VARCHAR(16) NOT NULL,
  balance        BIGINT NOT NULL DEFAULT 0,     -- JPY想定
  opened_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (branch_code, account_number)
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);

-- transactions
CREATE TABLE IF NOT EXISTS transactions (
  id                    BIGSERIAL PRIMARY KEY,
  account_id             BIGINT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,

  type                  VARCHAR(16) NOT NULL,  -- 'DEPOSIT','WITHDRAWAL','TRANSFER_IN','TRANSFER_OUT',...
  amount                BIGINT NOT NULL CHECK (amount >= 0),
  balance_after         BIGINT NOT NULL,

  description           TEXT,
  related_branch_code    VARCHAR(8),
  related_account_number VARCHAR(16),

  created_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 一覧表示・ページング用
CREATE INDEX IF NOT EXISTS idx_tx_account_created
  ON transactions(account_id, created_at DESC, id DESC);

-- relatedAccountNumber検索用
CREATE INDEX IF NOT EXISTS idx_tx_related_accnum
  ON transactions(related_account_number);

-- description検索（簡易）
CREATE INDEX IF NOT EXISTS idx_tx_desc_gin
  ON transactions USING GIN (to_tsvector('simple', coalesce(description,'')));

COMMIT;
CREATE TABLE IF NOT EXISTS address_change_requests (
  id              BIGSERIAL PRIMARY KEY,

  user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

  postal_code     VARCHAR(16)  NOT NULL,
  prefecture      VARCHAR(64)  NOT NULL,
  city            VARCHAR(128) NOT NULL,
  address_line1   VARCHAR(255) NOT NULL,
  address_line2   VARCHAR(255),

  proof_file_path TEXT,
  proof_file_data BYTEA,                 -- byte[] に対応

  status          VARCHAR(16) NOT NULL,  -- 'PENDING' など
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 最新1件取得を速くする（findLatest...に効く）
CREATE INDEX IF NOT EXISTS idx_addr_req_user_created
  ON address_change_requests(user_id, created_at DESC);
CREATE TABLE IF NOT EXISTS user_profile (
  user_id        BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  name_kanji     VARCHAR(128) NOT NULL,
  name_kana      VARCHAR(128),
  birth_date     DATE NOT NULL,
  gender         VARCHAR(16),
  phone          VARCHAR(32) NOT NULL,
  postal_code    VARCHAR(16),
  address        TEXT NOT NULL,
  my_number      VARCHAR(32)  
);


=========================================
csvがあるフォルダを開く
別タブで
scp *.csv user@localhost:/tmp/pos
ssh user@localhost
cd /tmp/pos
ls -l /tmp/pos
sudo chown -R postgres:postgres /tmp/pos
sudo chmod 755 /tmp/pos
sudo chmod 644 /tmp/pos/*.csv
=========================================
sudo -i -u postgres
psql -h 127.0.0.1 -U bank_user -d bank_app

=========================================
TRUNCATE TABLE transactions RESTART IDENTITY CASCADE;
TRUNCATE TABLE address_change_requests RESTART IDENTITY CASCADE;
TRUNCATE TABLE accounts RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_profile RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
=========================================
\copy users (id, username, password, email, created_at)
FROM '/tmp/pos/users.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy user_profile
(user_id, name_kanji, name_kana, birth_date, gender, phone, postal_code, address, my_number)
FROM '/tmp/pos/user_profile.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy accounts
(id, user_id, branch_code, account_number, balance, opened_at)
FROM '/tmp/pos/accounts.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy transactions
(account_id, type, amount, balance_after, description, related_branch_code, related_account_number, created_at)
FROM '/tmp/pos/transactions.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy address_change_requests
(user_id, postal_code, prefecture, city, address_line1, address_line2,
 proof_file_path, proof_file_data, status, created_at)
FROM '/tmp/pos/address_change_requests.csv'
WITH (FORMAT csv, HEADER true);
=========================================
SELECT setval(pg_get_serial_sequence('users', 'id'),
              (SELECT MAX(id) FROM users));

SELECT setval(pg_get_serial_sequence('accounts', 'id'),
              (SELECT MAX(id) FROM accounts));

SELECT setval(pg_get_serial_sequence('transactions', 'id'),
              (SELECT MAX(id) FROM transactions));

SELECT setval(pg_get_serial_sequence('address_change_requests', 'id'),
              (SELECT MAX(id) FROM address_change_requests));
=========================================
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM user_profile;
SELECT COUNT(*) FROM accounts;
SELECT COUNT(*) FROM transactions;
SELECT COUNT(*) FROM address_change_requests;
=========================================
SELECT u.id, u.username, a.branch_code, a.account_number, a.balance
FROM users u
JOIN accounts a ON a.user_id = u.id
WHERE u.username = 'admin';
=========================================
BEGIN;

CREATE TABLE IF NOT EXISTS atm_locations (
  id varchar(32) primary key,
  name varchar(200) not null,
  pref varchar(50) not null,
  city varchar(100) not null,
  address varchar(200) not null,
  lat double precision not null,
  lng double precision not null,
  open_now boolean not null,
  cash boolean not null,
  services varchar(200) not null,
  hours varchar(50) not null,
  updated_at timestamptz
);

CREATE TABLE IF NOT EXISTS rate_items (
  id serial primary key,
  category varchar(50) not null,
  product varchar(100) not null,
  rate_percent numeric(6,3) not null,
  term varchar(50) not null,
  note varchar(200),
  unique (category, product, term)
);

CREATE TABLE IF NOT EXISTS fee_items (
  id serial primary key,
  service varchar(50) not null,
  channel varchar(50) not null,
  amount_yen int not null,
  note varchar(200),
  unique (service, channel, amount_yen)
);

CREATE TABLE IF NOT EXISTS fx_rates (
  currency varchar(10) primary key,
  rate_to_jpy numeric(12,6) not null
);

CREATE TABLE IF NOT EXISTS news_items (
  id varchar(32) primary key,
  category varchar(50) not null,
  title varchar(200) not null,
  summary varchar(500) not null,
  body text not null,
  published_at timestamptz not null,
  updated_at timestamptz not null
);

CREATE TABLE IF NOT EXISTS security_alerts (
  id varchar(32) primary key,
  title varchar(200) not null,
  tag varchar(50) not null,
  risk_level varchar(20) not null,
  recent_count int not null,
  tip varchar(500) not null,
  updated_at timestamptz not null
);

CREATE TABLE IF NOT EXISTS faq_items (
  id varchar(32) primary key,
  category varchar(50) not null,
  question varchar(300) not null,
  answer text not null,
  tags varchar(200)
);

COMMIT;

=========================================
BEGIN;

INSERT INTO atm_locations (id, name, pref, city, address, lat, lng, open_now, cash, services, hours, updated_at) VALUES
('atm-001', '+Acts Bank 渋谷支店', 'tokyo', 'Shibuya', '東京都渋谷区道玄坂1-1-1', 35.6581, 139.7017, true, true, 'withdraw,deposit,transfer', '07:00-23:00', '2026-02-10T09:00:00+09:00'),
('atm-002', '+Acts Bank 丸の内支店', 'tokyo', 'Chiyoda', '東京都千代田区丸の内2-4-1', 35.6812, 139.7671, false, true, 'withdraw,deposit', '08:00-22:00', '2026-02-09T10:30:00+09:00'),
('atm-003', '+Acts Bank 梅田支店', 'osaka', 'Kita', '大阪府大阪市北区梅田3-1-1', 34.7025, 135.4959, true, false, 'withdraw', '24H', '2026-02-08T14:10:00+09:00'),
('atm-004', '+Acts Bank 札幌支店', 'hokkaido', 'Chuo', '北海道札幌市中央区北4条西2-1', 43.0687, 141.3507, true, true, 'withdraw,deposit,loan', '09:00-21:00', '2026-02-07T17:20:00+09:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rate_items (category, product, rate_percent, term, note) VALUES
('deposit', '普通預金', 0.020, '', '通常金利'),
('deposit', '定期預金', 0.180, '6ヶ月', 'キャンペーン：2026-03-31まで'),
('deposit', '定期預金', 0.220, '12ヶ月', 'キャンペーン：2026-03-31まで'),
('loan', '住宅ローン', 1.100, '変動', '半年ごとに見直し'),
('loan', 'マイカーローン', 1.800, '固定', '全期間固定')
ON CONFLICT (category, product, term) DO NOTHING;

INSERT INTO fee_items (service, channel, amount_yen, note) VALUES
('transfer', 'online', 110, '当行宛'),
('transfer', 'online', 220, '他行宛'),
('transfer', 'branch', 330, '窓口取扱い'),
('atm', 'other-bank', 220, '他行ATM'),
('card', 'reissue', 1100, 'カード再発行')
ON CONFLICT (service, channel, amount_yen) DO NOTHING;

INSERT INTO fx_rates (currency, rate_to_jpy) VALUES
('JPY', 1.0),
('USD', 150.25),
('EUR', 162.80),
('GBP', 189.45),
('AUD', 98.70)
ON CONFLICT (currency) DO NOTHING;

INSERT INTO news_items (id, category, title, summary, body, published_at, updated_at) VALUES
('20260211-001', 'maintenance', '+Acts Bank モバイルアプリ メンテナンスのお知らせ', 'モバイルバンキングアプリの計画メンテナンスを実施します。',
 '2026-02-12 01:00-03:00（JST）に計画メンテナンスを実施します。一部機能がご利用いただけない場合があります。',
 '2026-02-11T09:00:00+09:00', '2026-02-11T09:00:00+09:00'),
('20260210-002', 'announcement', '振込手数料改定のお知らせ', '振込手数料の改定についてご案内します。',
 '2026-03-01よりオンラインチャネルの振込手数料が変更となります。',
 '2026-02-10T15:30:00+09:00', '2026-02-10T15:30:00+09:00'),
('20260209-003', 'outage', 'ATMネットワーク障害 復旧のお知らせ', '一部地域で発生していたATM接続障害が復旧しました。',
 '一部地域で発生していたATMネットワーク障害は復旧しました。ご不便をおかけしました。',
 '2026-02-09T12:15:00+09:00', '2026-02-09T14:00:00+09:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO security_alerts (id, title, tag, risk_level, recent_count, tip, updated_at) VALUES
('sec-001', '銀行サポートを装ったフィッシングSMS', 'phishing', 'high', 128,
 '差出人不明のリンクは開かず、公式アプリで確認してください。', '2026-02-10T09:30:00+09:00'),
('sec-002', '融資可決を装う偽の電話', 'voice', 'medium', 64,
 '当行が電話で暗証番号を伺うことはありません。', '2026-02-08T11:20:00+09:00'),
('sec-003', 'アカウントへの不正ログイン試行', 'account', 'high', 92,
 'パスワードの使い回しを避け、MFAを有効にしてください。', '2026-02-09T18:10:00+09:00'),
('sec-004', '不正なQR決済依頼', 'payment', 'medium', 51,
 '支払先情報を確認してから承認してください。', '2026-02-07T16:05:00+09:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO faq_items (id, category, question, answer, tags) VALUES
('faq-001', 'account', '住所変更はどうすればいいですか？',
 'プロフィール設定から住所変更が可能です。お近くの店舗でも承ります。', 'address,profile'),
('faq-002', 'transfer', '振込はいつ着金しますか？',
 '国内振込は通常、当日中（締切時刻まで）に着金します。', 'transfer,schedule'),
('faq-003', 'security', 'フィッシングが疑われる場合は？',
 'セキュリティ窓口へご連絡のうえ、パスワードを直ちに変更してください。', 'phishing,security'),
('faq-004', 'loan', 'ローンの繰上返済はできますか？',
 '商品によって条件は異なりますが、繰上返済は可能です。', 'loan,repayment')
ON CONFLICT (id) DO NOTHING;

COMMIT;


=========================================

-- 追加分（beneficiaries / security / notifications / limits / requests / cards）
CREATE TABLE IF NOT EXISTS beneficiaries (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  bank_name VARCHAR(120) NOT NULL,
  branch_name VARCHAR(120),
  account_type VARCHAR(30) NOT NULL,
  account_number VARCHAR(20) NOT NULL,
  account_holder_name VARCHAR(120) NOT NULL,
  nickname VARCHAR(120),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS login_history (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  result VARCHAR(20) NOT NULL,
  ip VARCHAR(64) NOT NULL,
  user_agent VARCHAR(300) NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_sessions (
  session_id VARCHAR(64) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  jwt_token VARCHAR(2000) NOT NULL,
  ip VARCHAR(64) NOT NULL,
  user_agent VARCHAR(300) NOT NULL,
  login_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  message VARCHAR(1000) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  category VARCHAR(50) NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_limits (
  user_id BIGINT PRIMARY KEY,
  transfer_limit_yen BIGINT NOT NULL,
  atm_withdraw_limit_yen BIGINT NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS service_requests (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  request_type VARCHAR(50) NOT NULL,
  status VARCHAR(30) NOT NULL,
  title VARCHAR(200) NOT NULL,
  detail VARCHAR(2000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cards (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  card_type VARCHAR(30) NOT NULL,
  masked_number VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  locked BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


=========================================

BEGIN;

-- beneficiaries
INSERT INTO beneficiaries
(user_id, bank_name, branch_name, account_type, account_number, account_holder_name, nickname)
VALUES
(1, 'Ichiban Bank', 'Shibuya', 'ORDINARY', '1234567', 'TARO YAMADA', '家賃'),
(1, 'Ichiban Bank', 'Marunouchi', 'SAVINGS', '2345678', 'HANAKO YAMADA', '貯金'),
(2, 'Plus Acts Bank', 'Umeda', 'ORDINARY', '3456789', 'KEN SUZUKI', '家族');

-- login_history
INSERT INTO login_history
(user_id, result, ip, user_agent, occurred_at)
VALUES
(1, 'SUCCESS', '203.0.113.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', now() - interval '2 days'),
(1, 'FAILED',  '203.0.113.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', now() - interval '2 days' + interval '1 minute'),
(1, 'SUCCESS', '203.0.113.11', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)', now() - interval '1 day'),
(2, 'SUCCESS', '198.51.100.20', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', now() - interval '3 hours');

-- user_sessions
INSERT INTO user_sessions
(session_id, user_id, jwt_token, ip, user_agent, login_at)
VALUES
('sess-001', 1, 'dummy-token-1', '203.0.113.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', now() - interval '2 days'),
('sess-002', 1, 'dummy-token-2', '203.0.113.11', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)', now() - interval '1 day'),
('sess-003', 2, 'dummy-token-3', '198.51.100.20', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', now() - interval '3 hours');

-- notifications
INSERT INTO notifications
(user_id, title, message, severity, category, is_read, created_at)
VALUES
(1, '住所変更を受け付けました', '住所変更の申請を受け付けました。審査完了までお待ちください。', 'INFO', 'address', false, now() - interval '1 day'),
(1, '振込受付', '振込依頼を受け付けました。', 'INFO', 'transfer', true, now() - interval '3 days'),
(2, 'セキュリティ注意喚起', '不審なログインが検出されました。パスワード変更をご検討ください。', 'WARN', 'security', false, now() - interval '5 hours');

-- user_limits
INSERT INTO user_limits
(user_id, transfer_limit_yen, atm_withdraw_limit_yen, updated_at)
VALUES
(1, 500000, 100000, now() - interval '7 days'),
(2, 300000,  50000, now() - interval '2 days')
ON CONFLICT (user_id) DO UPDATE
SET transfer_limit_yen = EXCLUDED.transfer_limit_yen,
    atm_withdraw_limit_yen = EXCLUDED.atm_withdraw_limit_yen,
    updated_at = EXCLUDED.updated_at;

-- service_requests
INSERT INTO service_requests
(user_id, request_type, status, title, detail, created_at, updated_at)
VALUES
(1, 'ADDRESS_CHANGE', 'REVIEWING', '住所変更申請', '本人確認書類を確認中です。', now() - interval '2 days', now() - interval '1 day'),
(1, 'CARD_REISSUE', 'RECEIVED', 'カード再発行申請', 'カード再発行を受け付けました。', now() - interval '6 hours', now() - interval '6 hours'),
(2, 'NAME_CHANGE', 'COMPLETED', '名義変更申請', '名義変更が完了しました。', now() - interval '10 days', now() - interval '9 days');

-- cards
INSERT INTO cards
(user_id, card_type, masked_number, status, locked, created_at, updated_at)
VALUES
(1, 'DEBIT', '**** **** **** 1234', 'ACTIVE', false, now() - interval '200 days', now() - interval '2 days'),
(1, 'CASH',  '**** **** **** 5678', 'LOCKED', true,  now() - interval '400 days', now() - interval '1 day'),
(2, 'DEBIT', '**** **** **** 9012', 'ACTIVE', false, now() - interval '100 days', now() - interval '3 hours');

COMMIT;


=========================================
追加テーブル（ログイン後機能）CSV取り込み
=========================================
TRUNCATE TABLE beneficiaries RESTART IDENTITY CASCADE;
TRUNCATE TABLE login_history RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_sessions RESTART IDENTITY CASCADE;
TRUNCATE TABLE notifications RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_limits RESTART IDENTITY CASCADE;
TRUNCATE TABLE service_requests RESTART IDENTITY CASCADE;
TRUNCATE TABLE cards RESTART IDENTITY CASCADE;
=========================================
\copy beneficiaries
(user_id, bank_name, branch_name, account_type, account_number, account_holder_name, nickname, created_at)
FROM '/tmp/pos/beneficiaries.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy login_history
(user_id, result, ip, user_agent, occurred_at)
FROM '/tmp/pos/login_history.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy user_sessions
(session_id, user_id, jwt_token, ip, user_agent, login_at)
FROM '/tmp/pos/user_sessions.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy notifications
(user_id, title, message, severity, category, is_read, created_at)
FROM '/tmp/pos/notifications.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy user_limits
(user_id, transfer_limit_yen, atm_withdraw_limit_yen, updated_at)
FROM '/tmp/pos/user_limits.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy service_requests
(user_id, request_type, status, title, detail, created_at, updated_at)
FROM '/tmp/pos/service_requests.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy cards
(user_id, card_type, masked_number, status, locked, created_at, updated_at)
FROM '/tmp/pos/cards.csv'
WITH (FORMAT csv, HEADER true);
=========================================
SELECT COUNT(*) FROM beneficiaries;
SELECT COUNT(*) FROM login_history;
SELECT COUNT(*) FROM user_sessions;
SELECT COUNT(*) FROM notifications;
SELECT COUNT(*) FROM user_limits;
SELECT COUNT(*) FROM service_requests;
SELECT COUNT(*) FROM cards;



TRUNCATE TABLE
  atm_locations,
  rate_items,
  fee_items,
  fx_rates,
  news_items,
  security_alerts,
  faq_items
RESTART IDENTITY CASCADE;

=========================================
公開情報テーブル CSV取り込み
=========================================
\copy atm_locations
(id, name, pref, city, address, lat, lng, open_now, cash, services, hours, updated_at)
FROM '/tmp/pos/atm_locations.csv'
WITH (FORMAT csv, HEADER true);
=========================================
-- 1) いったん受け皿（制約なし）を作る
DROP TABLE IF EXISTS stg_rate_items;
CREATE TEMP TABLE stg_rate_items (
  category     varchar(50),
  product      varchar(100),
  rate_percent numeric(6,3),
  term         varchar(50),
  note         varchar(200)
);

-- 2) CSVを受け皿へ読み込む
\copy stg_rate_items
(category, product, rate_percent, term, note)
FROM '/tmp/pos/rate_items.csv'
WITH (FORMAT csv, HEADER true);

-- 3) 本番テーブルへ重複排除して投入（必要なら全入替）
TRUNCATE TABLE rate_items RESTART IDENTITY;

INSERT INTO rate_items (category, product, rate_percent, term, note)
SELECT DISTINCT ON (category, product, COALESCE(NULLIF(term,''), '-'))
       category,
       product,
       rate_percent,
       COALESCE(NULLIF(term,''), '-') AS term,
       note
FROM stg_rate_items
ORDER BY category, product, COALESCE(NULLIF(term,''), '-'), rate_percent DESC;


=========================================
\copy fee_items
(service, channel, amount_yen, note)
FROM '/tmp/pos/fee_items.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy fx_rates
(currency, rate_to_jpy)
FROM '/tmp/pos/fx_rates.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy news_items
(id, category, title, summary, body, published_at, updated_at)
FROM '/tmp/pos/news_items.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy security_alerts
(id, title, tag, risk_level, recent_count, tip, updated_at)
FROM '/tmp/pos/security_alerts.csv'
WITH (FORMAT csv, HEADER true);
=========================================
\copy faq_items
(id, category, question, answer, tags)
FROM '/tmp/pos/faq_items.csv'
WITH (FORMAT csv, HEADER true);
=========================================
SELECT COUNT(*) FROM atm_locations;
SELECT COUNT(*) FROM rate_items;
SELECT COUNT(*) FROM fee_items;
SELECT COUNT(*) FROM fx_rates;
SELECT COUNT(*) FROM news_items;
SELECT COUNT(*) FROM security_alerts;
SELECT COUNT(*) FROM faq_items;
