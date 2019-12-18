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
 * Description:用于标记缓存实体类,可缓存字段类型:String,Bitmap,Drawable,基本类型的包装类型,及其他引用类型
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEntity {
    /**
     * 如果是true全局参数通过get(name1)或者get(name2)使用都是一样的,如果是false,那么字段名会带上group(group_fieldName)
     *
     * @return
     */
    boolean global() default true;

    /**
     * 缓存方式
     * @return
     */
    CacheMode mode() default CacheMode.DOUBLE;

    /**
     * 内存缓存最大数量
     * @return
     */
    int memoryMaxCount() default 256;

    /**
     * 磁盘缓存最大数量
     * @return
     */
    int diskMaxCount() default Integer.MAX_VALUE;

    /**
     * 磁盘缓存最大容量
     * @return
     */
    long diskMaxSize() default Long.MAX_VALUE;
}
