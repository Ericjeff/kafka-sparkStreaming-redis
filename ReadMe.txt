kafka+spark Streaming +redis
===================================

    手机客户端会收集用户的行为事件（我们以点击事件为例），将数据发送到数据服务器，我们假设这里直接进入到Kafka消息队列
    后端的实时服务会从Kafka消费数据，将数据读出来并进行实时分析，这里选择Spark Streaming，因为Spark Streaming提供了与Kafka整合的内置支持
    经过Spark Streaming实时计算程序分析，将结果写入Redis，可以实时获取用户的行为数据，并可以导出进行离线综合统计分析

1.KafkaEventProducer
    首先，写了一个Kafka Producer模拟程序，用来模拟向Kafka实时写入用户行为的事件数据，数据是JSON格式

一个事件包含4个字段：
    uid：用户编号
    event_time：事件发生时间戳
    os_type：手机App操作系统类型
    click_count：点击次数

2.RedisUtils

Jedis客户端来操作Redis，将分组计数结果数据累加写入Redis存储，如果其他系统需要实时获取该数据，直接从Redis实时读取即可

   在Spark集群环境部署Application后，在进行计算的时候会将作用于RDD数据集上的函数（Functions）发送到集群中Worker上的Executor上（在Spark Streaming中是作用于DStream的操作），
那么这些函数操作所作用的对象（Elements）必须是可序列化的，通过Scala也可以使用lazy引用来解决，否则这些对象（Elements）在跨节点序列化传输后，
无法正确地执行反序列化重构成实际可用的对象。上面代码我们使用lazy引用（Lazy Reference）来实现的


3.UserClikAnalysis
    现实时统计每个用户的点击次数，它是按照用户分组进行累加次数，逻辑比较简单，关键是在实现过程中要注意一些问题，如对象序列化等。先看实现代码