package me.chr.hex.core.log;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Author: CHR
 * @Date: create in 2026/2/28
 **/
@Slf4j
@Aspect
@Component
public class RequestLogAspect {

    private static String show="[隐藏: 该类未实现 Loggable 接口,不打印请求日志！]";

    // 拦截所有被 @RestController 注解标记的类中的【所有方法】
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void controllerPointcut() {}

    // @Around 表示“环绕通知”：在目标方法执行【前】、【后】、甚至【异常时】都能插手
    // "controllerPointcut()" 表示应用到上面定义的靶子上
    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // --- 【阶段一：请求开始前】(Before) ---

        // 获取 HTTP 请求上下文 (URL, Method等)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 如果不是HTTP请求（比如定时任务调用），直接放行，不记录日志
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod(); // 例如: POST
        String url = request.getRequestURI(); // 例如: /file/upload

        // 获取被拦截方法的签名 (类名.方法名)
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // 安全地获取参数
        // 这里的 joinPoint.getArgs() 拿到的是 Spring 已经反序列化好的 Java 对象数组
        String paramsLog = buildSafeParamsLog(joinPoint.getArgs());

        // 9. 打印“请求开始”日志
        if(!paramsLog.contains(show)){
            log.info(">>> [REQ] {} {}.{} \n| URL: {} \n| 参数: {}",
                    method, className, methodName, url, paramsLog);
        }

        long startTime = System.currentTimeMillis(); // 记录开始时间

        try {
            // --- 【阶段二：执行真正的业务】(Proceed) ---
            Object result = joinPoint.proceed();

            // --- 【阶段三：请求成功后】(After Returning) ---
            long endTime = System.currentTimeMillis();
            Long costTime=endTime - startTime;
            log.info("<<< [RES] {}.{} | 耗时: {}ms", className, methodName,costTime);
            if (result instanceof CommonResult) {
                ((CommonResult<?>) result).setCostTime(costTime.toString()+" ms");
            }
            return result; // 把业务方法的返回值原样返回给前端
        } catch (Exception e) {
            // --- 【阶段四：请求异常时】(After Throwing) ---
            long endTime = System.currentTimeMillis();
            log.error("!!! [ERR] {}.{} | 耗时: {}ms | 异常: {}",
                    className, methodName, (endTime - startTime), e.getMessage(), e);
            throw e;
        }
    }

    // --- 【辅助方法】：专门处理参数的安全打印 ---
    private String buildSafeParamsLog(Object[] args) {
        if (args == null || args.length == 0) return "无参数";

        return java.util.Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";

                    // 策略 A: 如果实现了 Loggable 接口，调用其定制的 toString()
                    if (arg instanceof Loggable) {
                        try {
                            return ((Loggable) arg).toString();
                        } catch (Exception ex) {
                            return "[Loggable Error]: " + ex.getMessage();
                        }
                    }

                    return show;
                })
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
