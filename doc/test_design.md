- [1. 组件启动 (无测试)](#1-组件启动-无测试)
- [2. 组件启动功能测试 Basic*.java](#2-组件启动功能测试-basicjava)
  - [2.1. 正常运行测试](#21-正常运行测试)
  - [2.2. 异常测试](#22-异常测试)
- [3. 基本业务逻辑测试](#3-基本业务逻辑测试)
  - [3.1. 正常运行测试](#31-正常运行测试)
  - [3.2. 异常测试](#32-异常测试)
- [4. ACID性质测试](#4-acid性质测试)
  - [4.1. TM 宕机](#41-tm-宕机)
  - [4.2. RM 宕机](#42-rm-宕机)
  - [4.3. WC 宕机](#43-wc-宕机)
- [5. 并行锁测试](#5-并行锁测试)
  - [5.1. 2 基本业务逻辑测试](#51-2-基本业务逻辑测试)
  - [5.2. 3 并发测试，锁功能测试](#52-3-并发测试锁功能测试)
  - [5.3. 4 宕机测试](#53-4-宕机测试)


# 1. 组件启动 (无测试)
1. run_server.sh


# 2. 组件启动功能测试 Basic*.java
## 2.1. 正常运行测试
- [x] BasicBind.java
  - 绑定WC
- [x] BasicCommit.java
  - 绑定WC后，提交一个空commit
- [x] BasicAbort.java
  - 绑定WC后，放弃WC
## 2.2. 异常测试
- BasicXid.java
  - 绑定WC后，使用错误的Xid进行commit，捕捉异常


# 3. 基本业务逻辑测试 CRUD*.java
主要为CRUD（create增, read读, update改, delete删）业务的独立以及组合测试。数据操作对象为Flight, Room, Car 以及 Customer。可以添加修改删除Flight, Room, Car, Customer。用户可以预订或者取消预订Flight, Room, Car。具体包括以下测试用例：
## 3.1. 正常运行测试
- [x] CRUDCreate.java
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car

- [x] CRUDRead.java
  - **分为2个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 查询创建的数据，并进行校验

- [x] CRUDUpdate.java
  - **分为4个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 查询创建的数据，并进行校验
  - 修改刚刚增加的Flight，Room，Car 和 Customer的信息
  - 查询更新后的数据，并进行校验

- CRUDDelete.java
  - **分为3个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 删除刚刚增加的Customer及其reservation，Flight，Room，Car的数据，**注意顺序**
  - 查询删除后的数据，并进行校验


## 3.2. 异常测试
- CRUDCreateFailParam.java
  - **此测试为创建失败，在删除阶段因操作而请求失败。分为2个阶段**
  - 增加Flight，Room，Car 和 Customer，（**注意增加的参数**）。
    - **增加数量为负数**：增加 -1 个 flight
    - **增加价格为负数**：增加 -1 个 room
    - **增加数量和价格都为负数**：增加-1个，价格为-1的car 
    - **增加的名字为null**：增加名字为 null 的customer
    - 操作异常，创建无效。

- CRUDCreateFailInvalidItem.java
  - **此测试为创建失败，在删除阶段因操作而请求失败。分为2个阶段**
  - 增加Flight 和 Customer。
    - **Customer预订的Flight的参数有误**：预订 flight2，而flight2不存在
    - 操作异常，创建无效。

- [x] CRUDDeleteFailSeq.java
  - **此测试为删除失败，在删除阶段因操作而请求失败。分为3个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 删除刚刚增加的Flight，Room，Car 和 Customer的信息（**注意顺序**）。
    - 删除有Customer预订的Flight，Room，Car，删除失败。
    - 操作异常，删除无效。
  - 查询数据，并进行校验
  
- CRUDDeleteFailParam.java
  - **此测试为删除失败，在删除阶段因操作而请求失败。分为3个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 删除刚刚增加的Flight，Room，Car 和 Customer的信息（**注意删除的参数**）。
    - **删除数量为负数**：删除 -1 个 room
    - **删除数量超过总数**：删除 10000000 辆 car
    - **删除的用户不存在**：删除 customer2 
    - 操作异常，删除无效。
  - 查询删除后的数据，并进行校验



# 4. ACID性质测试 ACID*.java
ACID这四个性质的目的就是为了保证系统能在异常的出现的情况下，系统能在这些规则条件下保持数据的稳定
## 4.1. TM 宕机
- ACIDDieTMBeforeCommit.java
  - 测试 Atomicity
  - **分为3个阶段**
  - 增加Flight
  - 测试提交，但在commit之前，TM宕机
  - 重新启动TM，并检查数据：Commit 无效，事务失败

- dieTMAfterCommit.java
  - 测试 Atomicity & Durability
  - **分为3个阶段**
  - 增加Flight
  - 测试提交，但在commit之后，TM宕机
  - 重新启动TM，并检查数据：已成功发送prepared 消息至TM，事务成功

- ACIDDieTM.java
  - 测试 Atomicity
  - **分为3个阶段**
  - 增加Flight
  - TM宕机
  - 重新启动TM，并检查数据：Flight 事务失败



## 4.2. RM 宕机
- ACIDDieRM.java
  - 测试 Atomicity & Consistency
  - **分为4个阶段**
  - 增加Flight1 并commit成功
  - 增加Flight2 但同时 RMFlights 宕机
  - 重启RMFlights，并提交commit
  - 读取数据并进行检查：flight1 成功，flight2 失败

- ACIDDieRMAfterEnlist.java
  - 测试 Atomicity & Consistency
  - **分为4个阶段**
  - 增加Flight 和 Customer，并commit成功
  - customer 预订 flight，但在 enlist 之后 RMFlights 宕机
  - 重启RMFlights，并提交commit
  - 读取数据并进行检查：reservate 失败

- ACIDDieRMBeforePrepare.java
  - 测试 Atomicity
  - **分为4个阶段**
  - 增加Flight
  - 在 prepare 之前 RMFlights 宕机
  - 重启RMFlights
  - 读取数据并进行检查：Flight 事务失败

- ACIDDieRMAfterPrepare.java
  - 测试 Atomicity
  - **分为4个阶段**
  - 增加Flight
  - 在 prepare 之后 RMFlights 宕机
  - 重启RMFlights
  - 读取数据并进行检查：未成功发送prepared 消息至TM, 事务失败

- ACIDDieRMBeforeCommit.java
  - 测试 Atomicity
  - **分为4个阶段**
  - 增加Flight
  - 在 commit 之前 RMFlights 宕机
  - 重启RMFlights
  - 读取数据并进行检查：Flight失败

- ACIDDieRMBeforeAbort.java
  - 测试 Atomicity & Consistency
  - **分为4个阶段**
  - 增加Flight 和 Car
  - 在 abort 之前 RMFlights 宕机
  - 重启RMFlights
  - 读取数据并进行检查：Flight abort失败，Car abort 成功


## 4.3. WC 宕机
- ACIDDieWC.java
  - 测试 Atomicity & Consistency & Durability
  - **分为4个阶段**
  - 增加Flight 和 Customer，并 commit
  - customer 预订 flight，随后 WC 宕机
  - 重启 WC，并 commit
  - 读取数据并进行检查：预订事务成功


# 5. 并行锁测试 Lock*.java
## 5.1. 正常运行测试
- LockRR.java
  - 测试 Durability
  - 读读共享
  - **分为4个阶段**
  - 增加Flight 并 commit
  - 申请两个xid
  - 用xid1 查询 flight1 的数量，用xid2 查询 flight1 的价格
  - 在代码层面先commit xid2，再commit xid1

- LockRW.java
  - 测试 Isolation & Durability
  - 读写等待，写wait读
  - **分为5个阶段**
  - 增加Flight 并 commit
  - 申请两个xid
  - 用xid1 查询 flight1 的数量，用xid2 增加另一台 flight2
  - 在代码层面先commit xid2，再commit xid1
  - 读取数据并进行检查

- LockWR.java
  - 测试 Isolation & Durability
  - 写读共享，读wait写
  - **分为5个阶段**
  - 增加Flight 并 commit
  - 申请两个xid
  - 用xid1 增加另一台 flight2，用xid2 查询 flight1 的数量
  - 在代码层面先commit xid2，再commit xid1
  - 读取数据并进行检查

- LockWW.java
  - 测试 Isolation & Durability
  - **分为4个阶段**
  - 申请两个xid
  - 用xid1 增加 flight1，用xid2 修改 flight1 的价格
  - 在代码层面先commit xid2，再commit xid1
  - 读取数据并进行检查：保留了xid1 的价格

## 5.2. 死锁测试
例子来自这两个: https://blog.csdn.net/fd2025/article/details/80597426
- LockDead2.java
  - **分为5个阶段**
  - 增加Flight 和 room 并 commit
  - 申请两个xid
  - 用 xid1 查询 flight1 的数量，用xid2 查询 room1 的价格
  - 用 xid1 增加 room2，用xid2 增加 flight2
    - 出现死锁
    - 死锁现象：xid1 检测到死锁，xid1 抛出异常
    - 死锁理由：一个用户A 访问表A(锁住了表A),然后又访问表B；另一个用户B 访问表B(锁住了表B)，然后企图访问表A；这时用户A由于用户B已经锁住表B，它必须等待用户B释放表B才能继续，同样用户B要等用户A释放表A才能继续，这就死锁就产生了。
  - 读取数据并进行检查

- LockDead2.java
  - **分为5个阶段**
  - 增加Flight 并 commit
  - 申请两个xid
  - 用 xid1 查询 flight1 的数量，用xid2 查询 flight1 的价格
  - 用 xid1 增加 flight2，用xid2 也增加 flight2
    - 出现死锁
    - 死锁现象：xid2 检测到死锁，xid2 抛出异常
    - 死锁理由：用户A查询一条纪录，然后修改该条纪录；这时用户B修改该条纪录，这时用户A的事务里锁的性质由查询的共享锁企图上升到独占锁，而用户B里的独占锁由于A有共享锁存在所以必须等A释放掉共享锁，而A由于B的独占锁而无法上升的独占锁也就不可能释放共享锁，于是出现了死锁。
  - 读取数据并进行检查
