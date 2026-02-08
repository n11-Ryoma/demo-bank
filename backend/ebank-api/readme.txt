0)git
git clone https://github.com/n11-Ryoma/demo-bank.git
cd ~/demo-bank/backend/ebank-api
vi src/main/resources/application.properties
application.properties
をDBのIPに書き換える。
1) 専用ユーザ作成
sudo useradd -r -s /usr/sbin/nologin -d /opt/ebank-api ebank
sudo mkdir -p /opt/ebank-api/app /opt/ebank-api/logs /opt/ebank-api/config
sudo chown -R ebank:ebank /opt/ebank-api

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
sudo cp target/ebank-api-0.0.1-SNAPSHOT.jar /opt/ebank-api/app/ebank-api.jar
sudo chown ebank:ebank /opt/ebank-api/app/ebank-api.jar

=========================================================
5) systemd サービス作成
sudo nano /etc/systemd/system/ebank-api.service


eshop-api.service

[Unit]
Description=ebank api
After=network.target

[Service]
User=ebank
Group=ebank
WorkingDirectory=/opt/ebank-api/app
ExecStart=/usr/bin/java -jar /opt/ebank-api/app/ebank-api.jar
Environment=JAVA_OPTS=-Xms256m -Xmx512m
Environment=SPRING_PROFILES_ACTIVE=prod
Restart=always
RestartSec=5
StandardOutput=append:/opt/ebank-api/logs/app.out.log
StandardError=append:/opt/ebank-api/logs/app.err.log

[Install]
WantedBy=multi-user.target



反映:
sudo systemctl daemon-reload
sudo systemctl enable ebank-api
sudo systemctl start ebank-api
sudo systemctl status ebank-api

6) よく使う運用コマンド
sudo systemctl restart ebank-api
sudo journalctl -u ebank-api -f

