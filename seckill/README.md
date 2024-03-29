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
    解决方案: 更新的同时需要判断库.(update操作是加锁的，所以两个条件可同时满足)
    ```
    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0}")
    public int reduceStock(MiaoshaGoods g);
    ```


### 细节问题的处理方案
1. 商品超卖
解决方案: 通过数据库锁实现。更新的同时需要判断库.(update操作是加锁的，所以两个条件可同时满足)
```
@Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0}")
public int reduceStock(MiaoshaGoods g);
```
2. 同一用户秒杀到多个商品(当用户同时发出了两个请求，间隔非常短时，有可能出现)
解决方案: 借助数据库唯一索引实现。对miaosha_order表中user_id和goods_id做唯一索引



### 秒杀方案优化
1. 页面优化技术
    1. 页面缓存 + URL缓存 + 对象缓存
        1. 页面 和 URL缓存
            1. 适用场景: 页面变化不大(例如商品列表，商品详情), 缓存时间比较短
            1. 流程
                1. 取缓存
                2. 手动渲染模板
                3. 结果输出
        2. 对象缓存
    2. 页面静态化， 前后端分离
        1. 优点: 可以充分利用浏览器缓存
    3. 静态资源优化
        1. JS/CSS压缩，减少流量
        2. 多个JS/CSS组合，减少连接数
    4. CDN优化
2. 秒杀接口优化
    1. 设计方向
        1. Redis预减库存，减少数据库访问
        2. 内存标记减少Redis访问
        3. 请求先入队缓冲，异步下单，增强用户体验
        4. Nginx水平扩展
    2. 整体流程
        1. 系统初始化，把商品库存数量加载到Redis
        2. 收到请求，Redis预减库存，库存不足，直接返回，否则进入3
        3. 请求入队，立即返回排队中
        4. 请求出队，生成订单，减少库存
        5. 客户端轮询，是否秒杀成功
        6. 本地使用HaspMap对秒杀完的商品进行缓存，减少Redis访问
3. 安全优化
    1. 秒杀接口隐藏
        1. 思路: 秒杀开始之前, 先去请求接口获取秒杀地址
        2. 具体流程
            1. 接口改造, 带上PathVariable参数
            2. 添加生成地址的接口
            3. 秒杀收到请求，先验证PathVariable
    2. 数学公式验证码
        1. 思路: 点击秒杀前, 使用验证码防止机器刷接口, 同时起到分散用户请求的作用
        2. 具体流程
            1. 添加生成验证码的接口
            2. 在获取秒杀路径的时候, 验证验证码
            3. ScriptEngine的使用
    3. 接口限流防刷
        1. 可选方案:
            1. 定时器计时技术, 定时器需要刷新
            2. 充分利用缓存. 设置有效期
        2. 代码优化: 使用拦截器和自定义注解减少对业务的侵入
            1. 自定义访问控制注解
            ```java
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            public @interface AccessLimit {

                int seconds();
                int maxCount();
                boolean needLogin() default true;
            }

            ```
            2. 自定义Interceptor拦截请求,获取注解信息
            ```java
            @Service
            public class AccessInterceptor extends HandlerInterceptorAdapter {

                @Autowired
                MiaoshaUserService userService;

                @Autowired
                RedisService redisService;

                @Override
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

                    if (handler instanceof HandlerMethod) {

                        MiaoshaUser user = getUser(request, response);

                        UserContext.setUser(user);

                        HandlerMethod hm = (HandlerMethod) handler;

                        AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

                        if (accessLimit == null) {
                            return true;
                        }


                        int seconds = accessLimit.seconds();
                        int maxCount = accessLimit.maxCount();
                        boolean needLogin = accessLimit.needLogin();

                        String key = request.getRequestURI();

                        if (needLogin) {
                            if (user == null) {
                                render(response, CodeMsg.SESSION_ERROR);
                                return false;
                            }
                            key += "_" + user.getId();
                        }

                        AccessKey ak = AccessKey.withExpire(seconds);
                        Integer count = redisService.get(ak, key, Integer.class);
                        if (count == null) {
                            redisService.set(ak, key, 1);
                        } else if (count < maxCount) {
                            redisService.incr(ak, key);
                        } else {
                            render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                            return false;
                        }


                        return true;
                    }

                    return true;
                }
            ```
            3. 添加拦截器
            ```java
            @Configuration
            public class WebConfig  extends WebMvcConfigurerAdapter{

                @Autowired
                UserArgumentResolver userArgumentResolver;

                @Autowired
                AccessInterceptor accessInterceptor;

                @Override
                public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
                    argumentResolvers.add(userArgumentResolver);
                }

                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(accessInterceptor);
                }
            }
            ```


### 教程bug
1. 超卖问题解决方案，存在扣减库存失败却写入了订单 (接口优化中进行了改正)
```java
@Transactional
public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
    //减库存 下订单 写入秒杀订单
    goodsService.reduceStock(goods);
    //order_info maiosha_order
    return orderService.createOrder(user, goods);
}
```

### 遇到的问题
1. idea 连接不上 mysql(08001错误): [mysql8.0驱动问题](https://blog.csdn.net/m0_37713761/article/details/89735831)
2. redis 问题(Caused by: redis.clients.jedis.exceptions.JedisDataException: ERR Client sent AUTH, but no password is set)
    1. 配置文件提供了redis.password就会以认证的方式请求服务器，即使密码为空也会出错
    2. 配置文件中注释掉`#redis.password=`即可
    2. [参考资料](https://blog.csdn.net/rchm8519/article/details/48347797)
3. RabbitMQ连接出错: guest默认不允许远程连接
    1. `org.springframework.amqp.rabbit.listener.exception.FatalListenerStartupException: Authentication failure`
    2. [解决方案](https://www.rabbitmq.com/access-control.html): rabbitmq/etc/rabbitmq下新建一个rabbitmq.config文件，写入`[{rabbit, [{loopback_users, []}]}].`
4. 设置了topic交换机,但是却都能收到消息
在一开始的时候绑定错了，后来虽然修改了绑定，但是rabbitmq里保存着原来的，将错误的解绑即可

### 性能测试截图
1. basic版本

2. basic + 页面缓存 + URL缓存 + 对象缓存 (差不多3倍）
    - 优化了`商品列表`, `商品详情`, `获取用户信息`(com.fhb.seckill.controller.GoodsController.list, com.fhb.seckill.controller.GoodsController.detail, com.fhb.seckill.service.MiaoshaUserService.getById)
    ![](../images/seckill-v1.1.png)

3. 前后端分离，页面静态化


### 需要思考的问题
1. 秒杀系统中的MQ该如何选择
    1. [不同MQ之间的对比]
2. 前后端问题，前后端认证的同步问题
3. 在很多Controller中都要判断user是否为空，拦截器是更好的方法，然后用注解@NeedLogin来检测登录

### 知识点
1. 使用HandlerMethodArgumentResolver，根据token进行全局的user对象注入
```java
// 实现HandlerMethodArgumentResolver接口
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    MiaoshaUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz==MiaoshaUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

// 添加配置信息
@Configuration
public class WebConfig  extends WebMvcConfigurerAdapter{

    @Autowired
    UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }
}
```
2. 拦截器的使用
3. 手动渲染模板
```java

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    // 手动渲染模板
    SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(),
            request.getLocale(), model.asMap(), applicationContext);

    html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);

    if (!StringUtils.isEmpty(html)) {
        redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
    }
```
3. 修改数据库时，缓存需要同步更新: 遵循cache aside pattern (在使用对象缓存时需要注意)
    1. 读数据时，先读缓存，缓存没有，再读数据库，同时把数据库值写入缓存
    2. 修改数据时，先修改数据库，修改成功删除缓存
4. 为什么不同模块调用时，需要调用其他模块的service而不调用dao？
答: 因为service层中可能使用了缓存，效率高
5. Get与Post提交方式的区别
    1. GET幂等
6. [Springboot 配置静态页面缓存](https://docs.spring.io/spring-boot/docs/1.5.21.RELEASE/reference/htmlsingle/)
```
spring.resources.add-mappings=true # Enable default resource handling.
spring.resources.cache-period= # Cache period for the resources served by the resource handler, in seconds.
spring.resources.chain.cache=true # Enable caching in the Resource chain.
spring.resources.chain.enabled= # Enable the Spring Resource Handling chain. Disabled by default unless at least one strategy has been enabled.
spring.resources.chain.gzipped=false # Enable resolution of already gzipped resources.
spring.resources.chain.html-application-cache=false # Enable HTML5 application cache manifest rewriting.
spring.resources.chain.strategy.content.enabled=false # Enable the content Version Strategy.
spring.resources.chain.strategy.content.paths=/** # Comma-separated list of patterns to apply to the Version Strategy.
spring.resources.chain.strategy.fixed.enabled=false # Enable the fixed Version Strategy.
spring.resources.chain.strategy.fixed.paths=/** # Comma-separated list of patterns to apply to the Version Strategy.
spring.resources.chain.strategy.fixed.version= # Version string to use for the Version Strategy.
spring.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/ # Locations of static resources.
```
7. RabbitMQ安装
    1. 安装erlang
        1. 下载erlang源码`wget http://erlang.org/download/otp_src_22.0.tar.gz`
        2. 安装依赖 `yum install ncurses-devel`
        3. 解压
        3. 配置 `./configure --prefix=/usr/local/erlang20 --without-javac`
        4. 编译 `make -j 4`

    2. 安装RabbitMQ
        1. 下载RabbitMQ `https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.7.15/rabbitmq-server-generic-unix-3.7.15.tar.xz`
        2. 解压
            1. `xz -d [file_name.xz]`
            2. `tar xf [file_name.tar]`
        3. 安装依赖
            1. `yum install python -y`
            2. `yum install xmlto -y`
            3. `yum install python-simplejson -y`
        4. mv ... /usr/local/rabbitmq
    3. 配置erlang和rabbitmq的环境变量
    4. 与springboot的集成
        1. 添加依赖spring-boot-starter-amqp
        2. 创建消息接受者
        3. 创建消息发送者
    5. 安装web管理插件 `rabbitmq-plugins enable rabbitmq_management`
8. springboot系统初始化做一些工作： Controller实现InitializingBean接口中的afterPropertiesSet方法
9. git tag用法
```
git tag　　//查看tag
git tag test_tag c809ddbf83939a89659e51dc2a5fe183af384233　　　　//在某个commit 上打tag
git tag
...
git push origin test_tag　　　　//!!!本地tag推送到线上
...
git tag -d test_tag　　　　　　　　//本地删除tag
git push origin :refs/tags/test_tag　　　　//本地tag删除了，再执行该句，删除线上tag
```