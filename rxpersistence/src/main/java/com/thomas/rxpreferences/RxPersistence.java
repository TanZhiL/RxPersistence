package com.thomas.rxpreferences;

import android.app.Application;
import android.content.Context;

public final class RxPersistence {
    private static Context mContext;

    private static PersistenceParser mPersistenceParser;
    /**
     * 用户令牌决定sp数据的key,用于区分用户独有数据
     */
    private static String[] mUserTokens;
    /**
     * group决定sp文件名称,用于对内容分组共享
     */
    private static String[] mGroupTokens;

    public static void init(Context context, PersistenceParser persistenceParser) {
        if(!(context instanceof Application))
            context=context.getApplicationContext();
        mContext = context;
        mPersistenceParser = persistenceParser;
    }
    public static Context getContext() {
        return mContext;
    }

    public static PersistenceParser getParser() {
        return mPersistenceParser;
    }
    public static void setUserTokens(String... userTokens) {
        mUserTokens = userTokens;
    }
    public static void setUserGroups(String... userGroups) {
        mGroupTokens = userGroups;
    }
    public static String getUserToken() {
        if(mUserTokens.length==0){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < mUserTokens.length; i++) {
            sb.append(mUserTokens[i]).append("_");
        }
        return sb.toString();
    }
    public static String getGroupToken() {
        if(mGroupTokens.length==0){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < mGroupTokens.length; i++) {
            sb.append(mGroupTokens[i]).append("_");
        }
        return sb.toString();
    }

}
