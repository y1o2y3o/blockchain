package com.bsp.conf;

import com.bsp.interceptor.CorsInterceptor;
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
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * statusInterceptor拦截器Bean
     *
     * @return statusInterceptor
     */
    @Bean
    public StatusInterceptor statusInterceptor() {
        return new StatusInterceptor();
    }

    /**
     * statusInterceptor拦截器Bean
     *
     * @return statusInterceptor
     */
    @Bean
    public CorsInterceptor corsInterceptor() {
        return new CorsInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(statusInterceptor())
                .addPathPatterns("/message/**")
                .excludePathPatterns("/asfadsfsafs");

        registry.addInterceptor(corsInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/asfadsfsafs");

    }


}
