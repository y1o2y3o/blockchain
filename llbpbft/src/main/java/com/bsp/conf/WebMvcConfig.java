package com.bsp.conf;

import com.bsp.interceptor.StatusInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类
 *
 * @author 3
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * statusInterceptor拦截器Bean
     *
     * @return statusInterceptor
     */
    @Bean
    public StatusInterceptor statusInterceptor() {
        return new StatusInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(statusInterceptor())
                .addPathPatterns("/message/**")
                .excludePathPatterns("/asfadsfsafs");

    }


}
