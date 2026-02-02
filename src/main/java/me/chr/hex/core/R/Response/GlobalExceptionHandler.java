package me.chr.hex.core.R.Response;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * @Author: CHR
 * @Date: create in 2025/2/28
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public CommonResult<String> handleBizException(BizException e) {
        log.warn(">>> 业务异常：{}", e.getMessage());
        return CommonResult.failure(ResultCode.FAILED, e.getMessage());
    }

//    /**
//     * 用户锁定/禁用
//     */
//    @ExceptionHandler({LockedException.class, DisabledException.class})
//    public CommonResult<String> handleUserStatusException(Exception e) {
//        log.warn(">>> 用户状态异常：{}", e.getMessage());
//        return CommonResult.failure(ResultCode.FAILED, "用户已禁用/已锁定");
//    }
//
//    /**
//     * 登录失败
//     */
//    @ExceptionHandler(BadCredentialsException.class)
//    public CommonResult<String> handleBadCredentials(BadCredentialsException e) {
//        log.warn(">>> 登录失败：{}", e.getMessage());
//        return CommonResult.failure(ResultCode.FAILED, "登录失败，请检查用户名和密码");
//    }
//
//    //此处异常前置到security过滤器
////    @Deprecated
////    /**
////     * token校验
////     */
////    // 新增：专门处理认证相关异常（返回 401）
////    @ExceptionHandler(AuthenticationException.class)
////    public CommonResult<String> handleAuthenticationException(AuthenticationException e) {
////        log.warn(">>> 认证异常：{}", e.getMessage());
////        return CommonResult.failure(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
////    }
//
//    /**
//     * 权限不足
//     */
//    @ExceptionHandler(AccessDeniedException.class)
//    public CommonResult<String> handleAccessDenied(AccessDeniedException e) {
//        log.warn(">>> 权限不足：{}", e.getMessage());
//        return CommonResult.failure(ResultCode.FORBIDDEN, e.getMessage());
//    }

//    /**
//     * 静态资源请求
//     */
//    @ExceptionHandler(NoResourceFoundException.class)
//    public ResponseEntity<Void> handleStaticNotFound(NoResourceFoundException ex) {
//        // 静态资源 404 直接返回空 404，不记录堆栈
//        return ResponseEntity.notFound().build();
//    }

    /**
     * 捕获@RequestBody参数校验异常，返回自定义友好提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 一行拼接错误信息：字段名+提示，简洁高效
        String errorMsg = e.getBindingResult().getFieldErrors().stream().map(f -> String.format("%s",  f.getDefaultMessage())).collect(Collectors.joining(";"));
        // 第一行：日志输出（与权限异常日志格式一致）
        log.warn(">>> 参数校验异常：{}", errorMsg);
        // 第二行：返回结果（与权限异常返回格式一致，两行核心逻辑搞定）
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, "参数校验失败：" + errorMsg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // 1. 提取嵌套的 InvalidFormatException
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
            // 2. 复用原有枚举异常处理逻辑
            String invalidValue = invalidFormatException.getValue() == null ? "null" : invalidFormatException.getValue().toString();
            // 修复：getPath() 可能为空，兼容处理；get(0) 替换为 getFirst()（或保持 get(0)，两者功能一致）
            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "未知字段"
                    : invalidFormatException.getPath().get(0).getFieldName();
            Class<?> targetClass = invalidFormatException.getTargetType();
            String errorMsg;

            // 3. 判断是否为枚举类型，拼接友好提示
            if (targetClass != null && targetClass.isEnum()) {
                Enum<?>[] enumConstants = (Enum<?>[]) targetClass.getEnumConstants();
                String validEnumList = Arrays.stream(enumConstants)
                        .map(Enum::toString)
                        .collect(Collectors.joining("、"));
                errorMsg = String.format("字段【%s】传入无效枚举值【%s】，支持的有效枚举为：[%s]",
                        fieldName, invalidValue, validEnumList);
            } else {
                errorMsg = String.format("字段【%s】传入无效值【%s】，请检查参数类型", fieldName, invalidValue);
            }

            log.warn(">>> 枚举反序列化异常（JSON解析失败）：{}", errorMsg);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED, errorMsg);
        }

        // 非 InvalidFormatException 导致的 JSON 解析失败，返回默认提示
        String defaultMsg = "JSON 格式错误或参数类型不匹配，请检查请求参数";
        log.warn(">>> JSON 解析异常：{}", defaultMsg, e);
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, defaultMsg);
    }

    /**
     * 保留原有的 InvalidFormatException 处理（防止直接抛出该异常时未被拦截）
     */
    @ExceptionHandler(InvalidFormatException.class)
    public CommonResult<String> handleEnumInvalidFormatException(InvalidFormatException e) {
        String invalidValue = e.getValue() == null ? "null" : e.getValue().toString();
        String fieldName = e.getPath().isEmpty() ? "未知字段" : e.getPath().get(0).getFieldName();
        Class<?> targetClass = e.getTargetType();
        String errorMsg;

        if (targetClass != null && targetClass.isEnum()) {
            Enum<?>[] enumConstants = (Enum<?>[]) targetClass.getEnumConstants();
            String validEnumList = Arrays.stream(enumConstants)
                    .map(Enum::toString)
                    .collect(Collectors.joining("、"));
            errorMsg = String.format("字段【%s】传入无效枚举值【%s】，支持的有效枚举为：[%s]",
                    fieldName, invalidValue, validEnumList);
        } else {
            errorMsg = String.format("字段【%s】传入无效值【%s】，请检查参数类型", fieldName, invalidValue);
        }

        log.warn(">>> 枚举反序列化异常：{}", errorMsg);
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, errorMsg);
    }


    /**
     * 全局兜底异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(Exception e) {
        log.error(">>> 全局异常捕获", e);
        return CommonResult.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
