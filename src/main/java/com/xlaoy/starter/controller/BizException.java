package com.xlaoy.starter.controller;


public class BizException extends RuntimeException {

    private String errorKey;

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }

}
