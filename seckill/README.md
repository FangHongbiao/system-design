### 搭建秒杀业务框架
1. 整合相关模块
    1. 整合Redis
    2. 整合Mybatis
    3. 整合MQ
        1. RabbitMQ
        2. Kafka
2. 分布式session
3. 商品列表页开发
4. 商品详情页开发
5. 对系统对初步压测
    1. 暴露的问题: 商品超卖

### 秒杀方案优化
1. 页面优化技术
    1. 页面缓存 + URL缓存 + 对象缓存
    2. 页面静态化， 前后端分离
    3. 静态资源优化
    4. CDN优化

### 遇到的问题
1. idea 连接不上 mysql(08001错误): [mysql8.0驱动问题](https://blog.csdn.net/m0_37713761/article/details/89735831)
2. redis 问题(Caused by: redis.clients.jedis.exceptions.JedisDataException: ERR Client sent AUTH, but no password is set)
    1. 配置文件提供了redis.password就会以认证的方式请求服务器，即使密码为空也会出错
    2. 配置文件中注释掉`#redis.password=`即可
    2. [参考资料](https://blog.csdn.net/rchm8519/article/details/48347797)

### 需要思考的问题
1. 秒杀系统中的MQ该如何选择
    1. [不同MQ之间的对比]