package me.chr.hex.core.R.Response;

import lombok.Getter;

/**
 * @Author: CHR
 * @Date: create in 2025/2/28
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILED(0, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源未找到"),
    INTERNAL_SERVER_ERROR(500, "后台服务异常"); // 添加 500 异常代码
    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
