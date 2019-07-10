# EurekaServer 高可用的注册中心集群搭建

>Eureka是Netflix开发的服务发现框架，本身是一个基于REST的服务，主要用于定位运行在AWS域中的中间层服务，以达到负载均衡和中间层服务故障转移的目的。SpringCloud将它集成在其子项目spring-cloud-netflix中，以实现SpringCloud的服务发现功能。 

> Eureka包含两个组件：Eureka Server和Eureka Client。

> Eureka Server提供服务注册服务，各个节点启动后，会在Eureka Server中进行注册，这样EurekaServer中的服务注册表中将会存储所有可用服务节点的信息，服务节点的信息可以在界面中直观的看到。  

> Eureka Client是一个java客户端，用于简化与Eureka Server的交互，客户端同时也就是一个内置的、使用轮询(round-robin)负载算法的负载均衡器。 

> 在应用启动后，将会向Eureka Server发送心跳,默认周期为30秒，如果Eureka Server在多个心跳周期内没有接收到某个节点的心跳，Eureka Server将会从服务注册表中把这个服务节点移除(默认90秒)。

> Eureka Server之间通过复制的方式完成数据的同步，Eureka还提供了客户端缓存机制，即使所有的Eureka Server都挂掉，客户端依然可以利用缓存中的信息消费其他服务的API。综上，Eureka通过心跳检查、客户端缓存等机制，确保了系统的高可用性、灵活性和可伸缩性。
>
> **以上是 Eureka 的简单介绍，接下来我们只用服务端做一个注册中心集群**

1. 准备三台主机

   ```java
   //我们打开 C:\Windows\System32\drivers\etc\hosts 文件
   //模拟三台主机，实际上使用的是我们自己的本地机器
   127.0.0.1 com.one
   127.0.0.1 com.two
   127.0.0.1 com.three
   ```

2. 创建一个 spring boot 的 maven 项目(声明 spring cloud 项目并规定了各组件的版本)

   ```xml
   <!-- 引入pom文件 -->
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>
   
       <groupId>com.eureka</groupId>
       <artifactId>eureka-servers</artifactId>
       <packaging>pom</packaging>
       <version>1.0-SNAPSHOT</version>
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-parent</artifactId>
           <version>2.0.3.RELEASE</version>
       </parent>
   
       <dependencyManagement>
           <dependencies>
               <dependency>
                   <groupId>org.springframework.cloud</groupId>
                   <artifactId>spring-cloud-dependencies</artifactId>
                   <version>Finchley.SR2</version>
                   <scope>import</scope>
                   <type>pom</type>
               </dependency>
           </dependencies>
       </dependencyManagement>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-web</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-test</artifactId>
           </dependency>
       </dependencies>
   
   </project>
   ```

3. 创建第一个服务端子项目 eureka-server1

   引入pom文件

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>eureka-servers</artifactId>
           <groupId>com.eureka</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>eureka-server1</artifactId>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
           </dependency>
       </dependencies>
   
   </project>
   ```

   配置文件

   ```properties
   #服务的端口号
   server.port=1001
   #服务的名字
   spring.application.name=EUREKA-SERVER
   
   #配置数据复制的节点
   eureka.client.service-url.defaultZone= http://com.one:1001/eureka,http://com.two:1002/eureka,http://com.three:1003/eureka
   #关闭自我保护
   eureka.server.enable-self-preservation=false
   #注册自己到Eureka注册中心（方便在视图中观察）
   eureka.client.register-with-eureka=true
   #配置不获取注册信息
   eureka.client.fetch-registry=false
   ```

   创建启动类

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
   
   /**
    * @ClassName EurekaServerStart1
    * @Description: TODO
    * @Author 小松
    * @Date 2019/7/8
    **/
   @SpringBootApplication
   @EnableEurekaServer
   public class EurekaServerStart1 {
       public static void main(String[] args) {
           SpringApplication.run(EurekaServerStart1.class,args);
       }
   }
   ```

4. 创建第二个服务端子项目

   引入pom文件

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>eureka-servers</artifactId>
           <groupId>com.eureka</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>eureka-server2</artifactId>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
           </dependency>
       </dependencies>
   
   </project>
   ```

   配置文件

   ```properties
   #服务的端口号
   server.port=1002
   #服务的名字
   spring.application.name=EUREKA-SERVER
   
   #配置数据复制的节点
   eureka.client.service-url.defaultZone= http://com.one:1001/eureka,http://com.two:1002/eureka,http://com.three:1003/eureka
   #关闭自我保护
   eureka.server.enable-self-preservation=false
   #注册自己到Eureka注册中心（方便在视图中观察）
   eureka.client.register-with-eureka=true
   #配置不获取注册信息
   eureka.client.fetch-registry=false
   ```

   创建启动类

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
   
   /**
    * @ClassName EurekaServerStart2
    * @Description: TODO
    * @Author 小松
    * @Date 2019/7/8
    **/
   @SpringBootApplication
   @EnableEurekaServer
   public class EurekaServerStart2 {
       public static void main(String[] args) {
           SpringApplication.run(EurekaServerStart2.class,args);
       }
   }
   ```

5. 创建第三个

   引入pom文件

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <parent>
           <artifactId>eureka-servers</artifactId>
           <groupId>com.eureka</groupId>
           <version>1.0-SNAPSHOT</version>
       </parent>
       <modelVersion>4.0.0</modelVersion>
   
       <artifactId>eureka-server3</artifactId>
   
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
           </dependency>
       </dependencies>
   
   </project>
   ```

   配置文件

   ```properties
   #服务的端口号
   server.port=1003
   #服务的名字
   spring.application.name=EUREKA-SERVER
   
   #配置数据复制的节点
   eureka.client.service-url.defaultZone= http://com.one:1001/eureka,http://com.two:1002/eureka,http://com.three:1003/eureka
   #关闭自我保护
   eureka.server.enable-self-preservation=false
   #注册自己到Eureka注册中心（方便在视图中观察）
   eureka.client.register-with-eureka=true
   #配置不获取注册信息
   eureka.client.fetch-registry=false
   ```

   创建启动类

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
   
   /**
    * @ClassName EurekaServerStart3
    * @Description: TODO
    * @Author 小松
    * @Date 2019/7/8
    **/
   @SpringBootApplication
   @EnableEurekaServer
   public class EurekaServerStart3 {
       public static void main(String[] args) {
           SpringApplication.run(EurekaServerStart3.class,args);
       }
   }
   ```

6. 依次运行三个启动类

   访问 http://com.one:1001 显示如下：	![在这里插入图片描述](https://img-blog.csdnimg.cn/20190708171404782.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzY1MDI1NA==,size_16,color_FFFFFF,t_70)

   访问 http://com.two:1002 显示如下：  	![在这里插入图片描述](https://img-blog.csdnimg.cn/2019070817141879.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzY1MDI1NA==,size_16,color_FFFFFF,t_70)

   访问 http://com.three:1003 显示如下：   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190708171445972.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzY1MDI1NA==,size_16,color_FFFFFF,t_70)

   可以看到每个 eureka 注册中心都可以看到其他的注册中心

7. **至此，EurekaServer 高可用的注册中心集群搭建完成**

博客链接：https://blog.csdn.net/weixin_43650254/article/details/95075098

