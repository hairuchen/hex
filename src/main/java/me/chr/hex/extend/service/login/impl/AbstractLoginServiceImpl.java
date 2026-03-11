package me.chr.hex.extend.service.login.impl;

import lombok.Getter;
import me.chr.hex.core.R.Response.BizException;
import me.chr.hex.extend.DTO.LoginDTO;
import me.chr.hex.extend.VO.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: CHR
 * @Date: create in 2025/12/9
 */
@Getter
public abstract class AbstractLoginServiceImpl {

    @Value("${jwt.expiration:3600}")
    private long jwtExpirationSeconds;
    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    protected AuthenticationManager authenticationManager;

//    @Autowired
//    private RedisUtil redisUtil;

    protected LoginVO doLogin(LoginDTO loginRequestDTO) {
        // 1. 身份校验
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
        );
        User user = (User) authentication.getPrincipal();

        // 2. 生成JWT Token
        String token=this.generateToken(user);

        // 3. 存储Token到Redis（差异化：由子类实现）
//        storeTokenToRedis(details.getUsername(), token);

        // 4. 封装返回结果（共性）
        return new LoginVO(user,token);
    }

    private String generateToken(User user){
        try {
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("http://localhost:8080") // 签发者（和验签配置一致）
                    .subject(user.getUsername())     // 用户名
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(jwtExpirationSeconds)) // 1小时有效期
                    .claim("username", user.getUsername())   // 自定义字段
                    .claim("authorities", user.getAuthorities())
                    .build();
            return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            throw new BizException("生成 Token 失败：" + e.getMessage());
        }
    }

//
//    protected abstract void storeTokenToRedis(String username, String token);
//
//    protected abstract String getUsernamePrefix();
//    protected abstract String getTokenPrefix();
//
//    protected Boolean logout(){
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String token="";
//        if (auth instanceof HexUsernamePasswordAuthenticationToken tokenAuth) {
//            token = tokenAuth.getToken();
//        }
//        return redisUtil.delete(getTokenPrefix()+token);
//    }
//
//    protected Long countOnlineUsers(){
//        return redisUtil.countKeys(getUsernamePrefix() + "*");
//    }
//
//    protected Long countOnlineSessions(){
//        return redisUtil.countKeys(getTokenPrefix() + "*");
//    }
//
//    /**
//     * 刷新Token在Redis中的过期时间（与JWT过期时间保持一致）
//     * @param token 原始Token
//     */
//    public void refreshTokenExpiration(String token) {
//        String tokenKey = getTokenPrefix() + token;
//        // 先判断Token是否存在（避免操作已过期的键）
//        if (redisUtil.exists(tokenKey)) {
//            // 从反向映射中获取用户名键（用于同步刷新正向映射的过期时间）
//            String usernameKey = (String) redisUtil.get(tokenKey);
//            if (usernameKey != null && !usernameKey.isEmpty()) {
//                // 刷新正向映射（usernameKey -> tokenKey）的过期时间
//                redisUtil.set(usernameKey, tokenKey, jwtExpirationMs, TimeUnit.MILLISECONDS);
//                // 刷新反向映射（tokenKey -> usernameKey）的过期时间
//                redisUtil.set(tokenKey, usernameKey, jwtExpirationMs, TimeUnit.MILLISECONDS);
//            }
//        }
//    }

}
