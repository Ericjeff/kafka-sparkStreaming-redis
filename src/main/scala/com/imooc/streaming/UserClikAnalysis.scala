package com.imooc.streaming

import com.imooc.redis.RedisUtils
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies
import org.apache.spark.streaming.kafka010.ConsumerStrategies
import org.codehaus.jettison.json.JSONObject
/**
  * 分析用户的点击次数
  */
object UserClikAnalysis {

  def main(args: Array[String]): Unit = {

    //生成sparkStreming对象
    val conf = new SparkConf().setAppName("UserClikAnalysis")
                              .setMaster("local[*]")
    val ssc = new StreamingContext(conf,Seconds(10))


    val brokers = "node1:9092,node2:9092,node5:9092"
    val kafkaParams = Map[String,Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "UserClikAnalysis",
      "auto.offset.reset" -> "earliest",//"latest",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )

    val topic = Array("test")

    //从kafka中生成dStream
    val dStream = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](topic,kafkaParams)
    )

    //计算点击数,并将结果存放到redis中
    val dbIndex = 2
    val userHashKey = "app::users::click"
    dStream.foreachRDD(RDD=>
      RDD.foreachPartition(x=>
        x.foreach{records =>
          val record = new JSONObject(records.value())
          val uid = record.getString("uid")
          val clickCount = record.getInt("click_Count")
          //获取redis对象
          val jedis = RedisUtils.pool.getResource
          //redis密码
          jedis.auth("redis")
          //选择数据库(0-15)
          jedis.select(dbIndex)
          val count = jedis.hincrBy(userHashKey,uid,clickCount)
          println(count)
          RedisUtils.pool.returnResource(jedis)
        }
      )
    )

    ssc.start()
    ssc.awaitTermination()
  }
}
