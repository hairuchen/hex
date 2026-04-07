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
import me.chr.hex.general.entity.SysUser;
import me.chr.hex.general.mapper.SysUserMapper;
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
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
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
    private SysUserMapper userMapper;
    @Autowired
    private me.chr.hex.general.mapper.TenantMapper tenantMapper;
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
     * 实现用户校验（支持普通用户和租户登录）
     */
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        // 1. 先尝试从普通用户表查找
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("username", username));

        if (user != null) {
            // 普通用户登录
            return loadSysUserDetails(user);
        }

        // 2. 普通用户不存在，尝试从租户表查找
        me.chr.hex.general.entity.Tenant tenant = tenantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<me.chr.hex.general.entity.Tenant>()
                        .eq("username", username));

        if (tenant != null) {
            // 租户登录
            return loadTenantDetails(tenant);
        }

        // 3. 都找不到，抛出异常
        throw new UsernameNotFoundException("用户不存在");
    }

    /**
     * 加载普通用户详情
     */
    private UserDetails loadSysUserDetails(SysUser user) {
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
     * 加载租户详情（租户拥有所有权限）
     */
    private UserDetails loadTenantDetails(me.chr.hex.general.entity.Tenant tenant) {
        // 账号是否可用
        boolean enabled = tenant.getStatus() != null && tenant.getStatus() == 1;

        // 获取租户下的所有权限
        List<Permission> allPermissions = permissionService.getTenantAllPermissions(tenant.getId());

        return new org.springframework.security.core.userdetails.User(
                tenant.getUsername(),
                tenant.getPassword(),
                enabled,
                true,
                true,
                true,
                getAuthorities(allPermissions));
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
    @Value("${jwt.issuer:http://localhost:8080}")
    private String issuer;

    // 用 RSA 公钥创建 JwtDecoder（自动验签）
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(rsaPublicKey).build();

        // 只验签名，不验时间戳（过期由 Redis 控制）
        // 但仍可保留 issuer 校验
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(issuer)  // 只验签发者
                // 故意不加 JwtTimestampValidator
        );
        decoder.setJwtValidator(validator);
        return decoder;
    }

    // 配置 JwtEncoder
    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey rsaPublicKey,RSAPrivateKey rsaPrivateKey) {
        if (!rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus())) {
            // 抛出明确的异常，提示公私钥不匹配
            throw new IllegalArgumentException("公私钥长度不匹配!");
        }
        log.info("✅ RSA 公私钥配对验证通过，模数长度：{} bit", rsaPublicKey.getModulus().bitLength());
        return NimbusJwtEncoder.withKeyPair(rsaPublicKey,rsaPrivateKey).build();
    }

    // 加密使用 RSA 私钥
    @Bean
    public RSAPrivateKey rsaPrivateKey()  {
        try {
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
        }catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new IllegalArgumentException("非法私钥!",e);
        }
    }

    // 解密使用 RSA 公钥（用于验签）
    @Bean
    public RSAPublicKey rsaPublicKey() {
        try {
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
        }catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new IllegalArgumentException("非法公钥!",e);
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
            List<String> authorities = jwt.getClaimAsStringList("authorities");
            // 2. 空值处理
            if (authorities == null) return Collections.emptyList();
            // 3. 转为Spring Security的权限对象
            return authorities.stream()
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return converter;
    }
}