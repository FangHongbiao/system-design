package com.fhb.sso.core.redis;

import com.fhb.sso.core.SSOApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/26
 * Time: 21:15
 *
 * @author hbfang
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SSOApplication.class)
public class RedisUtilTest {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void redisGet() {
        Object hahh = redisUtil.get("test");
        System.out.println(hahh == null);
        System.out.println(hahh);
    }

    @Test
    public void redisAdd() {
        redisUtil.set("name", "fhb");
    }

    @Test
    public void redisDelete() {
        redisUtil.del("name");
    }

    @Test
    public void redisModify() {
        redisUtil.set("name", "hahah");
    }
}
