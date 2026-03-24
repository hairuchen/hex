package me.chr.hex.core.log;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.core.R.Response.ResultCode;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        log.warn(">>> 业务异常: ", e);
        return CommonResult.failure(ResultCode.FAILED, e.getMessage());
    }

    /**
     * 全局兜底异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(Exception e) {
        log.warn(">>> 全局异常捕获", e);
        return CommonResult.failure(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * 资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public CommonResult<String> handleNoResourceFoundException(NoResourceFoundException e) {
        return CommonResult.failure(ResultCode.NOT_FOUND);
    }


    // ====================================== 请求参数校验异常 ======================================
    /**
     *  处理 请求体等 参数 外边界 异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // 1. 获取请求上下文信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = "unknown";
        String method = "unknown";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
        }
        // 分场景处理
        if (e.getMessage().contains("Required request body is missing")){
            // 请求体为空
            log.warn(">>> 请求参数异常 | 接口: [{} {}] | 详情: 请求体Http Body为空", method, uri);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED,"请求体Http Body为空");
        } else if (e.getMessage().contains("JSON parse error")&&!e.getMessage().contains("extend.Enum.")) {
            // json 格式错误
            log.warn(">>> 请求参数异常 | 接口: [{} {}] | 详情: JSON 格式错误", method, uri);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED,"JSON 格式错误");
        } else if (e.getMessage().contains("Cannot deserialize value of type") && e.getMessage().contains("from Array value") && e.getMessage().contains("JsonToken.START_ARRAY")) {
            // 预期对象，实际传入数组
            // 正则1: 匹配 "Cannot deserialize value of type `xxx` from Array value"（对象传数组）
            final Pattern OBJECT_FROM_ARRAY_PATTERN = Pattern.compile("Cannot deserialize value of type `([^`]+)` from Array value");
            // 从异常信息中提取目标类型名称
            String targetType = extractTargetType(e.getMessage(),OBJECT_FROM_ARRAY_PATTERN);
            // 拼接精准的提示信息
            log.warn(">>> 请求参数异常 | 接口: [{} {}] | 详情: 预期接收[{}}]类型的对象，实际传入了数组", method, uri,targetType);
            String tip = String.format("请求参数格式错误: 预期接收[%s]类型的对象，实际传入了数组", targetType);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED, tip);
        } else if (e.getMessage().contains("Cannot deserialize value of type") && e.getMessage().contains("from Object value") && e.getMessage().contains("JsonToken.START_OBJECT")) {
            // 预期数组，实际传入对象
            // 正则2: 匹配 "Cannot deserialize value of type `xxx` from Object value"（数组传对象）
            final Pattern ARRAY_FROM_OBJECT_PATTERN = Pattern.compile("Cannot deserialize value of type `([^`]+)` from Object value");
            String targetType = extractTargetType(e.getMessage(), ARRAY_FROM_OBJECT_PATTERN);
            log.warn(">>> 请求参数异常 | 接口: [{} {}] | 详情: 预期接收[{}}]类型的List数组，实际传入了对象", method, uri,targetType);
            String tip = String.format("请求参数格式错误: 预期接收[%s]类型的List数组，实际传入了对象", targetType);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED, tip);
        }else if (e.getMessage().contains("extend.Enum.")) {
            //枚举类型错误
            int problemIndex = e.getMessage().indexOf("problem: ");
            log.warn(">>> 请求参数异常 | 接口: [{} {}] | 详情: {}", method, uri,e.getMessage());
            return CommonResult.failure(ResultCode.VALIDATE_FAILED,e.getMessage().substring(problemIndex+9));
        } else{
            log.warn(">>> 请求参数异常 | 接口: [{} {}] | 详情: ", method, uri,e);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED,"特殊异常 请联系开发者");
        }
    }

    /**
     * 从异常信息中提取预期的目标类型名称
     * @param errorMsg 异常信息
     * @return 目标类型
     */
    private String extractTargetType(String errorMsg, Pattern pattern) {
        Matcher matcher = pattern.matcher(errorMsg);
        if (matcher.find()) {
            String fullType = matcher.group(1);
            return fullType.substring(fullType.lastIndexOf(".") + 1).replace(">","");
        }
        return "对象"; // 匹配失败时的兜底值
    }

    /**
     *  处理集合 / 数组相关的参数校验失败
     *  @NotEmpty & @Valid 的空异常
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public CommonResult<String> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.warn(">>> 请求参数异常: ", e);
        List<ParameterValidationResult> results = e.getParameterValidationResults();
        List<String> errorMessages = new ArrayList<>();

        for (ParameterValidationResult result : results) {
            // 场景1: getContainerIndex()为null → 空数组/集合本身校验失败
            if (result.getContainerIndex() == null) {
                for (MessageSourceResolvable error : result.getResolvableErrors()) {
                    String defaultMessage = error.getDefaultMessage();
                    return CommonResult.failure(ResultCode.VALIDATE_FAILED,defaultMessage);
                }
            }
            // 场景2: getContainerIndex()非null → 集合中某个元素校验失败
            else {
                // 索引+1，让提示更符合用户认知（第0组→第1组）
                int index = result.getContainerIndex() + 1;
                errorMessages.add("第" + index + "组数据:");
                for (MessageSourceResolvable error : result.getResolvableErrors()) {
                    String defaultMessage = error.getDefaultMessage();
                    if (defaultMessage != null && !defaultMessage.isEmpty()) {
                        errorMessages.add(defaultMessage);
                    }
                }
                return CommonResult.failure(ResultCode.VALIDATE_FAILED,errorMessages.toString());
            }
        }

        return CommonResult.failure(ResultCode.VALIDATE_FAILED,"特殊异常 请联系开发者",e.getMessage());
    }


    /**
     * 处理非集合 / 单个对象的字段校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 拼接错误信息
        String errorMsg = e.getBindingResult().getFieldErrors().stream().map(f -> String.format("%s",  f.getDefaultMessage())).collect(Collectors.joining(";"));
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = "unknown";
        String method = "unknown";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
        }
        log.warn(">>> 请求参数异常 | 接口: [{} {}] | 错误详情: {}", method, uri, errorMsg);
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, errorMsg);
    }


    // ====================================== Spring Security ======================================
    /**
     * 登录失败
     */
    @ExceptionHandler(BadCredentialsException.class)
    public CommonResult<String> handleBadCredentials(BadCredentialsException e) {
        log.warn(">>> 登录失败: ", e);
        return CommonResult.failure(ResultCode.FAILED, e.getMessage());
    }
    /**
     * 用户禁用 enabled属性
     */
    @ExceptionHandler(DisabledException.class)
    public CommonResult<String> handleDisabledException(Exception e) {
        log.warn(">>> 登录失败: 账户已禁用,请联系管理人员!");
        return CommonResult.failure(ResultCode.FAILED,"账户已禁用,请联系管理人员!");
    }
    /**
     * 用户锁定 accountNonLocked
     */
    @ExceptionHandler(LockedException.class)
    public CommonResult<String> handleLockedException(Exception e) {
        log.warn(">>> 登录失败: 账户已锁定");
        return CommonResult.failure(ResultCode.FAILED, "账户已锁定");
    }
    /**
     * 用户权限 AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn(">>> "+e);
        return CommonResult.failure(ResultCode.FORBIDDEN);
    }



}
