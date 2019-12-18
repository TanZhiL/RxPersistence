package com.thomas.rxpersistence;

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
 * Description:用于标记sp实体类成员变量
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SPField {
    /**
     * 是否加入sp中
     * @return
     */
    boolean save() default true;

    /**
     * 是否为commit 提交
     * @return
     */
    boolean commit() default false;

    /**
     * 如果是true全局参数通过get(name1)或者get(name2)使用都是一样的,如果是false,那么字段名会带上token(token_fieldName)
     * @return
     */
    boolean global() default true;
}