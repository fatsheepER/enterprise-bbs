package com.feiyang.bbs.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(40000, "请求参数错误"),
    LOGIN_FAILED(40001, "用户名或密码错误"),
    USERNAME_EXISTS(40002, "用户名已存在"),
    BOARD_NAME_EXISTS(40003, "版块名称已存在"),
    OLD_PASSWORD_ERROR(40004, "旧密码错误"),
    UNAUTHORIZED(40100, "请先登录"),
    FORBIDDEN(40300, "无权限访问"),
    NOT_FOUND(40400, "资源不存在"),
    RESOURCE_UNAVAILABLE(40900, "资源状态不可用"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
