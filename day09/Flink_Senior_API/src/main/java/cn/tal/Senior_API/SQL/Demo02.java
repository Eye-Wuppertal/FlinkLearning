package cn.tal.Senior_API.SQL;
/* 
    @TODO: 演示Flink Table&SQL 案例- 使用SQL和Table两种方式做WordCount
    @Author tal
*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.util.Arrays;

import static org.apache.flink.table.api.Expressions.$;

public class Demo02 {
    public static void main(String[] args) throws Exception {
        //TODO 0.env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tenv = StreamTableEnvironment.create(env, settings);

        //TODO 1.source
        DataStream<WC> wordsDS = env.fromElements(
                new WC("Hello", 1),
                new WC("World", 1),
                new WC("Hello", 1)
        );

        //TODO 2.transformation
        // 将DataStream数据转Table和View,然后查询
        tenv.createTemporaryView("t_words", wordsDS,$("word"), $("frequency"));

        /*
        select word,sum(frequency) as frequency
        from t_words
        group by word
         */

        String sql = "select word,sum(frequency) as frequency\n " +
                "from t_words\n " +
                "group by word";

        //执行sql
        Table resultTable = tenv.sqlQuery(sql);


        //将Table转为DataStream
        DataStream<Tuple2<Boolean, WC>> resultDS = tenv.toRetractStream(resultTable, WC.class);//union使用toRetractStream
        //toAppendStream → 将计算后的数据append到结果DataStream中去
        //toRetractStream  → 将计算后的新的数据在DataStream原数据的基础上更新true或是删除false
        //类似StructuredStreaming中的append/update/complete


        //TODO 3.sink
        resultDS.print();

        //TODO 4.execute

        env.execute();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WC {
        public String word;
        public long frequency;
    }
}
