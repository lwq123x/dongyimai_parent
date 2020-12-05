package com.offcn.entity;

import java.io.Serializable;

/**
 * 更新方法的复合实体类
 */
public class Result implements Serializable {
    public boolean success; //更新标识
    public String message; //提示语

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
