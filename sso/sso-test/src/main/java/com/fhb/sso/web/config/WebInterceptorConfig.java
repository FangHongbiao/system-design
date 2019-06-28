package com.fhb.sso.web.config;

import com.fhb.sso.web.interceptor.WebInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/28
 * Time: 21:23
 *
 * @author hbfang
 */

@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {
    @Bean
    WebInterceptor webInterceptor() {
        return new WebInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webInterceptor()).addPathPatterns("/**").excludePathPatterns("/static");
    }
}
