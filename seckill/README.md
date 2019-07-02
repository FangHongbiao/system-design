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

### 教程bug
1. 超卖问题解决方案，存在扣减库存失败却写入了订单
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
