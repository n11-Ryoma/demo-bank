insert into atm_locations (id, name, pref, city, address, lat, lng, open_now, cash, services, hours, updated_at) values
('atm-001', 'Ichiban Bank Shibuya', 'tokyo', 'Shibuya', '1-1-1 Dogenzaka', 35.6581, 139.7017, true, true, 'withdraw,deposit,transfer', '07:00-23:00', '2026-02-10T09:00:00+09:00'),
('atm-002', 'Ichiban Bank Marunouchi', 'tokyo', 'Chiyoda', '2-4-1 Marunouchi', 35.6812, 139.7671, false, true, 'withdraw,deposit', '08:00-22:00', '2026-02-09T10:30:00+09:00'),
('atm-003', 'Ichiban Bank Umeda', 'osaka', 'Kita', '3-1-1 Umeda', 34.7025, 135.4959, true, false, 'withdraw', '24H', '2026-02-08T14:10:00+09:00'),
('atm-004', 'Ichiban Bank Sapporo', 'hokkaido', 'Chuo', '4-2-1 Kita', 43.0687, 141.3507, true, true, 'withdraw,deposit,loan', '09:00-21:00', '2026-02-07T17:20:00+09:00')
on conflict (id) do nothing;

insert into rate_items (category, product, rate_percent, term, note) values
('deposit', 'Regular Savings', 0.020, '', 'Standard rate'),
('deposit', 'Time Deposit', 0.180, '6 months', 'Campaign until 2026-03-31'),
('deposit', 'Time Deposit', 0.220, '12 months', 'Campaign until 2026-03-31'),
('loan', 'Housing Loan', 1.100, 'Variable', 'Review every 6 months'),
('loan', 'Auto Loan', 1.800, 'Fixed', 'Fixed for full term')
on conflict (category, product, term) do nothing;

insert into fee_items (service, channel, amount_yen, note) values
('transfer', 'online', 110, 'Within bank'),
('transfer', 'online', 220, 'To other banks'),
('transfer', 'branch', 330, 'Counter service'),
('atm', 'other-bank', 220, 'Other bank ATM'),
('card', 'reissue', 1100, 'Card reissue')
on conflict (service, channel, amount_yen) do nothing;

insert into fx_rates (currency, rate_to_jpy) values
('JPY', 1.0),
('USD', 150.25),
('EUR', 162.80),
('GBP', 189.45),
('AUD', 98.70)
on conflict (currency) do nothing;

insert into news_items (id, category, title, summary, body, published_at, updated_at) values
('20260211-001', 'maintenance', 'モバイルアプリ メンテナンスのお知らせ', 'モバイルバンキングアプリの計画メンテナンスを実施します。',
 '2026-02-12 01:00-03:00（JST）に計画メンテナンスを実施します。一部機能がご利用いただけない場合があります。',
 '2026-02-11T09:00:00+09:00', '2026-02-11T09:00:00+09:00'),
('20260210-002', 'announcement', '振込手数料改定のお知らせ', '振込手数料の改定についてご案内します。',
 '2026-03-01よりオンラインチャネルの振込手数料が変更となります。',
 '2026-02-10T15:30:00+09:00', '2026-02-10T15:30:00+09:00'),
('20260209-003', 'outage', 'ATMネットワーク障害 復旧のお知らせ', '一部地域で発生していたATM接続障害が復旧しました。',
 '一部地域で発生していたATMネットワーク障害は復旧しました。ご不便をおかけしました。',
 '2026-02-09T12:15:00+09:00', '2026-02-09T14:00:00+09:00')
on conflict (id) do nothing;

insert into security_alerts (id, title, tag, risk_level, recent_count, tip, updated_at) values
('sec-001', '銀行サポートを装ったフィッシングSMS', 'phishing', 'high', 128,
 '差出人不明のリンクは開かず、公式アプリで確認してください。', '2026-02-10T09:30:00+09:00'),
('sec-002', '融資可決を装う偽の電話', 'voice', 'medium', 64,
 '当行が電話で暗証番号を伺うことはありません。', '2026-02-08T11:20:00+09:00'),
('sec-003', 'アカウントへの不正ログイン試行', 'account', 'high', 92,
 'パスワードの使い回しを避け、MFAを有効にしてください。', '2026-02-09T18:10:00+09:00'),
('sec-004', '不正なQR決済依頼', 'payment', 'medium', 51,
 '支払先情報を確認してから承認してください。', '2026-02-07T16:05:00+09:00')
on conflict (id) do nothing;

insert into faq_items (id, category, question, answer, tags) values
('faq-001', 'account', '住所変更はどうすればいいですか？',
 'プロフィール設定から住所変更が可能です。お近くの店舗でも承ります。', 'address,profile'),
('faq-002', 'transfer', '振込はいつ着金しますか？',
 '国内振込は通常、当日中（締切時刻まで）に着金します。', 'transfer,schedule'),
('faq-003', 'security', 'フィッシングが疑われる場合は？',
 'セキュリティ窓口へご連絡のうえ、パスワードを直ちに変更してください。', 'phishing,security'),
('faq-004', 'loan', 'ローンの繰上返済はできますか？',
 '商品によって条件は異なりますが、繰上返済は可能です。', 'loan,repayment')
on conflict (id) do nothing;
