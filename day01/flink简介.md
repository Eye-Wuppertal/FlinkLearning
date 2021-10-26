# Flink介绍

## 发展历史

![1610587267864](.\img\1610587267864.png)

![1610587279113](.\img\1610587279113.png)

## 官方介绍

![1610587249773](.\img\1610587249773.png)



## 组件栈

![1610587438992](.\img\1610587438992.png)

API（批处理、流处理）、核心层、部署层

## 应用场景

所有的***流式计算***



Flink1.12.0其中一些比较重要的修改包括：

1. 在 DataStream API 上添加了高效的批执行模式的支持。这是批处理和流处理实现真正统一的运行时的一个重要里程碑。
2. 实现了基于Kubernetes的高可用性（HA）方案，作为生产环境中，ZooKeeper方案之外的另外一种选择。
3. 扩展了 Kafka SQL connector，使其可以在 upsert 模式下工作，并且支持在 SQL DDL 中处理 connector 的 metadata。现在，时态表 Join 可以完全用 SQL 来表示，不再依赖于 Table API 了。
4. PyFlink 中添加了对于 DataStream API 的支持，将 PyFlink 扩展到了更复杂的场景，比如需要对状态或者定时器 timer 进行细粒度控制的场景。除此之外，现在原生支持将 PyFlink 作业部署到 Kubernetes上。

