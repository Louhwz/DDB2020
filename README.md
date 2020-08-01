# DDB 2020
## Usage
### run server
进入到项目根目录下，运行：
```
$ ./run_server.sh
```

输出如下：
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
Warning: rmic has been deprecated and is subject to removal in a future        
release. Generation and use of skeletons and static stubs for JRMP      
is deprecated. Skeletons are unnecessary, and static stubs have 
been superseded by dynamically generated stubs. Users are encouraged    
to migrate away from using this tool to generate skeletons and static   
stubs. See the documentation for java.rmi.server.UnicastRemoteObject.
/usr/bin/javac -classpath .. TransactionManagerImpl.java
/usr/bin/rmic -classpath .. -d .. transaction.TransactionManagerImpl
Warning: rmic has been deprecated and is subject to removal in a future        
release. Generation and use of skeletons and static stubs for JRMP      
is deprecated. Skeletons are unnecessary, and static stubs have 
been superseded by dynamically generated stubs. Users are encouraged    
to migrate away from using this tool to generate skeletons and static   
stubs. See the documentation for java.rmi.server.UnicastRemoteObject.
/usr/bin/javac -classpath .. WorkflowControllerImpl.java
/usr/bin/rmic -classpath .. -d .. transaction.WorkflowControllerImpl
Warning: rmic has been deprecated and is subject to removal in a future        
release. Generation and use of skeletons and static stubs for JRMP      
is deprecated. Skeletons are unnecessary, and static stubs have 
been superseded by dynamically generated stubs. Users are encouraged    
to migrate away from using this tool to generate skeletons and static   
stubs. See the documentation for java.rmi.server.UnicastRemoteObject.
/usr/bin/rmiregistry -J-classpath -J.. 3345                                                                                                                                                                                          
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

### 单独运行client
```
$ cd src
$ javac transaction/Client.java
$ java transaction.Client
```

### 运行 test
在项目根目录下
#### 测试全部例子
```
$ ./run_test.sh
```

### 测试单例子
```
$ ./run_test.sh <例子的类名>
```
具体的例子的类名及其测试的含义，请参考下表：
[TODO]

### 终止 server 进程
```
$ ./stop_server.sh
```