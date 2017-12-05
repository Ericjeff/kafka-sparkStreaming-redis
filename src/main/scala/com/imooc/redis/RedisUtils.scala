package com.imooc.redis

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.JedisPool

object RedisUtils {

  private val host = "node1"
  private val port = 6379
  //private val poolConfig = new GenericObjectPoolConfig()
  lazy val pool = new JedisPool(host,port)

  //关闭
  lazy val hooks = new Thread(){
    override def run(): Unit ={
      println("Execute hook thread: " + this)
      pool.destroy()
    }
  }
}
