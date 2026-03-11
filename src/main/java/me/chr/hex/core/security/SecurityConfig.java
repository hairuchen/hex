package me.chr.hex.core.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chr.hex.core.R.Response.CommonResult;
import me.chr.hex.core.R.Response.ResultCode;
import me.chr.hex.extend.BO.Permission;
import me.chr.hex.extend.service.PermissionService;
import me.chr.hex.general.entity.User;
import me.chr.hex.general.mapper.UserMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: CHR
 * @Date: create in 2025/9/23
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig implements AuthenticationEntryPoint, AccessDeniedHandler,
                                        UserDetailsService{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 系统默认需要放行的路径（静态变量，框架级路径）
    private static final List<String> DEFAULT_PERMIT_PATHS = List.of(
            "/swagger-ui/**",               // Swagger 前端 UI
            "/swagger-ui.html",             // 旧版 UI 入口
            "/api-docs/**",                 // OpenAPI 描述文件
            "/swagger-resources/**",        // 旧版资源
            "/webjars/**"                  // Swagger 依赖的 webjar（js/css）
    );

    @Value("${spring.security.permit:}")  // 冒号后为空表示默认空列表
    private List<String> customPermitPaths;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PermissionService permissionService;


    /*
     *   保安
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        // 从 Spring Security 的配置中获取默认的 AuthenticationManager
        return authConfig.getAuthenticationManager();
    }

    /*
     *   加密员
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     *   门杆
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 获取合并后的所有放行路径
        List<String> permitPaths = this.getAllPermitPaths();
        return http
                // 1. 关闭CSRF（非浏览器客户端调用API时建议关闭）
                .csrf(AbstractHttpConfigurer::disable)
                // 2. 配置请求授权规则（白名单核心）
                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll() // 临时放行所有
                        .requestMatchers(permitPaths.toArray(new String[0])).permitAll() // 登录接口放行
                        .anyRequest().authenticated() // 其他接口必须认证
                )
                // 3. 关闭默认的表单登录（避免生成/login默认接口）
                .formLogin(AbstractHttpConfigurer::disable)
                // 4. 关闭默认的HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 5. 禁用默认的登出接口
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this) // 绑定当前类的 AuthenticationEntryPoint实现
                        .accessDeniedHandler(this)       // 绑定当前类的 AccessDeniedHandler实现
                )
                // 6. 设置Session为无状态（Token认证）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置OAuth2资源服务器
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter( jwtAuthenticationConverter()))
                        //关闭OAuth2的异常处理 使用自己定义的
                        .authenticationEntryPoint(this)
                        .accessDeniedHandler(this)
                )
                .build();
    }

    // 合并系统默认路径和用户自定义路径
    private List<String> getAllPermitPaths() {
        List<String> allPaths = new ArrayList<>(DEFAULT_PERMIT_PATHS);
        if (customPermitPaths != null && !customPermitPaths.isEmpty()) {
            allPaths.addAll(customPermitPaths);
        }
        return allPaths;
    }

    /**
     * AuthenticationEntryPoint 接口 Http header 401 响应 封装为 Http body统一响应
     */
    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull AuthenticationException authException) throws IOException, ServletException {
        // 设置响应头
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK); // 统一返回200，用code字段标识实际状态
        // 写入响应体
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(CommonResult.failure(ResultCode.UNAUTHORIZED)));
    }

    /**
     * AccessDeniedHandler 接口 Http header 403 响应 封装为 Http body统一响应
     */
    @Override
    public void handle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 设置响应头
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK); // 统一返回200，用code字段标识实际状态
        // 写入响应体
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(CommonResult.failure(ResultCode.FORBIDDEN)));
    }

    /**
     * UserDetailsService 接口
     * 实现用户校验
     */
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", username));
        if (user==null){
            throw new UsernameNotFoundException("用户不存在");
        }
        //账号是否可用
        boolean enabled = true;
        //账号是否没有锁定
        boolean accountNonLocked = true;
        if (user.getStatus() == 0) {
            enabled=false;
        }
        if (user.getStatus() == 2) {
            accountNonLocked=false;
        }

        List<Permission> permissionList=permissionService.getUserPermission(user.getId());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled,
                true,
                true,
                accountNonLocked,
                getAuthorities(permissionList));
    }

    /**
     * 从UserPermission中提取所有权限标识（controller_name:function_name）
     */
    private static Collection<? extends GrantedAuthority> getAuthorities(List<Permission> permissionList) {
        // 过滤有效权限（状态正常+控制器和方法名不为空），转换为"控制器:方法名"格式
        return permissionList.stream()
                .filter(perm -> perm.getStatus() == 1) // 仅生效的权限（status=1）
                .filter(perm -> perm.getIsDeleted() == 0) // 仅没删除的权限（is_deleted=0）
                .filter(perm -> StringUtils.hasText(perm.getControllerName())
                        && StringUtils.hasText(perm.getFunctionName())) // 控制器和方法名不为空
                .map(perm -> new SimpleGrantedAuthority(
                        perm.getControllerName() + ":" + perm.getFunctionName() // 拼接权限标识
                ))
                .collect(Collectors.toList());
    }


    //============================== Token 认证 ==============================
    @Value("${jwt.public-key}")
    private String publicKey;
    @Value("${jwt.private-key}")
    private String privateKey;

    // 用 RSA 公钥创建 JwtDecoder（自动验签）
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    // 配置 JwtEncoder
    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey rsaPublicKey,RSAPrivateKey rsaPrivateKey) {
        if (!rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus())) {
            // 抛出明确的异常，提示公私钥不匹配
            throw new IllegalArgumentException("有内鬼 终止交易!");
        }
        log.warn("✅ RSA 公私钥配对验证通过，模数长度：{} bit", rsaPublicKey.getModulus().bitLength());
        return NimbusJwtEncoder.withKeyPair(rsaPublicKey,rsaPrivateKey).build();
    }

    // 加密使用 RSA 私钥
    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        String cleanKey = privateKey
                .replaceAll("\\s", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        if (privateKey instanceof RSAPrivateKey) {
            return (RSAPrivateKey) privateKey;
        } else {
            throw new IllegalArgumentException("非法私钥!");
        }
    }

    // 解密使用 RSA 公钥（用于验签）
    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        // 去掉 Base64 字符串中的换行和头尾（如果有的话）
        String cleanKey = publicKey
                .replaceAll("\\s", "") // 去掉所有空白字符
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        if (publicKey instanceof RSAPublicKey) {
            return (RSAPublicKey) publicKey;
        } else {
            throw new IllegalArgumentException("非法公钥!");
        }
    }

    /**
     * 自定义JWT权限解析器：解析Token中自定义的authorities字段（字符串数组）
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. 读取JWT中的authorities数组（每个元素是{"role":"xxx"}）
            List<Map<String, String>> authObjects = jwt.getClaim("authorities");
            // 2. 空值处理
            if (authObjects == null || authObjects.isEmpty()) {
                return Collections.emptyList();
            }
            // 3. 提取每个对象中的role字段值
            List<String> authorityList = authObjects.stream()
                    .map(authObj -> authObj.get("role")) // 关键：从role字段取值
                    .filter(StringUtils::hasText) // 过滤空值
                    .toList();
            // 4. 转为Spring Security的权限对象
            return authorityList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return converter;
    }
}