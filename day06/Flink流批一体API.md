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
Exception in thread "main" org.apache.flink.runtime.client.JobExecutionException: Job execution failed.
	at org.apache.flink.runtime.jobmaster.JobResult.toJobExecutionResult(JobResult.java:144)
	at org.apache.flink.runtime.minicluster.MiniClusterJobClient.lambda$getJobExecutionResult$3(MiniClusterJobClient.java:137)
	at java.base/java.util.concurrent.CompletableFuture$UniApply.tryFire(CompletableFuture.java:642)
	at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:506)
	at java.base/java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2073)
	at org.apache.flink.runtime.rpc.akka.AkkaInvocationHandler.lambda$invokeRpc$0(AkkaInvocationHandler.java:250)
	at java.base/java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:859)
	at java.base/java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:837)
	at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:506)
	at java.base/java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2073)
	at org.apache.flink.util.concurrent.FutureUtils.doForward(FutureUtils.java:1389)
	at org.apache.flink.runtime.concurrent.akka.ClassLoadingUtils.lambda$null$1(ClassLoadingUtils.java:93)
	at org.apache.flink.runtime.concurrent.akka.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:68)
	at org.apache.flink.runtime.concurrent.akka.ClassLoadingUtils.lambda$guardCompletionWithContextClassLoader$2(ClassLoadingUtils.java:92)
	at java.base/java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:859)
	at java.base/java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:837)
	at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:506)
	at java.base/java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2073)
	at org.apache.flink.runtime.concurrent.akka.AkkaFutureUtils$1.onComplete(AkkaFutureUtils.java:47)
	at akka.dispatch.OnComplete.internal(Future.scala:300)
	at akka.dispatch.OnComplete.internal(Future.scala:297)
	at akka.dispatch.japi$CallbackBridge.apply(Future.scala:224)
	at akka.dispatch.japi$CallbackBridge.apply(Future.scala:221)
	at scala.concurrent.impl.CallbackRunnable.run(Promise.scala:60)
	at org.apache.flink.runtime.concurrent.akka.AkkaFutureUtils$DirectExecutionContext.execute(AkkaFutureUtils.java:65)
	at scala.concurrent.impl.CallbackRunnable.executeWithValue(Promise.scala:68)
	at scala.concurrent.impl.Promise$DefaultPromise.$anonfun$tryComplete$1(Promise.scala:284)
	at scala.concurrent.impl.Promise$DefaultPromise.$anonfun$tryComplete$1$adapted(Promise.scala:284)
	at scala.concurrent.impl.Promise$DefaultPromise.tryComplete(Promise.scala:284)
	at akka.pattern.PromiseActorRef.$bang(AskSupport.scala:621)
	at akka.pattern.PipeToSupport$PipeableFuture$$anonfun$pipeTo$1.applyOrElse(PipeToSupport.scala:24)
	at akka.pattern.PipeToSupport$PipeableFuture$$anonfun$pipeTo$1.applyOrElse(PipeToSupport.scala:23)
	at scala.concurrent.Future.$anonfun$andThen$1(Future.scala:532)
	at scala.concurrent.impl.Promise.liftedTree1$1(Promise.scala:29)
	at scala.concurrent.impl.Promise.$anonfun$transform$1(Promise.scala:29)
	at scala.concurrent.impl.CallbackRunnable.run(Promise.scala:60)
	at akka.dispatch.BatchingExecutor$AbstractBatch.processBatch(BatchingExecutor.scala:63)
	at akka.dispatch.BatchingExecutor$BlockableBatch.$anonfun$run$1(BatchingExecutor.scala:100)
	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:12)
	at scala.concurrent.BlockContext$.withBlockContext(BlockContext.scala:81)
	at akka.dispatch.BatchingExecutor$BlockableBatch.run(BatchingExecutor.scala:100)
	at akka.dispatch.TaskInvocation.run(AbstractDispatcher.scala:49)
	at akka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinTask.exec(ForkJoinExecutorConfigurator.scala:48)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:290)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1020)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1656)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1594)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:183)
Caused by: org.apache.flink.runtime.JobException: Recovery is suppressed by NoRestartBackoffTimeStrategy
	at org.apache.flink.runtime.executiongraph.failover.flip1.ExecutionFailureHandler.handleFailure(ExecutionFailureHandler.java:138)
	at org.apache.flink.runtime.executiongraph.failover.flip1.ExecutionFailureHandler.getFailureHandlingResult(ExecutionFailureHandler.java:82)
	at org.apache.flink.runtime.scheduler.DefaultScheduler.handleTaskFailure(DefaultScheduler.java:228)
	at org.apache.flink.runtime.scheduler.DefaultScheduler.maybeHandleTaskFailure(DefaultScheduler.java:218)
	at org.apache.flink.runtime.scheduler.DefaultScheduler.updateTaskExecutionStateInternal(DefaultScheduler.java:209)
	at org.apache.flink.runtime.scheduler.SchedulerBase.updateTaskExecutionState(SchedulerBase.java:679)
	at org.apache.flink.runtime.scheduler.SchedulerNG.updateTaskExecutionState(SchedulerNG.java:79)
	at org.apache.flink.runtime.jobmaster.JobMaster.updateTaskExecutionState(JobMaster.java:444)
	at jdk.internal.reflect.GeneratedMethodAccessor14.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor.lambda$handleRpcInvocation$1(AkkaRpcActor.java:316)
	at org.apache.flink.runtime.concurrent.akka.ClassLoadingUtils.runWithContextClassLoader(ClassLoadingUtils.java:83)
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleRpcInvocation(AkkaRpcActor.java:314)
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleRpcMessage(AkkaRpcActor.java:217)
	at org.apache.flink.runtime.rpc.akka.FencedAkkaRpcActor.handleRpcMessage(FencedAkkaRpcActor.java:78)
	at org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleMessage(AkkaRpcActor.java:163)
	at akka.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:24)
	at akka.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:20)
	at scala.PartialFunction.applyOrElse(PartialFunction.scala:123)
	at scala.PartialFunction.applyOrElse$(PartialFunction.scala:122)
	at akka.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:20)
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:171)
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:172)
	at scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:172)
	at akka.actor.Actor.aroundReceive(Actor.scala:537)
	at akka.actor.Actor.aroundReceive$(Actor.scala:535)
	at akka.actor.AbstractActor.aroundReceive(AbstractActor.scala:220)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:580)
	at akka.actor.ActorCell.invoke(ActorCell.scala:548)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:270)
	at akka.dispatch.Mailbox.run(Mailbox.scala:231)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:243)
	... 5 more
Caused by: java.io.IOException: Error opening the Input Split file:/F:/FlinkLearning/day06/Flink_API/data/input/dir.txt.gz [0,-1]: Not in GZIP format
	at org.apache.flink.api.common.io.FileInputFormat.open(FileInputFormat.java:873)
	at org.apache.flink.api.common.io.DelimitedInputFormat.open(DelimitedInputFormat.java:499)
	at org.apache.flink.api.common.io.DelimitedInputFormat.open(DelimitedInputFormat.java:50)
	at org.apache.flink.streaming.api.functions.source.ContinuousFileReaderOperator.loadSplit(ContinuousFileReaderOperator.java:415)
	at org.apache.flink.streaming.api.functions.source.ContinuousFileReaderOperator.access$300(ContinuousFileReaderOperator.java:98)
	at org.apache.flink.streaming.api.functions.source.ContinuousFileReaderOperator$ReaderState$2.prepareToProcessRecord(ContinuousFileReaderOperator.java:122)
	at org.apache.flink.streaming.api.functions.source.ContinuousFileReaderOperator.processRecord(ContinuousFileReaderOperator.java:348)
	at org.apache.flink.streaming.api.functions.source.ContinuousFileReaderOperator.lambda$new$0(ContinuousFileReaderOperator.java:240)
	at org.apache.flink.streaming.runtime.tasks.StreamTaskActionExecutor$1.runThrowing(StreamTaskActionExecutor.java:50)
	at org.apache.flink.streaming.runtime.tasks.mailbox.Mail.run(Mail.java:90)
	at org.apache.flink.streaming.runtime.tasks.mailbox.MailboxProcessor.processMailsNonBlocking(MailboxProcessor.java:353)
	at org.apache.flink.streaming.runtime.tasks.mailbox.MailboxProcessor.processMail(MailboxProcessor.java:317)
	at org.apache.flink.streaming.runtime.tasks.mailbox.MailboxProcessor.runMailboxLoop(MailboxProcessor.java:201)
	at org.apache.flink.streaming.runtime.tasks.StreamTask.runMailboxLoop(StreamTask.java:809)
	at org.apache.flink.streaming.runtime.tasks.StreamTask.invoke(StreamTask.java:761)
	at org.apache.flink.runtime.taskmanager.Task.runWithSystemExitMonitoring(Task.java:958)
	at org.apache.flink.runtime.taskmanager.Task.restoreAndInvoke(Task.java:937)
	at org.apache.flink.runtime.taskmanager.Task.doRun(Task.java:766)
	at org.apache.flink.runtime.taskmanager.Task.run(Task.java:575)
	at java.base/java.lang.Thread.run(Thread.java:834)
Caused by: java.util.zip.ZipException: Not in GZIP format
	at java.base/java.util.zip.GZIPInputStream.readHeader(GZIPInputStream.java:166)
	at java.base/java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:80)
	at java.base/java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:92)
	at org.apache.flink.api.common.io.compression.GzipInflaterInputStreamFactory.create(GzipInflaterInputStreamFactory.java:42)
	at org.apache.flink.api.common.io.compression.GzipInflaterInputStreamFactory.create(GzipInflaterInputStreamFactory.java:30)
	at org.apache.flink.api.common.io.FileInputFormat.decorateInputStream(FileInputFormat.java:902)
	at org.apache.flink.api.common.io.FileInputFormat.open(FileInputFormat.java:863)
	... 19 more
# 错误待解决
```







# Transform（操作处理）



# Sink（输出）



# Connectors（）











