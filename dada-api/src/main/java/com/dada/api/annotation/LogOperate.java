package com.dada.api.annotation;

import java.lang.annotation.*;

@Inherited//注解可以被继承
@Documented//注解可以被javadoc工具文档化
@Retention(RetentionPolicy.RUNTIME)//注解在运行时保留
@Target(ElementType.METHOD)//注解在方法上使用
public @interface LogOperate {//
    String module() default "";//模块
}
