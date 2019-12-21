[![](https://jitpack.io/v/TanZhiL/RxPersistence.svg)](https://jitpack.io/#TanZhiL/RxPersistence)

RxPersistence是基于面向对象设计的快速持久化框架，目的是为了简化SharePreferences及本地缓存Cache的使用，减少代码的编写。可以非常快速地保存基本类型和对象。RxPersistence是基于APT技术实现，在编译期间实现代码的生成，支持混淆。根据不同的用户区分持久化信息。

### 特点
1. 把通过的Javabean变成SharedPreferences操作类
2. 把通过的Javabean变成Cache操作类
3. 支持保存基本类型及对象
4. 支持根据不同的用户区分持久化信息
5. 支持Rxjava
6. 可支持自定义缓存方式,如内存缓存,磁盘缓存及二级缓存

### 简单例子
##### 定义javabean类
```
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
	
	
	@SPEntity(global = false)
public class UserProfile {
    @SPField(commit = true,save = true)    
    private String name;
    @SPField(commit = true,save = true,global = false)
    private double num;
    private int age;
```
##### 使用方式
```
//初始化
    RxPersistence.init(this, new PersistenceParser() {
            @Override
            public Object deserialize(Type clazz, String text) {
                return new Gson().fromJson(text,clazz);
            }

            @Override
            public String serialize(Object object) {
                return new Gson().toJson(object);
            }
        });
	RxPersistence.setUserGroups("用户组ID");//可选配置
    RxPersistence.setUserTokens("用户ID");//可选配置
// 保存信息
UserTestMemoryCache.get().setName()
 
UserProfileSP.get().setAge();
UserProfileSP.get().setAgeRx();
// 获取信息

 UserTestMemoryCache.get().getName()
 
UserProfileSP.get().getAge();
UserProfileSP.get().getAgeRx();

```
从上面的简单例子可以看到，我们需要做SharePreferences持久化，仅仅定义一个简单的javabean类（UserProfileSP）并添加注解即可，这个框架会根据javabean生成带有持久化功能的UserProfile 类，通过这个类就可以非常简单去保持或者获取数据，大大简化了SharePreferences的使用，也可以保持对象。
### 项目地址
https://github.com/TanZhiL/RxPersistence
### 一、配置项目

##### 配置项目根目录 build.gradle
```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```
##### 配置app build.gradle
```
apply plugin: 'com.android.application'


//...
dependencies {
     implementation 'com.github.TanZhiL.RxPersistence:rxpersistence:0.0.6'
    annotationProcessor 'com.github.TanZhiL.RxPersistence:rxpersistence-compiler:0.0.6'
    implementation 'com.google.code.gson:gson:2.8.6'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.0'
}
```

### 二、定义持久化Javabean

使用方法非常简单，先编写一个普通带getter、setter的javabean类，在类头部添加@SPEntity或者@CacheEntity：

```
//Sharepreference
	@SPEntity(global = false)
public class UserProfile {
    @SPField(commit = true,save = true)    
    private String name;
    @SPField(commit = true,save = true,global = false)
    private double num;
    private int age;
    // ...

    // get、set方法必须写
}
//Cache
@CacheEntity(mode = CacheMode.MEMORY,global = true, memoryMaxCount = 300,diskMaxSize = 400)
public class UserTest {
    @CacheField(saveTime = 200,save = true)
    private String name;
    @CacheField(save = true)
    private double num;
    private int age;
    private Bitmap bitmap;
    private Drawable drawable;
```



### 三、注解及使用说明
请参考注解注释,及simple


### 四、初始化

使用之前要进行初始化，建议在Application进行初始化，需要需要保存对象，还需要实现对象的解析器，这里使用Gson作为实例：

```

//初始化
    RxPersistence.init(this, new PersistenceParser() {
            @Override
            public Object deserialize(Type clazz, String text) {
                return new Gson().fromJson(text,clazz);
            }

            @Override
            public String serialize(Object object) {
                return new Gson().toJson(object);
            }
        });
	RxPersistence.setUserGroups("用户组ID");//可选配置
    RxPersistence.setUserTokens("用户ID");//可选配置

```





### 五、根据不同的用户设置
如果app支持多用户登录，需要根据不用的用户持久化，可以通过下面方法配置。再通过@SPField(global = false)，就可以针对某个字段跟随用户不同进行持久化。
```
	RxPersistence.setUserGroups("用户组ID");//可选配置
    RxPersistence.setUserTokens("用户ID");//可选配置
```

### 六、代码调用

```

// 普通类型保存
SettingsPreferences.get().setUseLanguage("zh");
SettingsPreferences.get().setLastOpenAppTimeMillis(System.currentTimeMillis());
// 对象类型保存
Settings.LoginUser loginUser = new Settings.LoginUser();
loginUser.setUsername("username");
loginUser.setPassword("password");
SettingsPreferences.get().setLoginUser(loginUser);


// 获取
String useLanguage = settingsPreference.getUseLanguage();
Settings.LoginUser loginUser1 = settingsPreference.getLoginUser();
boolean openPush = settingsPreference.getPush().isOpenPush();
```

### 七、默认值

很多时候我们需要在没有获取到值时使用默认值，SharedPreferences本身也是具备默认值的，所以我们也是支持默认值配置。分析生成的代码可以看到：

```

@Override
public long getLastOpenAppTimeMillis() {
   return mPreferences.getLong("lastOpenAppTimeMillis", super.getLastOpenAppTimeMillis());
}

```

如果没有获取到值，会调用父类的方法，那么就可以通过这个方式配置默认值：

```

@SPEntity
public class Settings {
   // 使用commit提交，默认是使用apply提交，配置默认值
   @SPField(commit = true)
   private String useLanguage = "zh";

   // ...

}

```

### 致谢
* 感谢所有开源库的大佬
* https://github.com/Blankj/AndroidUtilCode
* https://github.com/taoweiji/AptPreferences
### 问题反馈
欢迎加星，打call https://github.com/TanZhiL/RxPersistence
* email：1071931588@qq.com
### 关于作者
谭志龙
### 开源项目
* 快速切面编程开源库 https://github.com/TanZhiL/OkAspectj
* 高仿喜马拉雅听Android客户端 https://github.com/TanZhiL/Zhumulangma
* 骨架屏弹性块 https://github.com/TanZhiL/SkeletonBlock
* RxPersistence是基于面向对象设计的快速持久化框架 https://github.com/TanZhiL/RxPersistence



## License

    Copyright 2019 Thomas, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
