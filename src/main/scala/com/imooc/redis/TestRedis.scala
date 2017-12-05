package com.imooc.redis

object TestRedis {

  def main(args: Array[String]): Unit = {
    val jedis = RedisUtils.pool.getResource
    jedis.auth("redis")
    jedis.select(0)
    val s = jedis.set("testRedis_+++++","hello world!!! redis????")
    jedis.close()
    println(s)
  }
}
