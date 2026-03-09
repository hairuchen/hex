package me.chr.hex.core.R.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: CHR
 * @Date: create in 2025/2/28
 */
@Data
@Schema(description = "通用响应模型")
public class CommonResult<T> implements Serializable {
    @Schema(description  = "响应码", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer code;

    @Schema(description  = "响应信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String msg;

    @Schema(description  = "响应数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private T data;

    @Schema(description  = "响应时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String costTime;

    public CommonResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> CommonResult<T> success(String message,T data) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(),message, data);
    }

    public static <T> CommonResult<T> failure() {
        return new CommonResult<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage(), null);
    }

    public static <T> CommonResult<T> failure(ResultCode resultCode) {
        return new CommonResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> CommonResult<T> failure(ResultCode resultCode, T data) {
        return new CommonResult<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    public static <T> CommonResult<T> failure(ResultCode resultCode,String message, T data) {
        return new CommonResult<>(resultCode.getCode(), message,data);
    }
}
