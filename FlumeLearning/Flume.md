# Flume

- 概述

  - flume是一款大数据中海量数据采集传输汇总的软件。特别指的是数据流转的过程，或者说是数据搬运的过程。把数据从一个存储介质通过flume传递到另一个存储介质中。

- 核心组件

  - source ：用于对接各个不同的数据源
  - sink：用于对接各个不同存储数据的目的地（数据下沉地）
  - channel：用于中间临时存储缓存数据

- 运行机制

  - flume本身是java程序 在需要采集数据机器上启动 ----->agent进程

  - agent进程里面包含了：source  sink  channel

  - 在flume中，数据被包装成event 真是的数据是放在event body中

    event是flume中最小的数据单元

- 运行架构

  - 简单架构

    只需要部署一个agent进程即可

  - 复杂架构

    多个agent之间的串联  相当于大家手拉手共同完成数据的采集传输工作

    在串联的架构中没有主从之分 大家的地位都是一样的

------

- flume的安装部署

  - 在conf/flume-env.sh  导入java环境变量

    保证flume工作的时候一定可以正确的加载到环境变量

- flume开发步骤

  - 中的就是根据业务需求编写采集方案配置文件

  - 文件名见名知意  通常以souce——sink.conf

  - 具体需要描述清楚sink source channel组件配置信息 结合官网配置

  - 启动命令

    ```
    bin/flume-ng agent --conf conf --conf-file conf/netcat-logger.conf --name a1 -Dflume.root.logger=INFO,console  命令完整版
    
    bin/flume-ng agent -c ./conf -f ./conf/spool-hdfs.conf -n a1 -Dflume.root.logger=INFO,console  命令精简版
    
    --conf指定配置文件的目录
    --conf-file指定采集方案路径
    --name  agent进程名字 要跟采集方案中保持一致
    ```

---

- 案例：监控目录数据变化到hdfs

  - hdfs sink

    ```
    roll控制写入hdfs文件 以何种方式进行滚动
    a1.sinks.k1.hdfs.rollInterval = 3  以时间间隔
    a1.sinks.k1.hdfs.rollSize = 20     以文件大小
    a1.sinks.k1.hdfs.rollCount = 5     以event个数
    如果三个都配置  谁先满足谁触发滚动
    如果不想以某种属性滚动  设置为0即可
    
    是否开启时间上的舍弃  控制文件夹以多少时间间隔滚动
    以下述为例：就会每10分钟生成一个文件夹
    a1.sinks.k1.hdfs.round = true
    a1.sinks.k1.hdfs.roundValue = 10
    a1.sinks.k1.hdfs.roundUnit = minute
    ```

  - spooldir  source

    - 注意其监控的文件夹下面不能有同名文件的产生
    - 如果有 报错且罢工 后续就不再进行数据的监视采集了
    - 在企业中通常给文件追加时间戳命名的方式保证文件不会重名

- 案例：监控文件的变化采集到hdfs

  - exec source  可以执行指定的linux command  把命令的结果作为数据进行收集

    ```
    while true; do date >> /root/logs/test.log;done
    使用该脚本模拟数据实时变化的过程
    ```

---

- flume的负载均衡

  - 所谓的负载均衡 用于解决一个进程或者程序处理不了所有请求 多个进程一起处理的场景
  - 同一个请求只能交给一个进行处理 避免数据重复
  - 如何分配请求就涉及到了负载均衡的算法：轮询（round_robin）  随机（random）  权重

- flume串联跨网络传输数据

  - avro sink  

  - avro source

    使用上述两个组件指定绑定的端口ip 就可以满足数据跨网络的传递 通常用于flume串联架构中

- flume串联启动

  通常从远离数据源的那一级开始启动

----

- flume failover
  - 容错又称之为故障转移  容忍错误的发生。
  - 通常用于解决单点故障 给容易出故障的地方设置备份
  - 备份越多 容错能力越强  但是资源的浪费越严重

----

- 静态拦截器

  ```
  如果没有使用静态拦截器
  Event: { headers:{} body:  36 Sun Jun  2 18:26 }
  
  使用静态拦截器之后 自己添加kv标识对
  Event: { headers:{type=access} body:  36 Sun Jun  2 18:26 }
  Event: { headers:{type=nginx} body:  36 Sun Jun  2 18:26 }
  Event: { headers:{type=web} body:  36 Sun Jun  2 18:26 }
  ```

  后续在存放数据的时候可以使用flume的规则语法获取到拦截器添加的kv内容

  ```
  %{type}
  ```

- 模拟数据实时产生

  ```
  while true; do echo "access access....." >> /root/logs/access.log;sleep 0.5;done
  while true; do echo "web web....." >> /root/logs/web.log;sleep 0.5;done
  while true; do echo "nginx nginx....." >> /root/logs/nginx.log;sleep 0.5;done
  ```

----

- 























