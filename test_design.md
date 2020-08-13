- [1. 测试设计](#1-测试设计)
  - [1.1. 组件启动 (无测试)](#11-组件启动-无测试)
  - [1.2. 组件启动功能测试 Basic*.java](#12-组件启动功能测试-basicjava)
  - [1.3.](#13)
  - [1.4. 2 基本业务逻辑测试](#14-2-基本业务逻辑测试)
  - [1.5. 3 并发测试，锁功能测试](#15-3-并发测试锁功能测试)
  - [1.6. 4 宕机测试](#16-4-宕机测试)


# 1. 组件启动 (无测试)
1. run_server.sh


# 2. 组件启动功能测试 Basic*.java
- BasicBind.java
  - 绑定WC
- BasicCommit.java
  - 绑定WC后，提交一个空commit
- BasicAbort.java
  - 绑定WC后，放弃WC


# 3. 基本业务逻辑测试
主要为CRUD（create增, read读, update改, delete删）业务的独立以及组合测试。数据操作对象为Flight, Room, Car 以及 Customer。可以添加修改删除Flight, Room, Car, Customer。用户可以预订或者取消预订Flight, Room, Car。具体包括以下测试用例：
- CRUDCreate.java
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
- CRUDRead.java
  - **分为2个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 查询创建的数据，并进行校验
- CRUDUpdate.java
  - **分为3个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 查询创建的数据，并进行校验
  - 修改刚刚增加的Flight，Room，Car 和 Customer的信息
  - 查询更新后的数据，并进行校验
- CRUDDelete.java
  - **此测试为可正常删除。分为3个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 删除刚刚增加的Customer及其reservation，Flight，Room，Car的数据，**注意顺序**
  - 查询删除后的数据，并进行校验
- CRUDDeleteFail.java
  - **此测试为不可删除，在删除阶段因操作而请求失败。分为3个阶段**
  - 增加Flight，Room，Car 和 Customer，并给Customer预订Flight，Room 和 Car
  - 删除刚刚增加的Flight，Room，Car 和 Customer的信息（**注意顺序**）。删除有Customer预订的Flight，Room，Car，删除失败。删除无效。
  - 查询删除后的数据，并进行校验
- 


## 3.1. 2 基本业务逻辑测试
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
## 3.2. 3 并发测试，锁功能测试
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
## 3.3. 4 宕机测试
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