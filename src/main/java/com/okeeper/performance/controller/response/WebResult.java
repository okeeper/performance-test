package com.okeeper.performance.controller.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangyue1
 */
@Data
public class WebResult<T> implements Serializable {

    public int code;

    public T data;

    public String message;

    public static <T> WebResult<T> success() {
        WebResult<T> webResult = new WebResult<>();
        webResult.setCode(0);
        return webResult;
    }


    public static <T> WebResult<T> success(T data) {
        WebResult<T> webResult = new WebResult<>();
        webResult.setCode(0);
        webResult.setData(data);
        return webResult;
    }

    public static <T> WebResult<T> error(int code, String message) {
        WebResult<T> webResult = new WebResult<>();
        webResult.setCode(code);
        webResult.setMessage(message);
        return webResult;
    }


}
