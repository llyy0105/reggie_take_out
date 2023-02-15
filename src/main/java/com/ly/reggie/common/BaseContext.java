package com.ly.reggie.common;

/**
 * @Description  基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 * @Author ly
 * @create 2023-02-10 0:36
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 设置值
    public static void setCurrendId(Long id){
        threadLocal.set(id);
    }

    // 获取值
    public static Long getCurrendId(){
        return threadLocal.get();
    }
}
