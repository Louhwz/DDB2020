# 1. DDB 2020
成员：组长——周孟莹 19110240001，组员：方睿钰 19210240002、章苏尧 19212010032

# 2. Outline
- [1. DDB 2020](#1-ddb-2020)
- [2. Outline](#2-outline)
- [3. 环境配置](#3-环境配置)
- [4. 文件目录](#4-文件目录)
- [5. 运行系统](#5-运行系统)
  - [5.1. 运行系统server](#51-运行系统server)
  - [5.2. 运行client](#52-运行client)
  - [5.3. 终止运行](#53-终止运行)
- [6. 测试](#6-测试)
- [7. 其他](#7-其他)

# 3. 环境配置

系统：Ubuntu 18.04
环境：java v1.8+
配置方法：apt install openjdk-8-jdk-headless(headless适用Ubuntu server，若不是server可不加)

若为Windows系统，可使用docker配置

  ```
  docker pull ubuntu:18.04
  docker run -itd --name ddb -v /f/School/DDB2020:/home/DDB ubuntu:18.04
  docker exec -it ddb /bin/bash

  apt-get update
  y | apt-get install openjdk-8-jdk
  apt-get update
  apt-get install make
  ```

# 4. 文件目录

- src
    - lockmgr: 已提供的实现好的锁管理器
    - test: Java编写实现的测试用例
        - data: 运行生成的数据文件夹
        - TestFileObject.java: [TODO]
        - TestManager.java: 测试主程序
        - Makefile
        - 其它Java程序: 测试用例
    - transaction: 主要实现部分
        - data: 运行生成的数据文件夹
        - exceptions: 自定义的可能异常
        - models: 数据实体定义，包括接口 `ResourceItem` 与类 `Car`, `Customer`, `Flight`, `Hotel`, `Reservation`, `ReservationKey`
        - Client.java: 脚本测试时调用的客户端
        - MyClient.java: 默认简单客户端
        - ResourceManager.java: `RM` 接口
        - ResourceManagerImpl.java: `RM` 接口实现
        - RMTable.java: 数据表
        - TransactionManager.java: `TM`接口（主要修改文件）
        - TransactionManagerImpl.java: `TM`接口实现（主要修改文件）
        - Utils.java: 工具类，用于简化代码
        - WorkflowController.java: `WC` 接口
        - WorkflowControllerImpl.java: `WC` 接口实现（主要修改文件）
        - Makefile
- doc
    - [测试用例说明](doc/测试用例说明.md)
    - [项目分工说明](doc/项目分工说明.md)
    - [项目技术报告-周孟莹](doc/项目技术报告-周孟莹.pdf)
    - [项目技术报告-方睿钰](doc/项目技术报告-方睿钰.pdf)
    - [项目技术报告-章苏尧](doc/项目技术报告-章苏尧.pdf)
- README.md
- run_server.sh
- run_test.sh
- stop_server.sh
- .gitignore


# 5. 运行系统
## 5.1. 运行系统server
在测试前需要运行整个预定系统服务，根目录中的 run_server.sh 脚本文件可一次运行整个服务。

_注：server需要一直保持后台运行，请开多个窗口，运行成功后不要关闭该窗口。若不方便，可使用screen等工具帮助管理_

- 进入到项目根目录下，运行
```
$ ./run_server.sh
```

- 参考输出如下，以下输出结束后，预定系统的服务正式运行成功。这时可切换到另一个命令窗口进行测试
```
rm -f *.class
javac -classpath .. DataObj.java
javac -classpath .. DeadlockException.java
javac -classpath .. LockManager.java
Note: ../lockmgr/TPHashTable.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
/bin/rm -rf *.class
/bin/rm -rf */*.class
/usr/bin/javac -classpath .. ResourceManager.java
/usr/bin/javac -classpath .. ResourceManagerImpl.java
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
/usr/bin/rmic -classpath .. -d .. transaction.ResourceManagerImpl
to migrate away from using this tool to generate skeletons and static
stubs. See the documentation for java.rmi.server.UnicastRemoteObject.
/usr/bin/javac -classpath .. TransactionManagerImpl.java
/usr/bin/rmic -classpath .. -d .. transaction.TransactionManagerImpl
/usr/bin/javac -classpath .. WorkflowControllerImpl.java
/usr/bin/rmic -classpath .. -d .. transaction.WorkflowControllerImpl
/usr/bin/java -classpath .. -DrmiPort=3345 -DrmiName=RMFlights -Djava.security.policy=./security-policy transaction.ResourceManagerImpl
/usr/bin/java -classpath .. -DrmiPort=3345 -Djava.security.policy=./security-policy transaction.WorkflowControllerImpl
/usr/bin/java -classpath .. -DrmiPort=3345 -DrmiName=RMRooms -Djava.security.policy=./security-policy transaction.ResourceManagerImpl
/usr/bin/java -classpath .. -DrmiPort=3345 -DrmiName=RMCustomers -Djava.security.policy=./security-policy transaction.ResourceManagerImpl
/usr/bin/java -classpath .. -DrmiPort=3345 -Djava.security.policy=./security-policy transaction.TransactionManagerImpl
/usr/bin/java -classpath .. -DrmiPort=3345 -DrmiName=RMCars -Djava.security.policy=./security-policy transaction.ResourceManagerImpl
RMCustomers's xids is Empty ? true
RMCustomers bound to TM
WC cannot bind to some component:java.rmi.NotBoundException: RMFlights
RMFlights's xids is Empty ? true
RMFlights bound to TM
RMCars's xids is Empty ? true
RMCars bound to TM
RMRooms's xids is Empty ? true
RMRooms bound to TM
RMCustomersbound!
RMCarsbound!
RMRoomsbound!
RMFlightsbound!
WC bound to RMFlights
WC bound to RMRooms
WC bound to RMCars
WC bound to RMCustomers
WC bound to TM
RMFlights's xids is Empty ? true
RMFlights bound to TM
RMRooms's xids is Empty ? true
RMRooms bound to TM
RMCars's xids is Empty ? true
RMCars bound to TM
RMCustomers's xids is Empty ? true
RMCustomers bound to TM
WC bound
```



## 5.2. 运行client
运行client文件可帮助判断系统是否正常运行，client中包含了一个简单的使用用例。
```
$ cd src
$ javac transaction/Client.java
$ java transaction.Client
```

## 5.3. 终止运行
在测试结束后，可利用 stop_server.sh 终止运行系统。
```
$ ./stop_server.sh
```

# 6. 测试
测试用例存储在`src/test/`中，在根目录中的 run_test.sh 可以帮助运行测试用例。
```
$ ./run_test.sh
```

具体测试用例的类名及其测试目的，可参考文件 `测试用例说明.pdf`


# 7. 其他
* 如果修改代码后，run_server.sh 脚本启动失败，想重新启动，保险起见，最好运行以下命令（避免脚本中已经启动的部分进程未被杀死）

  ```
  kill $(ps -ef | grep make | awk '{print $2}')
  kill $(ps -ef | grep java | awk '{print $2}')
  ```

