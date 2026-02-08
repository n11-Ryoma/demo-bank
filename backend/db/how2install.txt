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

