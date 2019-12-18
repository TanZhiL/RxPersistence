package com.thomas.simple;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.thomas.rxpreferences.CacheEntity;
import com.thomas.rxpreferences.CacheField;
import com.thomas.rxpreferences.CacheMode;

import java.util.List;

/**
 * Author: Thomas.<br/>
 * Date: 2019/12/13 9:30<br/>
 * GitHub: https://github.com/TanZhiL<br/>
 * CSDN: https://blog.csdn.net/weixin_42703445<br/>
 * Email: 1071931588@qq.com<br/>
 * Description:
 */

@CacheEntity(mode = CacheMode.MEMORY,global = true, memoryMaxCount = 300,diskMaxSize = 400)
public class UserTest {
    @CacheField(saveTime = 200,save = true)
    private String name;
    @CacheField(save = true)
    private double num;
    private int age;
    private Bitmap bitmap;
    private Drawable drawable;
    private List<String> list;
    @CacheField(saveTime = 200,save = true)
    private User user;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
