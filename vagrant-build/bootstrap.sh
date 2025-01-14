#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive

echo
echo "Install dependencies"
echo
echo "  * apt-get update"
apt-get update
echo "  * apt-get install wget unzip vim-common dos2unix git htop"
apt-get install -y wget unzip vim-common dos2unix git htop
echo "  * apt-get install mariadb-client mariadb-server"
apt-get install -y mariadb-client mariadb-server
echo "  * apt-get install openjdk-11-jdk maven"
apt-get install -y openjdk-11-jdk maven

echo
echo "Set up GEMP directory structure"
echo "  * mkdir -p /env/gemp-swccg/web"
mkdir -p /env/gemp-swccg/web
echo "  * chown /env/gemp-swccg"
chown -R vagrant:vagrant /env/gemp-swccg
echo "  * mkdir /logs"
mkdir /logs/
echo "  * chown /logs"
chown vagrant:vagrant /logs

echo
echo "Setting CWD To /vagrant in bash_profile"
echo
echo "cd /vagrant" >> ~vagrant/.bash_profile


echo
echo "Fix windows line endings if necessary at login"
echo
echo 'dos2unix /vagrant/get-card-images.sh 1>/dev/null 2>&1' >> ~vagrant/.bash_profile
echo 'dos2unix /vagrant/run-gemp.sh 1>/dev/null 2>&1' >> ~vagrant/.bash_profile
echo
source ~vagrant/.bash_profile

echo
echo "Configure MariaDB"
echo "  * set listening host to 0.0.0.0"
sed -i 's/127.0.0.1/0.0.0.0/g' /etc/mysql/mariadb.conf.d/50-server.cnf
echo "  * enable MariaDB service"
systemctl enable --now mariadb.service

echo "  * Load seed database"
mysql -u root mysql <<< "CREATE USER 'gemp'@'localhost' IDENTIFIED BY 'gemp';"
mysql -u root mysql <<< "GRANT ALL PRIVILEGES ON *.* TO 'gemp'@'localhost' WITH GRANT OPTION;"
mysql -u root mysql < /vagrant/database_script.sql

echo "  * Add test users"
mysql -u root gemp-swccg <<< "
INSERT INTO player (name, password, type, last_login_reward, last_ip, create_ip)
VALUES (
	'test1',
	'9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
	'aut',
	'20170101',
	'192.168.50.1',
	'192.168.50.1'
),
(
	'test2',
	'9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
	'aut',
	'20170101',
	'192.168.50.1',
	'192.168.50.1'
);"

echo
echo "Download card images to /vagrant from holotable repo"
echo
cd /vagrant
bash ./get-card-images.sh

echo
echo 'Done!'
echo