package com.jsnws.server.common;

/**
 * 业务异常，message 会直接展示在前端的错误提示里
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}
