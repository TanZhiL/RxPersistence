package com.thomas.rxpreferences;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Thomas.<br/>
 * Date: 2019/12/16 14:25<br/>
 * GitHub: https://github.com/TanZhiL<br/>
 * CSDN: https://blog.csdn.net/weixin_42703445<br/>
 * Email: 1071931588@qq.com<br/>
 * Description:用户标记SP实体类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SPEntity {
    /**
     * 如果是true全局参数通过get(name1)或者get(name2)使用都是一样的,如果是false,那么字段名会带上group(group_fieldName)
     * @return
     */
    boolean global() default true;
}
