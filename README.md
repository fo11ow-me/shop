#### 项目介绍
  本项目是基于JSP+JavaBean+Servlet+MySQL开发的小型商城，分为前台和后台。
#### 项目基本设置
  1. 将项目克隆到本地之后，找到WebContent下image文件夹的绝对路径（此处image文件夹的存放位置可以不在项目路径下，可以移动此文件夹到电脑上其他路径）
   ![image](https://user-images.githubusercontent.com/58590342/123023687-65589e00-d40a-11eb-8e10-5f6f4233105c.png)
  2. 在项目的web.xml文件中设置image路径
   ![image](https://user-images.githubusercontent.com/58590342/118268882-8ddaa780-b4f0-11eb-9097-bbe94f91bf6d.png)
  3. 将项目部署到tomcat之后，修改server.xml文件，设置项目访问路径
   ![image](https://user-images.githubusercontent.com/58590342/118270412-9df38680-b4f2-11eb-95fa-68857c7d2eaa.png)
  4. 新建数据库shop(数据库名称可以自定义)，运行shop.sql代码
  5. 数据库连接（com.shop.util.ConnectDB）
   ![image](https://user-images.githubusercontent.com/58590342/123020443-d5fcbc00-d404-11eb-9539-2a44ff97709d.png)
  6. 因为上传的商品的图片都是以url形式访问的，并且是访问的8080端口，所以项目要以8080端口启动（如果要改端口，则要将jsp页面代码中的访问商品图片的端口改变）
#### 前台地址
  http://47.98.199.212/shop/GetCategory
#### 后台地址
  http://47.98.199.212/shop/admin_login.jsp
#### 登录账号（前后台均可）
  账号：admin 
  密码：000
#### 项目结构
  ![image](https://user-images.githubusercontent.com/58590342/118215549-44646b00-b4a4-11eb-8caf-50588a2dffba.png)
  ![image](https://user-images.githubusercontent.com/58590342/123022210-0abe4280-d408-11eb-94bc-b696084f95a8.png)
#### 物理数据模型(PDM)
  ![image](https://user-images.githubusercontent.com/58590342/118206322-d3698700-b494-11eb-9338-8e72d6d52632.png)
#### 项目环境（仅供参考，此项目对环境没有太大要求，可以自定义环境）
  1. 服务器：apache-tomcat-9.0.39 
  2. jdbc驱动：mysql-connector-java-8.0.17 
  3. 数据库：mysql-8.0.17
  4. java版本: jdk14.0.1（当时想尝试最新版就用了jdk14，可以使用jdk1.8）
#### 其他
  这个项目是我的第一个项目，前后端模板都是这个视频中的https://www.bilibili.com/video/BV1zE411Y7Mg?share_source=copy_web ，思路是跟着这个老师走的，但数据库和项目结构是自己设计的。
