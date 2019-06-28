package com.fhb.sso.core.utils;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/27
 * Time: 11:03
 *
 * @author hbfang
 */

public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {
}