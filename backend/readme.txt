0)git
git clone https://github.com/n11-Ryoma/demo-bank.git
cd demo-bank/backend/ebank-api
vi src/main/resources/application.properties
application.properties
をDBのIPに書き換える。
1) 専用ユーザ作成
sudo useradd -r -s /usr/sbin/nologin -d /opt/eshop-api eshop
sudo mkdir -p /opt/eshop-api/app /opt/eshop-api/logs /opt/eshop-api/config
sudo chown -R eshop:eshop /opt/eshop-api
=========================================================
2) OpenJDK 17 インストール
実行のみなら JRE:
sudo apt update
sudo apt install -y openjdk-17-jre-headless
java -version
=========================================================
本番サーバでビルドもする場合は JDK:
sudo apt install -y openjdk-17-jdk
=========================================================
本番でビルドする場合（リポジトリ直下で）:
cd ~/demo-bank/backend/ebank-api
chmod +x mvnw
# BOMが付いてるJavaファイルを探す
grep -rl $'^\xEF\xBB\xBF' src/main/java

# 見つかったやつ全部からBOMを削除（GNU sed）
grep -rl $'^\xEF\xBB\xBF' src/main/java | xargs -r sed -i '1s/^\xEF\xBB\xBF//'


./mvnw -DskipTests package
生成物:
target/eshop-api-0.0.1-SNAPSHOT.jar
=========================================================
4) 配置
sudo cp target/eshop-api-0.0.1-SNAPSHOT.jar /opt/eshop-api/app/eshop-api.jar
sudo chown eshop:eshop /opt/eshop-api/app/eshop-api.jar
=========================================================
5) systemd サービス作成
sudo nano /etc/systemd/system/eshop-api.service

eshop-api.service

[Unit]
Description=eshop api
After=network.target

[Service]
User=eshop
Group=eshop
WorkingDirectory=/opt/eshop-api/app
ExecStart=/usr/bin/java -jar /opt/eshop-api/app/eshop-api.jar
Environment=JAVA_OPTS=-Xms256m -Xmx512m
Environment=SPRING_PROFILES_ACTIVE=prod
Restart=always
RestartSec=5
StandardOutput=append:/opt/eshop-api/logs/app.out.log
StandardError=append:/opt/eshop-api/logs/app.err.log

[Install]
WantedBy=multi-user.target


反映:
sudo systemctl daemon-reload
sudo systemctl enable eshop-api
sudo systemctl start eshop-api
sudo systemctl status eshop-api

6) よく使う運用コマンド
sudo systemctl restart eshop-api
sudo journalctl -u eshop-api -f
