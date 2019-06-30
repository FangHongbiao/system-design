package com.fhb.sso.core.service;

import com.fhb.common.domain.User;
import com.fhb.sso.core.mapper.UserMapper;
import com.fhb.sso.core.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/27
 * Time: 10:49
 *
 * @author hbfang
 */

@Component
public class LoginService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    public User login(String username, String password) {

        Object obj = redisUtil.get(username);

        if (obj == null) {
            Example example = new Example(User.class);

            example.createCriteria().andEqualTo("username", username);

            User user = userMapper.selectOneByExample(example);

            if (user != null) {
                if (password.equals(user.getPassword())) {
                    redisUtil.set(username, user, 60 * 60 * 24);
                    return user;
                } else {
                    return null;
                }
            } else {
                return null;
            }

        } else {
            return (User) obj;
        }
    }
}
