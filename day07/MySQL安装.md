MySQL安装

my.ini

```ini
[client]
port=3306
default-character-set=utf8
 
[mysqld] 
# 设置为自己MYSQL的安装目录 
basedir=D:\Mysql\mysql-5.7.23-winx64
# 设置为MYSQL的数据目录 
datadir=D:\Mysql\mysql-5.7.23-winx64\data
port=3306
character_set_server=utf8
sql_mode=NO_ENGINE_SUBSTITUTION,NO_AUTO_CREATE_USER
#开启查询缓存
explicit_defaults_for_timestamp=true
skip-grant-tables
```

修改环境变量

在path中添加MySQL的bin目录

SQLyog 
Name: any
key: dd987f34-f358-4894-bd0f-21f3f04be9c1

```shell
mysqld --initialize --console
mysqld --install mysql
net start mysql
mysql -uroot -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '111111';
```

