# some knowledge
## Java RMI (Java Remote Method Invocation)
Java远程方法调用，即Java RMI（Java Remote Method Invocation）是Java编程语言里，一种用于实现**远程过程调用**的应用程序编程接口。它使客***户机上运行的程序可以调用远程服务器上的对象***。远程方法调用特性使Java编程人员能够在网络环境中分布操作。RMI全部的宗旨就是尽可能简化远程接口对象的使用。

Java RMI极大地依赖于接口。在需要创建一个远程对象的时候，程序员通过传递一个接口来隐藏底层的实现细节。客户端得到的远程对象句柄正好与本地的根代码连接，由后者负责透过网络通信。这样一来，程序员只需关心如何通过自己的接口句柄发送消息。

接口的两种常见实现方式是：最初使用JRMP（Java Remote Message Protocol，Java远程消息交换协议）实现；此外还可以用与CORBA兼容的方法实现。RMI一般指的是编程接口，也有时候同时包括JRMP和API（应用程序编程接口），而RMI-IIOP则一般指RMI接口接管绝大部分的功能，以支持CORBA的实现。

最初的RMI API设计为通用地支持不同形式的接口实现。后来，CORBA增加了传值（pass by value）功能，以实现RMI接口。然而RMI-IIOP和JRMP实现的接口并不完全一致。

所使用Java包的名字是java.rmi。

## 为什么要删掉RMXXX
在原有的代码基础上，Java rmi registry 的注册地址是 ip:port/RMIName。所以我们并不需 要为每一个 RM，TM，WC分配一个端口。只要使用 RMIName 来区分各个服务即可，所以 相应的接口配置文件。同时因为所有的 RMs 使用相同的实现，再为每一个 RM 写一个启动 类，略显多余，所以我们删除了多余的启动类。

## client 类中 为什么要实现 thread
在利用script进行test.part2的并发测试时，需要这个类。eg：
``` java
private static final String SCRIPTDIR = "scripts/";

proc = Runtime.getRuntime().exec(new String[]{
        "sh",
        "-c",
        "java -classpath .. -DrmiPort=" +
                System.getProperty("rmiPort") +
                " -Djava.security.policy=./security-policy transaction.Client <" +
                SCRIPTDIR + id +
                " >" + LOGDIR + id + OUTSUFFIX +
                " 2>" + LOGDIR + id + ERRSUFFIX});

```

## System.getProperty("rmiPort")
这个是通过 ``java 命令行控制``。java -classpath .. -DrmiPort=3345 -DtestName=AddChange test.RunTest