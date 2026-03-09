package me.chr.hex.core.R.Response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.authentication.LockedException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.ValueInstantiationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    /**
     *  处理 @Valid 的参数校验异常
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public CommonResult<String> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        List<ParameterValidationResult> results = e.getParameterValidationResults();
        List<String> errorMessages = new ArrayList<>();
        for (ParameterValidationResult result : results) {
            // 遍历该参数下的所有具体错误
            errorMessages.add("第"+result.getContainerIndex()+"组数据:");
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                // 获取默认消息 (通常包含 "field: message" 或 "message")
                String defaultMessage = error.getDefaultMessage();
                if (defaultMessage != null && !defaultMessage.isEmpty()) {
                    errorMessages.add(defaultMessage);
                }
            }
        }

        // 如果上面没拿到，兜底使用 Spring 自带的格式化（可能会带 and）
        StringBuilder finalMsg = new StringBuilder();
        if (errorMessages.isEmpty()) {
            Object[] args = e.getDetailMessageArguments();
            finalMsg = new StringBuilder((args != null && args.length > 0) ? args[0].toString() : "参数校验失败");
        } else {
            // 用分号拼接： "id: xxx; id: yyy"
            for(String str:errorMessages){
                if (str.contains("组数据")&&str.contains(errorMessages.get(0).toString())){
                    finalMsg.append(str);
                }else if (str.contains("组数据")){
                    finalMsg.append(";").append(str);
                }else{
                    finalMsg.append(str).append(",");
                }
            }
        }

        log.warn(">>> 参数校验异常：{}", finalMsg);
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, finalMsg.toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        String invalidValue = "未知";
        String fieldName = "未知字段";
        Class<?> targetClass = null;
        List<JacksonException.Reference> path = null;

        // 情况 1: 普通枚举匹配失败 (无 @JsonCreator)
        if (cause instanceof tools.jackson.databind.exc.InvalidFormatException ife) {
            invalidValue = ife.getValue() == null ? "null" : ife.getValue().toString();
            fieldName = ife.getPath().isEmpty() ? "未知字段" : ife.getPath().get(0).getPropertyName();
            targetClass = ife.getTargetType();
            path = ife.getPath();
        }
        // 情况 2: @JsonCreator 工厂方法执行失败 (有 @JsonCreator) -> 新增分支
        else if (cause instanceof ValueInstantiationException vie) {
            targetClass = vie.getType().getRawClass();
            path = vie.getPath();
            fieldName = (path != null && !path.isEmpty()) ? path.get(0).getPropertyName() : "未知字段";

            // 从异常消息或 Cause 中提取值
            Throwable rootCause = vie.getCause();
            if (rootCause != null && rootCause.getMessage() != null) {
                String msg = rootCause.getMessage();
                if (msg.contains("No enum constant")) {
                    // 提取 PRODUCT_NAME1
                    String[] parts = msg.split("\\.");
                    invalidValue = parts[parts.length - 1].trim();
                } else {
                    invalidValue = msg; // 自定义消息
                }
            }
        }

        // 如果成功提取了枚举类型，则生成友好提示
        if (targetClass != null && targetClass.isEnum()) {
            Enum<?>[] enumConstants = (Enum<?>[]) targetClass.getEnumConstants();
            String validEnumList = Arrays.stream(enumConstants)
                    .map(Enum::toString)
                    .collect(Collectors.joining("、"));

            String errorMsg = String.format("字段【%s】传入无效枚举值【%s】，支持的有效枚举为：[%s]",
                    fieldName, invalidValue, validEnumList);

            log.warn(">>> 枚举反序列化异常：{}", errorMsg);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED, errorMsg);
        }

        // 兜底
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, "JSON 格式错误或参数类型不匹配");
    }

    /**
     * 保留原有的 InvalidFormatException 处理（防止直接抛出该异常时未被拦截）
     */
    @ExceptionHandler(tools.jackson.databind.exc.InvalidFormatException.class)
    public CommonResult<String> handleEnumInvalidFormatException(InvalidFormatException e) {
        String invalidValue = e.getValue() == null ? "null" : e.getValue().toString();
        String fieldName = e.getPath().isEmpty() ? "未知字段" : e.getPath().get(0).getPropertyName();
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
