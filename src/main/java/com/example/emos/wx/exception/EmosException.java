package com.example.emos.wx.exception;

import lombok.Data;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 10:05 2022/3/16
 */

@Data
public class EmosException extends RuntimeException {
    /**
     * 异常信息
     */
    private String msg;
    /**
     * 状态码
     */
    private int code = 500;

    public EmosException(String msg) {
        this.msg = msg;
    }

    public EmosException(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public EmosException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public EmosException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}