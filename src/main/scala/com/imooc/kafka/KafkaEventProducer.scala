package com.imooc.kafka

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.codehaus.jettison.json.JSONObject

import scala.util.Random

/**
  * 编写一个提交数据到kafka集群的producer
  */
object KafkaEventProducer {

  //用户数据id
  private val users = Array(
    "4A4D769EB9679C054DE81B973ED5D768", "8dfeb5aaafc027d89349ac9a20b3930f",
    "011BBF43B89BFBF266C865DF0397AA71", "f2a8474bf7bd94f0aabbd4cdd2c06dcf",
    "068b746ed4620d25e26055a9f804385f", "97edfc08311c70143401745a03a50706",
    "d7f141563005d1b5d0d3dd30138f3f62", "c8ee90aade1671a21336c721512b817a",
    "6b67c8c700427dee7552f81f3228c927", "a95f22eabc4fd4b580c011a3161a9d9d"
  )

  private var pointer = -1
  //获得用户的ID
  def getUserId():String={
    pointer = (pointer+1)%users.length
    users(pointer)
  }

  //获取点击数
  val random = new Random()
  def clickCount():Int={
    random.nextInt(users.length)
  }

  //获取点击时间
  def getTime():Long={
    System.currentTimeMillis()
  }

  //获取手机类型
  val phonesType = Array(
    "iphone","xiaomi","huawei","vivo","oppo","sangxin","jinli"
  )

  def getPhoneType():String={
    phonesType(random.nextInt(phonesType.length))
  }


  def main(args: Array[String]): Unit = {

    val topic = "test"
    val brokers = "node1:9092,node2:9092,node5:9092"
    //设置属性,配置
    val props = new Properties()
    props.setProperty("bootstrap.servers",brokers)
    props.setProperty("metadata.broker.list",brokers)
    props.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer")
    props.setProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer")

    //生成producer对象
    val producer = new KafkaProducer[String,String](props)

    //传输数据
    while(true){
      val event = new JSONObject()
      event.put("uid",getUserId())
        .put("event_time",getTime())
        .put("os_type",getPhoneType())
        .put("click_Count",clickCount())

      println(event.toString())
      //发送数据
      producer.send(new ProducerRecord[String,String](topic,event.toString))
      Thread.sleep(200)
    }
  }
}
