# Flink原理初探-慢慢理解/消化

## 角色分工

![1610611714750](.\img\1610611714750.png)



## 执行流程

![1610611974052](.\img\1610611974052.png)



## DataFlow

https://ci.apache.org/projects/flink/flink-docs-release-1.12/concepts/glossary.html



### DataFlow、Operator、Partition、Parallelism、SubTask

![1610612312780](.\img\1610612312780.png)

![1610612324687](.\img\1610612324687.png)

![1610612412996](.\img\1610612412996.png)



### OperatorChain和Task

![1610612647155](.\img\1610612647155.png)



### TaskSlot和TaskSlotSharing

![1610613324538](.\img\1610613324538.png)



![1610613488317](.\img\1610613488317.png)



## 执行流程图生成

![1610614330143](.\img\1610614330143.png)