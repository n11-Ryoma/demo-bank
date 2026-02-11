create table if not exists atm_locations (
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

create table if not exists rate_items (
    id serial primary key,
    category varchar(50) not null,
    product varchar(100) not null,
    rate_percent numeric(6,3) not null,
    term varchar(50) not null,
    note varchar(200),
    unique (category, product, term)
);

create table if not exists fee_items (
    id serial primary key,
    service varchar(50) not null,
    channel varchar(50) not null,
    amount_yen int not null,
    note varchar(200),
    unique (service, channel, amount_yen)
);

create table if not exists fx_rates (
    currency varchar(10) primary key,
    rate_to_jpy numeric(12,6) not null
);

create table if not exists news_items (
    id varchar(32) primary key,
    category varchar(50) not null,
    title varchar(200) not null,
    summary varchar(500) not null,
    body text not null,
    published_at timestamptz not null,
    updated_at timestamptz not null
);

create table if not exists security_alerts (
    id varchar(32) primary key,
    title varchar(200) not null,
    tag varchar(50) not null,
    risk_level varchar(20) not null,
    recent_count int not null,
    tip varchar(500) not null,
    updated_at timestamptz not null
);

create table if not exists faq_items (
    id varchar(32) primary key,
    category varchar(50) not null,
    question varchar(300) not null,
    answer text not null,
    tags varchar(200)
);

-- Future post-login feature tables (can be used when switching from in-memory repositories)
create table if not exists beneficiaries (
    id bigserial primary key,
    user_id bigint not null,
    bank_name varchar(120) not null,
    branch_name varchar(120),
    account_type varchar(30) not null,
    account_number varchar(20) not null,
    account_holder_name varchar(120) not null,
    nickname varchar(120),
    created_at timestamptz not null default now()
);

create table if not exists login_history (
    id bigserial primary key,
    user_id bigint not null,
    result varchar(20) not null,
    ip varchar(64) not null,
    user_agent varchar(300) not null,
    occurred_at timestamptz not null default now()
);

create table if not exists user_sessions (
    session_id varchar(64) primary key,
    user_id bigint not null,
    jwt_token varchar(2000) not null,
    ip varchar(64) not null,
    user_agent varchar(300) not null,
    login_at timestamptz not null default now()
);

create table if not exists notifications (
    id bigserial primary key,
    user_id bigint not null,
    title varchar(200) not null,
    message varchar(1000) not null,
    severity varchar(20) not null,
    category varchar(50) not null,
    is_read boolean not null default false,
    created_at timestamptz not null default now()
);

create table if not exists user_limits (
    user_id bigint primary key,
    transfer_limit_yen bigint not null,
    atm_withdraw_limit_yen bigint not null,
    updated_at timestamptz not null default now()
);

create table if not exists service_requests (
    id bigserial primary key,
    user_id bigint not null,
    request_type varchar(50) not null,
    status varchar(30) not null,
    title varchar(200) not null,
    detail varchar(2000),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists cards (
    id bigserial primary key,
    user_id bigint not null,
    card_type varchar(30) not null,
    masked_number varchar(30) not null,
    status varchar(30) not null,
    locked boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
