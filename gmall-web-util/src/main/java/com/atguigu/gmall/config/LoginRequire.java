package com.atguigu.gmall.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author York
 * @create 2018-11-14 20:56
 */
@Target(ElementType.METHOD)//标明注解的作用范围作用范围
@Retention(RetentionPolicy.RUNTIME)//元注解(注解的注解)
public @interface LoginRequire {
    //表示是否需要登录
    boolean autoRedirect()default true;
}
