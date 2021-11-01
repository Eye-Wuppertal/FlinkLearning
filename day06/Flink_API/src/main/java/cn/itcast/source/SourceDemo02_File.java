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
