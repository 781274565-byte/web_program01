//把自定义的LoginInterceptor注册到Spring MVC，并指定拦截哪些请求、不拦截哪些请求
package com.campusfasttransfer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration //把这个类标记成配置类,Spring启动时会识别它，并执行里面的MVC配置逻辑
//参与Spring MVC的配置过程
public class WebConfig implements WebMvcConfigurer {
    
    private final LoginInterceptor loginInterceptor;//定义一个成员变量，用来保存登录拦截器对象
    //Spring在创建WebConfig这个对象时，将容器中的LoginInterceptor自动传进来
    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override //重写WebMvcConfigurer接口中的方法
    //注册拦截器
    public void addInterceptors(InterceptorRegistry registry) {
        //把loginInterceptor注册到Spring MVC请求处理链中
        registry.addInterceptor(loginInterceptor)
                //指定哪些路径需要被拦截器拦截
                .addPathPatterns("/dashboard", "/files", "/files/**", "/share", "/share/**", "/admin", "/admin/**")
                //指定哪些路径不拦截
                .excludePathPatterns("/login", "/register", "/css/**", "/js/**");
    }
}
