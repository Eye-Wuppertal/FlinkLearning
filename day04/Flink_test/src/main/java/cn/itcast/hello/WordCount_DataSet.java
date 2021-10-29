package cn.itcast.hello;

import groovy.lang.Tuple;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.*;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.stringtemplate.v4.ST;

public class WordCount_DataSet {
    public static void main(String[] args) throws Exception {
        // TODO 0. 准备环境 env
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // TODO 1. 准备数据 source
        DataSet<String> lines = env.fromElements("it hadoop spark","it hadoop flink","hadoop","spark");
        // TODO 2. 转换 transformation
        //  切割
         /*
        @FunctionalInterface
        public interface FlatMapFunction<T, O> extends Function, Serializable {
            void flatMap(T value, Collector<O> out) throws Exception;
        }
         */
        DataSet<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String value, Collector<String> out) throws Exception {
                // value 表示每一行数据
                String[] arr = value.split(" ");
                for (String word : arr) {
                    out.collect(word);
                }
            }
        });

        //  数据量设为1
         /*
        @FunctionalInterface
        public interface MapFunction<T, O> extends Function, Serializable {
            O map(T value) throws Exception;
        }
         */
        DataSet<Tuple2<String, Integer>> wordAddOne = words.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String value) throws Exception {
                return Tuple2.of(value, 1);
            }
        });

        //  分组
        UnsortedGrouping<Tuple2<String, Integer>> grouped = wordAddOne.groupBy(0);

        //  聚合
        AggregateOperator<Tuple2<String, Integer>> result = grouped.sum(1);

        // TODO 3. 打印sink
        result.print();

    }
}
