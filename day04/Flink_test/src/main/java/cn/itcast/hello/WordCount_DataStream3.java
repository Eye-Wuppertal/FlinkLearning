package cn.itcast.hello;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


import java.util.Arrays;

/**
 * Author itcast
 * Desc 演示Flink-DataStream-API-实现WordCount
 * 注意:在Flink1.14中DataStream既支持流处理也支持批处理,如何区分?
 */

public class WordCount_DataStream3 {
    public static void main(String[] args) throws Exception {
        // TODO 0. 准备环境 env
        // ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // env.setRuntimeMode(RuntimeExecutionMode.BATCH);//注意:使用DataStream实现批处理
        // env.setRuntimeMode(RuntimeExecutionMode.STREAMING);//注意:使用DataStream实现流处理
        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);//注意:使用DataStream根据数据源自动选择使用流还是批

        // TODO 1. 准备数据 source
        // DataSet<String> lines = env.fromElements("it hadoop spark","it hadoop flink","hadoop","spark");
         DataStream<String> lines = env.fromElements("it hadoop spark","it hadoop flink","hadoop","spark");

        // TODO 2. 转换 transformation
        //  切割
         /*
        @FunctionalInterface
        public interface FlatMapFunction<T, O> extends Function, Serializable {
            void flatMap(T value, Collector<O> out) throws Exception;
        }
         */
//        DataStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
//            @Override
//            public void flatMap(String value, Collector<String> out) throws Exception {
//                // value 表示每一行数据
//                String[] arr = value.split(" ");
//                for (String word : arr) {
//                    out.collect(word);
//                }
//            }
//        });
        SingleOutputStreamOperator<String> words = lines.flatMap(
                (String value, Collector<String> out) -> Arrays.stream(value.split(" ")).forEach(out::collect)
        ).returns(Types.STRING);


        //  数据量设为1
         /*
        @FunctionalInterface
        public interface MapFunction<T, O> extends Function, Serializable {
            O map(T value) throws Exception;
        }
         */
//        DataStream<Tuple2<String, Integer>> wordAddOne = words.map(new MapFunction<String, Tuple2<String, Integer>>() {
//            @Override
//            public Tuple2<String, Integer> map(String value) throws Exception {
//                //value就是一个个单词
//                return Tuple2.of(value, 1);
//            }
//        });

        DataStream<Tuple2<String,Integer>> wordAddOne = words.map(
                (String value) -> Tuple2.of(value, 1 )
        ).returns(Types.TUPLE(Types.STRING, Types.INT));

        //  分组:注意DataSet中分组是groupBy,DataStream分组是keyBy
        //wordAndOne.keyBy(0);
        /*
        @FunctionalInterface
        public interface KeySelector<IN, KEY> extends Function, Serializable {
            KEY getKey(IN value) throws Exception;
        }
         */
        // UnsortedGrouping<Tuple2<String, Integer>> grouped = wordAddOne.groupBy(0);
        KeyedStream<Tuple2<String, Integer>,String> grouped = wordAddOne.keyBy(t -> t.f0); //元组里的第一个

        //  聚合
        // AggregateOperator<Tuple2<String, Integer>> result = grouped.sum(1);
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = grouped.sum(1);

        // TODO 3. 打印sink
        result.print();

        //TODO 4.execute/启动并等待程序结束
        env.execute();
    }
}
