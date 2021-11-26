# State-理解原理即可

## Flink中状态的自动管理

之前写的Flink代码中其实已经做好了状态自动管理,如

发送hello ,得出(hello,1)

再发送hello ,得出(hello,2)

说明Flink已经自动的将当前数据和历史状态/历史结果进行了聚合,做到了状态的自动管理

在实际开发中绝大多数情况下,我们直接使用自动管理即可

一些特殊情况才会使用手动的状态管理!---后面项目中会使用!

所以这里得先学习state状态如何手动管理!

## 无状态计算和有状态计算

- 无状态计算,不需要考虑历史值, 如map  

hello --> (hello,1)

hello --> (hello,1)

![1610871016671](.\img\1610871016671.png)



- 有状态计算,需要考虑历史值,如:sum

hello , (hello,1)

hello , (hello,2)

![1610871116774](.\img\1610871116774.png)

![1610871292360](.\img\1610871292360.png)



## 状态分类

- State
  - ManagerState--开发中推荐使用 : Fink自动管理/优化,支持多种数据结构
    - KeyState--只能在keyedStream上使用,支持多种数据结构
    - OperatorState--所有都可以使用一般用在Source上,支持ListState
  - RawState--完全由用户自己管理,只支持byte[],只能在自定义Operator上使用
    - OperatorState



分类详细图解:

![1610871484540](.\img\1610871484540.png)

![1610871611731](.\img\1610871611731.png)

在Flink Stream模型中，Datastream 经过 keyBy 的操作可以变为 KeyedStream。
Keyed State是基于KeyedStream上的状态。这个状态是跟特定的key绑定的，对KeyedStream流上的每一个key，都对应一个state，如stream.keyBy(…)；
KeyBy之后的State,可以理解为分区过的State，每个并行keyed Operator的每个实例的每个key都有一个Keyed State，即<parallel-operator-instance,key>就是一个唯一的状态，由于每个key属于一个keyed Operator的并行实例，因此我们将其简单的理解为<operator,key>

![image-20211117185741433](.\img\image-20211117185741433.png)

Operator State又称为 non-keyed state，与Key无关的State，每一个 operator state 都仅与一个 operat    的实例绑定。
   Operator State 可以用于所有算子，但一般常用于 Sourc

## 存储State的数据结构/API介绍

Keyed State 通过 RuntimeContext 访问，这需要 Operator 是一个RichFunction。保存Keyed state的数据结构:

- ValueState<T>:即类型为T的单值状态。这个状态与对应的key绑定，是最简单的状态了。它可以通过update方法更新状态值，通过value()方法获取状态值，如求按用户id统计用户交易总额
- ListState<T>:即key上的状态值为一个列表。可以通过add方法往列表中附加值；也可以通过get()方法返回一个Iterable<T>来遍历状态值，如统计按用户id统计用户经常登录的Ip
- ReducingState<T>:这种状态通过用户传入的reduceFunction，每次调用add方法添加值的时候，会调用reduceFunction，最后合并到一个单一的状态值
- MapState<UK, UV>:即状态值为一个map。用户通过put或putAll方法添加元素

需要注意的是，以上所述的State对象，仅仅用于与状态进行交互(更新、删除、清空等)，而真正的状态值，有可能是存在内存、磁盘、或者其他分布式存储系统中。相当于我们只是持有了这个状态的句柄。

Operator State 需要自己实现 CheckpointedFunction 或 ListCheckpointed 接口。保存Operator state的数据结构:

- ListState<T>
- BroadcastState<K,V>



## 代码演示-ManagerState-keyState

https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/stream/state/state.html

需求:
使用KeyState中的ValueState获取数据中的最大值(实际中直接使用maxBy即可)
编码步骤

```java
//-1.定义一个状态用来存放最大值
private transient ValueState<Long> maxValueState;

//-2.创建一个状态描述符对象
ValueStateDescriptor descriptor = new ValueStateDescriptor("maxValueState", Long.class);
//-3.根据状态描述符获取State
maxValueState = getRuntimeContext().getState(maxValueStateDescriptor);
//-4.使用State
Long historyValue = maxValueState.value();
判断当前值和历史值谁大
if (historyValue == null || currentValue > historyValue) 
//-5.更新状态
maxValueState.update(currentValue);
```

```java
package cn.tal.Senior_API.State;
/* 
    @TODO: 使用KeyState中的ValueState获取流数据中的最大值/实际中可以使用maxBy即可
    @Author tal
*/

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StateDemo01 {
    public static void main(String[] args) throws Exception {
        //TODO 0.env
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        //TODO 1.source
        DataStream<Tuple2<String, Long>> tupleDS = env.fromElements(
                Tuple2.of("北京", 1L),
                Tuple2.of("上海", 2L),
                Tuple2.of("北京", 6L),
                Tuple2.of("上海", 8L),
                Tuple2.of("北京", 3L),
                Tuple2.of("上海", 4L)
        );

        //TODO 2.transformation
        //需求:求各个城市的value最大值
        //实际中使用maxBy即可
        DataStream<Tuple2<String, Long>> result1 = tupleDS.keyBy(t -> t.f0).maxBy(1);
        //学习时可以使用KeyState中的ValueState来实现maxBy的底层
        SingleOutputStreamOperator<Tuple3<String, Long, Long>> result2 = tupleDS.keyBy(t -> t.f0).map(new RichMapFunction<Tuple2<String, Long>, Tuple3<String, Long, Long>>() {

            //-1.定义一个状态用来存放最大值
            private ValueState<Long> maxValueState;

            //-2.状态初始化
            @Override
            public void open(Configuration parameters) throws Exception {
                //创建状态描述器
                ValueStateDescriptor stateDescriptor = new ValueStateDescriptor("maxValueState", Long.class);
                //根据状态描述器获取/初始化状态
                maxValueState = getRuntimeContext().getState(stateDescriptor);
                //通过RuntimeContext 访问state
            }

            //-3.使用状态
            @Override
            public Tuple3<String, Long, Long> map(Tuple2<String, Long> value) throws Exception {
                Long currentValue = value.f1;
                //获取状态
                Long historyValue = maxValueState.value();
                //判断状态
                if (historyValue == null || currentValue > historyValue) {
                    historyValue = currentValue;
                    //更新状态
                    maxValueState.update(historyValue);

                }
                return Tuple3.of(value.f0, currentValue, historyValue);
            }
        });

        //TODO 3.sink
        result1.print();
        result2.print();

        //TODO 4.execute

        env.execute();
    }

}

```

![image-20211117183300459](.\img\image-20211117183300459.png)



## 代码演示-ManagerState-OperatorState

https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/stream/state/state.html

需求:
使用ListState存储offset模拟Kafka的offset维护
编码步骤

```java
//-1.声明一个OperatorState来记录offset
private ListState<Long> offsetState = null;
private Long offset = 0L;
//-2.创建状态描述器
ListStateDescriptor<Long> descriptor = new ListStateDescriptor<Long>("offsetState", Long.class);
//-3.根据状态描述器获取State
offsetState = context.getOperatorStateStore().getListState(descriptor);
//-4.获取State中的值
Iterator<Long> iterator = offsetState.get().iterator();
if (iterator.hasNext()) {//迭代器中有值
    offset = iterator.next();//取出的值就是offset
}
offset += 1L;
ctx.collect("subTaskId:" + getRuntimeContext().getIndexOfThisSubtask() + ",当前的offset为:" + offset);
if (offset % 5 == 0) {//每隔5条消息,模拟一个异常
//-5.保存State到Checkpoint中
offsetState.clear();//清理内存中存储的offset到Checkpoint中
//-6.将offset存入State中
offsetState.add(offset);
```

```java
package cn.tal.Senior_API.State;
/* 
    @TODO: 使用OperatorState中的ListState模拟KafkaSource进行offset维护
    @Author tal
*/

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.Iterator;

public class StateDemo02 {
    public static void main(String[] args) throws Exception {
        //TODO 0.env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        env.setParallelism(1);//并行度设置为1方便观察
        env.enableCheckpointing(1000);//每隔1s执行一次Checkpoint
        env.setStateBackend(new FsStateBackend("file:///F:/FlinkLearning/day10/ckp"));
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //固定延迟重启策略: 程序出现异常的时候，重启2次，每次延迟3秒钟重启，超过2次，程序退出
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2, 3000));


        //TODO 1.source
        DataStreamSource<String> ds = env.addSource(new MyKafkaSource()).setParallelism(1);

        //TODO 2.transformation


        //TODO 3.sink
        ds.print();

        //TODO 4.execute

        env.execute();
    }
    //使用OperatorState中的ListState模拟KafkaSource进行offset维护
    public static class MyKafkaSource extends RichParallelSourceFunction<String> implements CheckpointedFunction {
        private boolean flag = true;
        //-1.声明ListState
        private ListState<Long> offsetState = null; //用来存放offset
        private Long offset = 0L;//用来存放offset的值

        //-2.初始化/创建ListState
        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            ListStateDescriptor<Long> stateDescriptor = new ListStateDescriptor<>("offsetState", Long.class);
            offsetState = context.getOperatorStateStore().getListState(stateDescriptor);
        }
        //-3.使用state
        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            while (flag){
                Iterator<Long> iterator = offsetState.get().iterator();
                if(iterator.hasNext()){
                    offset = iterator.next();
                }
                offset += 1;
                int subTaskId = getRuntimeContext().getIndexOfThisSubtask();
                ctx.collect("subTaskId:"+ subTaskId + ",当前的offset值为:"+offset);
                Thread.sleep(1000);

                //模拟异常
                if(offset % 5 == 0){
                    throw new Exception("bug出现了.....");
                }
            }
        }
        //-4.state持久化
        //该方法会定时执行将state状态从内存存入Checkpoint磁盘目录中
        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            offsetState.clear();//清理内容数据并存入Checkpoint磁盘目录中
            offsetState.add(offset);
        }

        @Override
        public void cancel() {
            flag = false;
        }
    }
}
```

![image-20211117195916318](.\img\image-20211117195916318.png)

![image-20211117195408411](.\img\image-20211117195408411.png)

# 容错机制

## Checkpoint

### Checkpoint和State

![1610933897175](.\img\1610933897175.png)

### Checkpoint执行流程

![1610934109534](.\img\1610934109534.png)

0. Flink的JobManager创建CheckpointCoordinator

1. Coordinator向所有的SourceOperator发送Barrier栅栏(理解为执行 Checkpoint的信号)

2. SourceOperator接收到Barrier之后,暂停当前的操作(暂停的时间很短,因为后续的写快照是异步的),并制作State快照, 然后将自己的快照保存到指定的介质中(如HDFS), 一切 ok之后向Coordinator汇报并将Barrier发送给下游的其他Operator

3. 其他的如TransformationOperator接收到Barrier,重复第2步,最后将Barrier发送给Sink

4. Sink接收到Barrier之后重复第2步

5. Coordinator接收到所有的Operator的执行ok的汇报结果,认为本次快照执行成功

Flink中的Checkpoint底层使用了Chandy-Lamport algorithm分布式快照算法可以保证数据的在分布式环境下的一致性! 

https://zhuanlan.zhihu.com/p/53482103

Chandy-Lamport algorithm算法的作者也是ZK中Paxos 一致性算法的作者

https://www.cnblogs.com/shenguanpu/p/4048660.html

Flink中使用Chandy-Lamport algorithm分布式快照算法取得了成功,后续Spark的StructuredStreaming也借鉴了该算法



### 状态后端/存储介质

![1610935633839](.\img\1610935633839.png)

![1610935708886](.\img\1610935708886.png) 

![1610935783481](.\img\1610935783481.png)

![1610935904023](.\img\1610935904023.png)

```xml
 <dependency>
       <groupId>org.apache.flink</groupId>
       <artifactId>flink-statebackend-rocksdb_2.12</artifactId>
       <version>1.12.0</version>
    </dependency>
```



### Checkpoint代码演示

![1610936183684](.\img\1610936183684.png)

https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/stream/state/checkpointing.html

```java
package cn.tal.Senior_API.Checkpoint;
/* 
    @TODO: 演示Flink-Checkpoint相关配置
    @Author tal
*/

import org.apache.commons.lang3.SystemUtils;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;


import java.util.Properties;

public class CheckpointDemo01 {
    public static void main(String[] args) throws Exception {
        //TODO 0.env
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
        //TODO ===========Checkpoint参数设置====
        //===========类型1:必须参数=============
        //设置Checkpoint的时间间隔为1000ms做一次Checkpoint/其实就是每隔1000ms发一次Barrier!
        env.enableCheckpointing(1000);

        //设置State状态存储介质/状态后端
        //Memory:State存内存,Checkpoint存内存--开发不用!
        //Fs:State存内存,Checkpoint存FS(本地/HDFS)--一般情况下使用
        //RocksDB:State存RocksDB(内存+磁盘),Checkpoint存FS(本地/HDFS)--超大状态使用,但是对于状态的读写效率要低一点

        /*
        if(args.length > 0){
            env.setStateBackend(new FsStateBackend(args[0]));
        }else {
            env.setStateBackend(new FsStateBackend("file:///D:\\data\\ckp"));
        }
        */
        if(SystemUtils.IS_OS_WINDOWS) {
            env.setStateBackend(new FsStateBackend("file:///F:/FlinkLearning/day10/ckp"));
        }else{
            env.setStateBackend(new FsStateBackend("hdfs://msater:9000/flink-checkpoint/checkpoint"));
        }

        //===========类型2:建议参数===========
        //设置两个Checkpoint 之间最少等待时间,如设置Checkpoint之间最少是要等 500ms
        // (为了避免每隔1000ms做一次Checkpoint的时候,前一次太慢和后一次重叠到一起去了)
        //如:高速公路上,每隔1s关口放行一辆车,但是规定了两车之前的最小车距为500m

        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);//默认是0
        //设置如果在做Checkpoint过程中出现错误，是否让整体任务失败：true是  false不是
        //env.getCheckpointConfig().setFailOnCheckpointingErrors(false);//默认是true
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(10);//默认值为0，表示不容忍任何检查点失败
        //设置是否清理检查点,表示 Cancel 时是否需要保留当前的 Checkpoint，默认 Checkpoint会在作业被Cancel时被删除
        //ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION：true,当作业被取消时，删除外部的checkpoint(默认值)
        //ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION：false,当作业被取消时，保留外部的checkpoint
        env.getCheckpointConfig().enableExternalizedCheckpoints(
                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        //===========类型3:直接使用默认的即可===============
        //设置checkpoint的执行模式为EXACTLY_ONCE(默认)
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //设置checkpoint的超时时间,如果 Checkpoint在 60s内尚未完成说明该次Checkpoint失败,则丢弃。
        env.getCheckpointConfig().setCheckpointTimeout(60000);//默认10分钟
        //设置同一时间有多少个checkpoint可以同时执行
        //env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);//默认为1


        //TODO 1.source
        DataStream<String> linesDS = env.socketTextStream("master", 9999);

        //TODO 2.transformation
        //2.1切割出每个单词并直接记为1
        DataStream<Tuple2<String, Integer>> wordAndOneDS = linesDS.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                //value就是每一行
                String[] words = value.split(" ");
                for (String word : words) {
                    out.collect(Tuple2.of(word, 1));
                }
            }
        });

        //2.2分组
        //注意:批处理的分组是groupBy,流处理的分组是keyBy
        KeyedStream<Tuple2<String, Integer>, String> groupedDS = wordAndOneDS.keyBy(t -> t.f0);

        //2.3聚合
        DataStream<Tuple2<String, Integer>> aggResult = groupedDS.sum(1);

        DataStream<String> result = (SingleOutputStreamOperator<String>) aggResult.map(new RichMapFunction<Tuple2<String, Integer>, String>() {
            @Override
            public String map(Tuple2<String, Integer> value) throws Exception {
                return value.f0 + ":::" + value.f1;
            }
        });


        //TODO 3.sink
        result.print();

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "master:9092");
        FlinkKafkaProducer<String> kafkaSink = new FlinkKafkaProducer<>("flink_kafka", new SimpleStringSchema(), props);
        result.addSink(kafkaSink);


        //TODO 4.execute

        env.execute();
        // kafka-console-consumer.sh --bootstrap-server master:9092 --topic flink_kafka

    }
}

```





## 状态恢复

### 自动重启-全自动-掌握

![1610940036243](.\img\1610940036243.png)

```java
package cn.tal.Senior_API.Checkpoint;
/* 
    @TODO: 演示Flink-Checkpoint+重启策略实现状态恢复
    @Author tal
*/

import org.apache.commons.lang3.SystemUtils;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class CheckpointDemo02_Restart {
    public static void main(String[] args) throws Exception {
        //TODO 0.env
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        //TODO ===========Checkpoint参数设置====
        //===========类型1:必须参数=============
        //设置Checkpoint的时间间隔为1000ms做一次Checkpoint/其实就是每隔1000ms发一次Barrier!
        env.enableCheckpointing(1000);

        //设置State状态存储介质/状态后端
        //Memory:State存内存,Checkpoint存内存--开发不用!
        //Fs:State存内存,Checkpoint存FS(本地/HDFS)--一般情况下使用
        //RocksDB:State存RocksDB(内存+磁盘),Checkpoint存FS(本地/HDFS)--超大状态使用,但是对于状态的读写效率要低一点

        /*
        if(args.length > 0){
            env.setStateBackend(new FsStateBackend(args[0]));
        }else {
            env.setStateBackend(new FsStateBackend("file:///D:\\data\\ckp"));
        }
        */

        if(SystemUtils.IS_OS_WINDOWS) {
            env.setStateBackend(new FsStateBackend("file:///F:/FlinkLearning/day10/ckp"));
        }else{
            env.setStateBackend(new FsStateBackend("hdfs://master:9000/flink-checkpoint/checkpoint"));
        }

        //===========类型2:建议参数===========
        //设置两个Checkpoint 之间最少等待时间,如设置Checkpoint之间最少是要等 500ms
        // (为了避免每隔1000ms做一次Checkpoint的时候,前一次太慢和后一次重叠到一起去了)
        //如:高速公路上,每隔1s关口放行一辆车,但是规定了两车之前的最小车距为500m

        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);//默认是0
        //设置如果在做Checkpoint过程中出现错误，是否让整体任务失败：true是  false不是
        //env.getCheckpointConfig().setFailOnCheckpointingErrors(false);//默认是true
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(10);//默认值为0，表示不容忍任何检查点失败
        //设置是否清理检查点,表示 Cancel 时是否需要保留当前的 Checkpoint，默认 Checkpoint会在作业被Cancel时被删除
        //ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION：true,当作业被取消时，删除外部的checkpoint(默认值)
        //ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION：false,当作业被取消时，保留外部的checkpoint
        env.getCheckpointConfig().enableExternalizedCheckpoints(
                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        //===========类型3:直接使用默认的即可===============
        //设置checkpoint的执行模式为EXACTLY_ONCE(默认)
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //设置checkpoint的超时时间,如果 Checkpoint在 60s内尚未完成说明该次Checkpoint失败,则丢弃。
        env.getCheckpointConfig().setCheckpointTimeout(60000);//默认10分钟
        //设置同一时间有多少个checkpoint可以同时执行
        //env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);//默认为1

        //TODO ===配置重启策略:
        //1.配置了Checkpoint的情况下不做任务配置:默认是无限重启并自动恢复,可以解决小问题,但是可能会隐藏真正的bug
        //2.单独配置无重启策略
        //env.setRestartStrategy(RestartStrategies.noRestart());
        //3.固定延迟重启--开发中常用
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // 最多重启3次数
                Time.of(5, TimeUnit.SECONDS) // 重启时间间隔
        ));
        //上面的设置表示:如果job失败,重启3次, 每次间隔5s
        //4.失败率重启--开发中偶尔使用
        /*env.setRestartStrategy(RestartStrategies.failureRateRestart(
                3, // 每个测量阶段内最大失败次数
                Time.of(1, TimeUnit.MINUTES), //失败率测量的时间间隔
                Time.of(3, TimeUnit.SECONDS) // 两次连续重启的时间间隔
        ));*/
        //上面的设置表示:如果1分钟内job失败不超过三次,自动重启,每次重启间隔3s (如果1分钟内程序失败达到3次,则程序退出)


        //TODO 1.source
        DataStream<String> linesDS = env.socketTextStream("master", 9999);

        //TODO 2.transformation
        //2.1切割出每个单词并直接记为1
        DataStream<Tuple2<String, Integer>> wordAndOneDS = linesDS.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                //value就是每一行
                String[] words = value.split(" ");
                for (String word : words) {
                    if (word.equals("bug")) {
                        System.out.println("bug.....");
                        throw new Exception("bug.....");
                    }
                    out.collect(Tuple2.of(word, 1));
                }
            }
        });

        //2.2分组
        //注意:批处理的分组是groupBy,流处理的分组是keyBy
        KeyedStream<Tuple2<String, Integer>, String> groupedDS = wordAndOneDS.keyBy(t -> t.f0);

        //2.3聚合
        DataStream<Tuple2<String, Integer>> aggResult = groupedDS.sum(1);

        DataStream<String> result = (SingleOutputStreamOperator<String>) aggResult.map(new RichMapFunction<Tuple2<String, Integer>, String>() {
            @Override
            public String map(Tuple2<String, Integer> value) throws Exception {
                return value.f0 + ":::" + value.f1;
            }
        });


        //TODO 3.sink
        result.print();

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "master:9092");
        FlinkKafkaProducer<String> kafkaSink = new FlinkKafkaProducer<>("flink_kafka", new SimpleStringSchema(), props);
        result.addSink(kafkaSink);


        //TODO 4.execute

        env.execute();
        // kafka-console-consumer.sh --bootstrap-server master:9092 --topic flink_kafka

    }
}

```

![image-20211125133955453](.\img\image-20211125133955453.png)

### 手动重启-半自动-了解

1.打包-要大的里面用到了kafka

2.启动Flink集群

3.上传jar包配置并提交

http://node1:8081/#/submit

![1610940387984](.\img\1610940387984.png)

4.发送单词并观察hdfs目录

5.取消任务

![1610940703403](.\img\1610940703403.png)

6.重新提交任务并指定从指定的ckp目录恢复状态接着计算

hdfs://master:9000/flink-checkpoint/checkpoint/1e0a3039f1f868683b348bcc9c0f7121/chk-234

![1610940852166](.\img\1610940852166.png)

7.继续发送数据发现可以恢复从之前的状态继续计算



## Savepoint-全手动-了解

### Savepoint VS Checkpoint

![1610941381825](.\img\1610941381825.png)



![1610941510468](.\img\1610941510468.png)

### 演示

```shell
# 启动yarn session
yarn-session.sh -n 2 -tm 800 -s 1 -d

# 运行job-会自动执行Checkpoint
flink run --class cn.tal.Senior_API.Checkpoint.CheckpointDemo01 /root/ckp.jar

# 手动创建savepoint--相当于手动做了一次Checkpoint
flink savepoint d56445775346ea0926a9b4caf8bfb54b hdfs://master:9000/flink-checkpoint/savepoint/

# 停止job
flink cancel d56445775346ea0926a9b4caf8bfb54b

# 重新启动job,手动加载savepoint数据
flink run -s hdfs://master:9000/flink-checkpoint/savepoint/savepoint-d56445-52fc30fec149 --class cn.tal.Senior_API.Checkpoint.CheckpointDemo01 /root/ckp.jar 

# 停止yarn session
yarn application -kill application_1637810682893_0001
```

![image-20211125142404074](.\img\image-20211125142404074.png)

![image-20211125142727030](.\img\image-20211125142727030.png)
