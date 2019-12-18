package com.thomas.rxpersistence;

import java.util.Objects;

/**
 * Author: Thomas.<br/>
 * Date: 2019/12/17 8:47<br/>
 * GitHub: https://github.com/TanZhiL<br/>
 * CSDN: https://blog.csdn.net/weixin_42703445<br/>
 * Email: 1071931588@qq.com<br/>
 * Description:用于封装rx流中的数据,防止onNext(null)发生异常
 */
public class RxField<V> {
    private V value;
    public RxField(V v){
        value=v;
    }

    public V get() {
        return value;
    }

    public void set(V value) {
        this.value = value;
    }
    public boolean isNull(){
        return value==null;
    }
}
