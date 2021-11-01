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
