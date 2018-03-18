#项目环境
===========================================================
windows + tomcat8.0.9 + jdk1.8.0 + maven +mysql + idea + nginx（负载均衡服务器） + ftpserver（图片服务器） + 支付宝沙箱


#运行前配置
======================================================
1.	保证在已经安装jdk，maven，tomcat，mysql等的环境并配置好

2.	导入"项目环境配置文件"目录的sql初始化文件mmall.sql到mysql

3.	解压缩源码之后，使用eclipse或者idea导入maven项目

4.	然后配置以下文件：

#ftpserver：
ftpserver 的共享目录设为 D:\ftpserver\ftpfile（当然你也可以根据喜好更改）

#nginx:
（nginx的安装过程及环境配置就不说啦）

1.打开你C:\Windows\System32\drivers\etc目录下的hosts文件，在最下面加入

127.0.0.1   img.xiaochai.com
127.0.0.1   www.xiaochai.com

这两行字符。作用就是当你在浏览器输入以上两个域名时，打开的是你本地地址（127.0.0.1）。

2.打开nginx的conf目录下nginx.conf文件，在hrrp{}里面加入 include vhost/*.conf; 这一行字符（自定义的自己的服务相应配置），当然也可以拷贝"项目环境配置文件"目录下提供的参考文件


3.在nginx的conf目录下创建一个vhost文件夹，"项目环境配置文件"目录的img.xiaochai.com.conf拷贝到vhost里


#支付宝沙箱
项目resources下的zfbinfo.properties文件就是支付宝的配置文件，根据官网提供的demo修改的

具体配置去阿里那看开发文档
https://docs.open.alipay.com/200/105311/
当然不止这一个文档，根据具体情况翻阅其他文档

#mysql:
项目resources下的datasource.properties文件就是数据库配置文件，打开修改你自己的有关信息

#项目配置:
打开项目resources下的mmall.properties，修改成自己的ftp服务器地址，账号和密码，ftp文件服务器的访问前缀，支付宝回调的地址。MD5的salt值非常不建议修改。否则账号就登录不进去啦，还需要重置。


5.	管理员账号：admin 管理员密码：admin

6.	最后部署tomcat运行就可以了。





