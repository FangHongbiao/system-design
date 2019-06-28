package com.fhb.sso.mybatis;

import com.fhb.sso.core.SSOApplication;
import com.fhb.sso.core.domain.User;
import com.fhb.sso.core.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/27
 * Time: 17:21
 *
 * @author hbfang
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SSOApplication.class)
@Transactional
@Rollback
public class MybatisTest {

    /**
     * 注入数据查询接口
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 测试插入数据
     */
    @Test
    public void testInsert() {
        // 构造一条测试数据
        User tbUser = new User();
        tbUser.setUsername("Lusifer");
        tbUser.setPassword("123456");
//        tbUser.setTel("15112341234");
        tbUser.setSex("helo");
        tbUser.setUpdateTime(new Date());
        tbUser.setMsg("he");

        // 插入数据
        userMapper.insert(tbUser);
    }

    /**
     * 测试删除数据
     */
    @Test
    public void testDelete() {
        // 构造条件，等同于 DELETE from tb_user WHERE username = 'Lusifer'
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("username", "Lusifer");

        // 删除数据
        userMapper.deleteByExample(example);
    }

    /**
     * 测试修改数据
     */
    @Test
    public void testUpdate() {
        // 构造条件
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("username", "Lusifer");

        // 构造一条测试数据
        User tbUser = new User();
        tbUser.setUsername("Lusifer");
        tbUser.setPassword("123456");
        tbUser.setTel("15888888888");
        tbUser.setSex("男");
        tbUser.setUpdateTime(new Date());
        tbUser.setMsg("测试插入");

        // 修改数据
        userMapper.updateByExample(tbUser, example);
    }

    /**
     * 测试查询集合
     */
    @Test
    public void testSelect() {
        List<User> tbUsers = userMapper.selectAll();
        for (User tbUser : tbUsers) {
            System.out.println(tbUser.getUsername());
        }
    }
}
