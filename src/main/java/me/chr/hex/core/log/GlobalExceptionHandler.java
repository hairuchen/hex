package me.chr.hex.core.log;

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
        log.warn(">>> 请求参数异常: ", e);
        // 正则1: 匹配 "Cannot deserialize value of type `xxx` from Array value"（对象传数组）
        final Pattern OBJECT_FROM_ARRAY_PATTERN = Pattern.compile("Cannot deserialize value of type `([^`]+)` from Array value");
        // 正则2: 匹配 "Cannot deserialize value of type `xxx` from Object value"（数组传对象）
        final Pattern ARRAY_FROM_OBJECT_PATTERN = Pattern.compile("Cannot deserialize value of type `([^`]+)` from Object value");

        // 解包异常，判断是否是枚举反序列化失败
        if (e.getCause() instanceof tools.jackson.databind.exc.InvalidFormatException) {
            tools.jackson.databind.exc.InvalidFormatException cause =
                    (tools.jackson.databind.exc.InvalidFormatException) e.getCause();
            return handleEnumInvalidFormatException(cause); // 转发到枚举处理器
        }

        // 场景1: 请求体为空
        if (e.getMessage().contains("Required request body is missing")){
            return CommonResult.failure(ResultCode.VALIDATE_FAILED,"请求体Http Body为空");
        }
        // 场景2: 预期对象，实际传入数组
        else if (e.getMessage().contains("Cannot deserialize value of type") && e.getMessage().contains("from Array value") && e.getMessage().contains("JsonToken.START_ARRAY")) {
            // 从异常信息中提取目标类型名称
            String targetType = extractTargetType(e.getMessage(),OBJECT_FROM_ARRAY_PATTERN);
            // 拼接精准的提示信息
            String tip = String.format("请求参数格式错误: 预期接收[%s]类型的对象，实际传入了数组", targetType);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED, tip);
        }
        // 场景3: 预期数组，实际传入对象
        else if (e.getMessage().contains("Cannot deserialize value of type") && e.getMessage().contains("from Object value") && e.getMessage().contains("JsonToken.START_OBJECT")) {
            String targetType = extractTargetType(e.getMessage(), ARRAY_FROM_OBJECT_PATTERN);
            String tip = String.format("请求参数格式错误: 预期接收[%s]类型的List数组，实际传入了对象", targetType);
            return CommonResult.failure(ResultCode.VALIDATE_FAILED, tip);
        }
        else{
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
        log.warn(">>> 请求参数异常: {}", errorMsg);
        return CommonResult.failure(ResultCode.VALIDATE_FAILED, "" + errorMsg);
    }

    /**
     * 处理 枚举 字段 异常
     */
    @ExceptionHandler(tools.jackson.databind.exc.InvalidFormatException.class)
    public CommonResult<String> handleEnumInvalidFormatException(InvalidFormatException e) {
        // 1. 基础信息提取
        String invalidValue = e.getValue() == null ? "null" : e.getValue().toString();
        String fieldName = "未知字段";
        Integer elementIndex = null; // 集合元素索引（从1开始）
        boolean isCollection = false; // 是否是集合场景

        // 2. 解析路径，区分集合/非集合场景
        if (!e.getPath().isEmpty()) {
            for (JacksonException.Reference ref : e.getPath()) {
                // 提取字段名（优先取非索引节点的字段名）
                if (ref.getPropertyName() != null && !ref.getPropertyName().isEmpty()) {
                    fieldName = ref.getPropertyName();
                }
                // 判断是否是集合场景（索引 != -1 说明是集合元素）
                if (ref.getIndex() != -1) {
                    isCollection = true;
                    elementIndex = ref.getIndex() + 1; // 转成用户易理解的从1开始计数
                }
            }
        }

        // 3. 枚举合法性校验
        Class<?> targetClass = e.getTargetType();
        String errorMsg;

        if (targetClass != null && targetClass.isEnum()) {
            Enum<?>[] enumConstants = (Enum<?>[]) targetClass.getEnumConstants();
            String validEnumList = Arrays.stream(enumConstants)
                    .map(Enum::toString)
                    .collect(Collectors.joining("、"));

            // 4. 区分集合/非集合拼接提示
            if (isCollection) {
                // 集合场景: 第N个元素的XX字段
                errorMsg = String.format("第%d个元素的字段【%s】传入无效枚举值【%s】，支持的有效枚举为: [%s]",
                        elementIndex, fieldName, invalidValue, validEnumList);
            } else {
                // 非集合场景: XX字段
                errorMsg = String.format("字段【%s】传入无效枚举值【%s】，支持的有效枚举为: [%s]",
                        fieldName, invalidValue, validEnumList);
            }
        } else {
            // 非枚举类型的格式错误
            if (isCollection) {
                errorMsg = String.format("第%d个元素的字段【%s】传入无效值【%s】，请检查参数类型",
                        elementIndex, fieldName, invalidValue);
            } else {
                errorMsg = String.format("字段【%s】传入无效值【%s】，请检查参数类型",
                        fieldName, invalidValue);
            }
        }

        log.warn(">>> 枚举反序列化异常: {}", errorMsg);
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
