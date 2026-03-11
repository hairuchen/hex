package me.chr.hex.core.log;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: CHR
 * @Date: create in 2026/2/28
 **/
@Slf4j
@Aspect
@Component
public class RequestLogAspect {

    private static final String show="[隐藏: 该类未实现 Loggable 接口,不打印请求日志！]";

    // 【配置项】集合/数组日志打印的最大条数
    private static final int MAX_LOG_SIZE = 20;

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
        log.info(">>> [REQ] {} {}.{} \n| URL: {} \n| 参数: {}",
                method, className, methodName, url, paramsLog);

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
                    className, methodName, (endTime - startTime), e.getMessage());
            throw e;
        }
    }

    // --- 【辅助方法】：专门处理参数的安全打印 ---
    private String buildSafeParamsLog(Object[] args) {
        if (args == null || args.length == 0) return "无参数";

        return java.util.Arrays.stream(args)
                .map(this::safeToString) // 提取为独立方法处理递归逻辑
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String safeToString(Object arg) {
        // 策略 A: 如果实现了 Loggable 接口，调用其定制的 toString()
        if (arg instanceof Loggable) {
            try {
                return ((Loggable) arg).toString();
            } catch (Exception ex) {
                return "[Loggable Error]: " + ex.getMessage();
            }
        }

        // 策略 B: 集合 (List, Set 等)
        if (arg instanceof Collection<?> collection) {
            if (collection.isEmpty()) return "[]";

            int size = collection.size();
            // 转换为 Stream 以便截取
            Stream<?> stream = collection.stream();

            // 如果超过最大限制，只取前 MAX_LOG_SIZE 个
            if (size > MAX_LOG_SIZE) {
                stream = stream.limit(MAX_LOG_SIZE);
            }

            String innerLog = stream
                    .map(this::safeToString) // 递归调用
                    .collect(Collectors.joining(", "));

            // 如果截断了，追加提示信息
            if (size > MAX_LOG_SIZE) {
                return "[" + innerLog + ", ... (共 " + size + " 条，仅显示前 " + MAX_LOG_SIZE + " 条)]";
            } else {
                return "[" + innerLog + "]";
            }
        }

        // 策略 C: 如果是数组，递归处理内部元素
        if (arg.getClass().isArray()) {
            // 基本类型数组处理
            if (arg.getClass().getComponentType().isPrimitive()) {
                // 基本类型数组通常数据量大时也建议截断，但 Arrays.toString 不支持 limit
                // 这里简单处理：如果长度过大，只打印前 20 个手动拼接，或者为了性能直接提示
                int len = java.lang.reflect.Array.getLength(arg);
                if (len > MAX_LOG_SIZE) {
                    // 简单处理：只转前 20 个
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < MAX_LOG_SIZE; i++) {
                        sb.append(java.lang.reflect.Array.get(arg, i));
                        if (i < MAX_LOG_SIZE - 1) sb.append(", ");
                    }
                    sb.append(", ... (共 ").append(len).append(" 条，仅显示前 ").append(MAX_LOG_SIZE).append(" 条)]");
                    return sb.toString();
                } else {
                    return handlePrimitiveArray(arg, len);
                }
            }

            // 对象数组处理
            Object[] array = (Object[]) arg;
            int len = array.length;
            if (len == 0) return "[]";

            Stream<Object> stream = java.util.Arrays.stream(array);
            if (len > MAX_LOG_SIZE) {
                stream = stream.limit(MAX_LOG_SIZE);
            }

            String innerLog = stream
                    .map(this::safeToString)
                    .collect(Collectors.joining(", "));

            if (len > MAX_LOG_SIZE) {
                return "[" + innerLog + ", ... (共 " + len + " 条，仅显示前 " + MAX_LOG_SIZE + " 条)]";
            } else {
                return "[" + innerLog + "]";
            }
        }

        // 策略 D: 如果是 Map，简单处理 Key-Value
        if (arg instanceof Map<?, ?> map) {
            if (map.isEmpty()) return "{}";

            // 如果 Map 也很大，可以类似 Collection 处理，这里暂时全量打印 Key=Value
            // 为了安全，Value 依然走递归检查
            String innerLog = map.entrySet().stream()
                    .limit(MAX_LOG_SIZE) // 防止 Map 过大
                    .map(entry -> {
                        String k = safeToString(entry.getKey());
                        String v = safeToString(entry.getValue());
                        // 如果 Key 或 Value 被隐藏，整个 Entry 可能也没意义，但这里保留原样
                        return k + "=" + v;
                    })
                    .collect(Collectors.joining(", "));

            if (map.size() > MAX_LOG_SIZE) {
                return "{" + innerLog + ", ... (共 " + map.size() + " 项，仅显示前 " + MAX_LOG_SIZE + " 项)}";
            }
            return "{" + innerLog + "}";
        }

        // 策略 E: 普通对象，且未实现 Loggable
        return show;
    }

    // 辅助方法：处理基本类型数组的截断打印
    private String handlePrimitiveArray(Object array, int len) {
        if (len == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(len, MAX_LOG_SIZE);
        for (int i = 0; i < limit; i++) {
            sb.append(java.lang.reflect.Array.get(array, i));
            if (i < limit - 1) sb.append(", ");
        }
        if (len > MAX_LOG_SIZE) {
            sb.append(", ... (共 ").append(len).append(" 条，仅显示前 ").append(MAX_LOG_SIZE+").条)]");
        } else {
            sb.append("]");
        }
        return sb.toString();
    }
}
