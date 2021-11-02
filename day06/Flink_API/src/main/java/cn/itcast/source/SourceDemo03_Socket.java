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
