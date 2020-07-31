* Docker启动：

  ```
  docker pull ubuntu:18.04
  docker run -itd --name ddb -v /f/School/DDB2020:/home/DDB ubuntu:18.04
  docker exec -it ddb /bin/bash
  
  apt-get update
  y | apt-get install openjdk-8-jdk
  apt-get update
  apt-get install make
  ```

  

* 使用了SecurityManager对程序进行权限控制，需要在启动的时候在VM options中添加-Djava.security.policy=src\transaction\security-policy选项。

* 原项目使用makefile进行管理，配置了security policy和端口号。如果想跑通，要启动的文件顺序可以参照makefile。

* 参考makefile中runregistry，还需要实现本地的一个registry