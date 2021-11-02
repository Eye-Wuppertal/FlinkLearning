# Flink流批一体API

## 流处理说明

有边界的流bounded stream:批数据

无边界的流unbounded stream:真正的流数据

![1610675396439](.\img\1610675396439.png)

![image-20211101174746550](.\img\image-20211101174746550.png)

![1610676448033](.\img\1610676448033.png)

编程模型
Flink 应用程序结构主要包含三部分,Source/Transformation/Sink,如下图所示：

![image-20211101175027779](.\img\image-20211101175027779.png)

![image-20211101175044749](.\img\image-20211101175044749.png)

# Source(数据源)

![image-20211101175140111](F:\FlinkLearning\day06\img\image-20211101175140111.png)

新建一个maven工程，添加pom依赖

## 基于集合

API
一般用于学习测试时编造数据时使用

1. env.fromElements(可变参数);
2. env.fromColletion(各种集合);
3. env.generateSequence(开始,结束);
4. env.fromSequence(开始,结束);

```java
package cn.itcast.source;

//演示DataStream-Source-基于集合

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;

public class SourceDemo01_Collection {
    public static void main(String[] args) throws Exception {
        // TODO 0. env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        // TODO 1. source
        DataStreamSource<String> ds1 = env.fromElements("hello jack", "hello ross", "hello hello");            //可变参数
        DataStreamSource<String> ds2 = env.fromCollection(Arrays.asList("hello jack", "hello ross", "hello hello"));  //各种集合
        DataStreamSource<Long> ds3 = env.generateSequence(1, 100);  //方法已过期
        DataStreamSource<Long> ds4 = env.fromSequence(1, 100);      //开始结束

        // TODO 2. transformation

        // TODO 3. sink
        ds1.print();
        ds2.print();
        ds3.print();
        ds4.print();


        // TODO 4. execute
        env.execute();

    }
}

```

![image-20211101214702398](.\img\image-20211101214702398.png)

## 基于文件

API
一般用于学习测试时编造数据时使用
env.readTextFile(本地/HDFS文件/文件夹); //压缩文件也可以

```shell

Caused by: java.io.IOException: Error opening the Input Split file:/F:/FlinkLearning/day06/Flink_API/data/input/dir.txt.gz [0,-1]: Not in GZIP format
...
Caused by: java.util.zip.ZipException: Not in GZIP format
	at java.base/java.util.zip.GZIPInputStream.readHeader(GZIPInputStream.java:166)
	at java.base/java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:80)
	at java.base/java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:92)
	at 
	... 19 more
# 错误待解决
# 依据错误提示可以得出问题出在压缩文件上，检查发现压缩文件格式有问题修改之后问题解决
```

```java
package cn.itcast.source;

//演示DataStream-Source-基于本地/HDFS文件或文件夹或压缩文件

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;

public class SourceDemo02_File {
    public static void main(String[] args) throws Exception {
        // TODO 0. env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        // TODO 1. source
        DataStreamSource<String> ds1 = env.readTextFile("data/input/words.txt");//文件路径
        DataStreamSource<String> ds2 = env.readTextFile("data/input/dir");//目录路径
        DataStreamSource<String> ds3 = env.readTextFile("data/input/dir.txt.gz");


        // TODO 2. transformation

        // TODO 3. sink
        ds1.print();
        ds2.print();
        ds3.print();


        // TODO 4. execute
        env.execute();

    }
}

```



## 基于Socket

需求:
1.在node1上使用nc -lk 9999 向指定端口发送数据
nc是netcat的简称，原本是用来设置路由器,我们可以利用它向某个端口发送数据
如果没有该命令可以下安装
yum install -y nc
2.使用Flink编写流处理应用程序实时统计单词数量

```java
package cn.itcast.source;

//演示DataStream-Source-基于Socket

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


public class SourceDemo03_Socket {
    public static void main(String[] args) throws Exception {
        // TODO 0. env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        // TODO 1. source
        DataStream<String> lines = env.socketTextStream("master", 9999);


        // TODO 2. transformation
//        SingleOutputStreamOperator<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
//            @Override
//            public void flatMap(String value, Collector<String> out) throws Exception {
//                String[] arr = value.split(" ");
//                for (String word : arr) {
//                    out.collect(word);
//                }
//            }
//        });
//
//        words.map(new MapFunction<String, Tuple2<String,Integer>>() {
//            @Override
//            public Tuple2<String, Integer> map(String value) throws Exception {
//                return Tuple2.of(value,1);
//            }
//        });

        // TODO 整合上面两个 直接切割单词并记为1返回
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAddOne = lines.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {

            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] arr = value.split(" ");
                for (String word : arr) {
                    out.collect(Tuple2.of(word,1));
                }
            }
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> result = wordAddOne.keyBy(t -> t.f0).sum(1);


        // TODO 3. sink
        result.print();


        // TODO 4. execute
        env.execute();

    }
}

```

![image-20211102213600985](.\img\image-20211102213600985.png)

## 自定义Source

### 随机生成数据

一般用于学习测试,模拟生成一些数据
Flink还提供了数据源接口,我们实现该接口就可以实现自定义数据源，不同的接口有不同的功能，分类如下：
SourceFunction:非并行数据源(并行度只能=1)
RichSourceFunction:多功能非并行数据源(并行度只能=1)
ParallelSourceFunction:并行数据源(并行度能够>=1)
**RichParallelSourceFunction**:多功能并行数据源(并行度能够>=1)--后续学习的Kafka数据源使用的就是该接口

![1610678757851](.\img\1610678757851.png)

```java
package cn.itcast.source;

//演示DataStream-Source-自定义数据源
//需求：

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;
import java.util.UUID;


public class SourceDemo04_Customer {
    public static void main(String[] args) throws Exception {
        // TODO 0. env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        // TODO 1. source
        DataStream<Order> orderDS = env.addSource(new MyOrderSource()).setParallelism(2);


        // TODO 2. transformation


        // TODO 3. sink
        orderDS.print();


        // TODO 4. execute
        env.execute();

    }
    @Data//lombok已经捆绑使用 可以直接生成调用类数据的方法 alt+7 查看
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order{
        private String id;
        private Integer userId;
        private Integer money;
        private Long createTime;

    }
    public static class MyOrderSource extends RichParallelSourceFunction<Order> {

        private Boolean flag = true;
        //执行并生成数据
        @Override
        public void run(SourceContext<Order> ctx) throws Exception {
            Random random = new Random();
            while (flag) {
                String oid = UUID.randomUUID().toString();
                int userId = random.nextInt(3);
                int money = random.nextInt(101);
                long createTime = System.currentTimeMillis();
                ctx.collect(new Order(oid,userId,money,createTime));
                Thread.sleep(1000);
            }
        }

        //执行cancel命令的时候执行
        @Override
        public void cancel() {
            flag = false;
        }
    }

}

```

![image-20211102215717478](.\img\image-20211102215717478.png)

### MySQL

需求
实际开发中,经常会实时接收一些数据,要和MySQL中存储的一些规则进行匹配,那么这时候就可以使用Flink自定义数据源从MySQL中读取数据
那么现在先完成一个简单的需求:
从MySQL中实时加载数据
要求MySQL中的数据有变化,也能被实时加载出来

```java
package cn.itcast.source;

//演示DataStream-Source-自定义数据源 MySQL
//需求：

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.UUID;


public class SourceDemo05_MySQL {
    public static void main(String[] args) throws Exception {
        // TODO 0. env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        // TODO 1. source
        DataStream<Student> studentDS = env.addSource(new MySQLSource()).setParallelism(1);


        // TODO 2. transformation




        // TODO 3. sink
        studentDS.print();


        // TODO 4. execute
        env.execute();

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Student {
        private Integer id;
        private String name;
        private Integer age;
    }

    public static class MySQLSource extends RichParallelSourceFunction<Student> {
        private boolean flag = true;
        private Connection conn = null;
        private PreparedStatement ps =null;
        private ResultSet rs  = null;
        //open只执行一次,适合开启资源
        @Override
        public void open(Configuration parameters) throws Exception {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bigdata", "root", "root");
            String sql = "select id,name,age from t_student";
            ps = conn.prepareStatement(sql);
        }

        @Override
        public void run(SourceContext<Student> ctx) throws Exception {
            while (flag) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int age  = rs.getInt("age");
                    ctx.collect(new Student(id,name,age));
                }
                Thread.sleep(5000);
            }
        }

        //接收到cancel命令时取消数据生成
        @Override
        public void cancel() {
            flag = false;
        }

        //close里面关闭资源
        @Override
        public void close() throws Exception {
            if(conn != null) conn.close();
            if(ps != null) ps.close();
            if(rs != null) rs.close();

        }
    }

}

```









# Transform（操作处理）



# Sink（输出）



# Connectors（）











