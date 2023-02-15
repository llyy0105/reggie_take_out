package com.ly.reggie.common;

/**
 * @Description  自定义业务异常
 * @Author ly
 * @create 2023-02-10 17:40
 */
public class CustomException extends RuntimeException{

    public CustomException(String message){
        super(message);
    }
}
