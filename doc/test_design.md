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


# 5. 并行锁测试
- LockIsolation.java
  - 测试 Isolation
  - **分为4个阶段**
  - 申请两个xid
  - 用xid1 增加 flight1，用xid2 修改 flight1 的价格
  - 在代码层面先commit xid2，再commit xid1
  - 读取数据并进行检查：保留了xid1 的价格




## 5.1. 2 基本业务逻辑测试
增删改查极其组合业务，包含数据Flight, Room, Car, Customer。可以添加修改删除Flight, Room, Car, Customer。用户可以预订
或者取消预订Flight, Room, Car。8-16 个测试用例。
- 增加Flight, Room, Car, Customer。
- 删除Flight, Room, Car, Customer。
- 查看Flight, Room, Car, Customer。
- 修改Flight, Room, Car。
- 用户预订Flight, Room, Car。
- 用户取消预订Flight, Room, Car。
- 输入异常
    - 异常key，数据
## 5.2. 3 并发测试，锁功能测试
当并发执行以上事务逻辑，能够正确执行。基于两阶段锁测试。随机选择基本事务。5-10个测试用例
- 读读共享: T_L_RR
- 读写等待: T_L_RW
- 写读等待: T_L_WR
- 写写等待: T_L_WW
- 死锁，后者放弃事务
    - 读读写写死锁: T_L_RRWW
    - 2数据，读写读写死锁: T_L_RWRW
    - 2数据，写读写读死锁: T_L_WRWR
    - 2数据，写写写写死锁: T_L_WWWW
## 5.3. 4 宕机测试
基于两阶段提交进行测试。随机选择基本事务。基于现有WC接口。 7-14个测试用例。
- TM 宕机
    - 两阶段提交前宕机，事务失败. T_TM_DIE
    - 开启事务后直到写COMMIT log前宕机（After INITED），事务失败
        - PREPARE 之前，PREPARE 之后. T_TM_DBC
    - 写COMMIT log之后宕机，事务成功. T_TM_DAC
    - 事务ABORT的时候宕机，同写COMMIT log前，事务失败. no condition
- RM 宕机，多个RM，随机选择宕机
    - 与当前事务无关。事务成功 D_RM_DIE
    - 与当前事务有关
        - PREPARE 前宕机（已被TM通知过PREPARE），事务失败 D_RM_DBP
        - PREPARED 后宕机，（未成功发送prepared 消息至TM, 事务失败 D_RM_DAP
        - COMMIT log 前宕机过程中宕机（已成功发送prepared 消息至TM），事务成功 D_RM_DBC
        - COMMIT log 后宕机，事务成功 no condition.