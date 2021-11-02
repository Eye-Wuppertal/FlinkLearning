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
