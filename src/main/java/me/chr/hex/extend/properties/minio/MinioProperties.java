package me.chr.hex.extend.properties.minio;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: CHR
 * @Date: create in 2026/2/3
 **/
/**
 * MinIO 配置属性类
 * 绑定 application.yml 中以 minio 为前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * MinIO 服务地址
     */
    private String endpoint;

    /**
     * 访问密钥 (用户名)
     */
    private String accessKey;

    /**
     * 秘密密钥 (密码)
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucketName;
}
