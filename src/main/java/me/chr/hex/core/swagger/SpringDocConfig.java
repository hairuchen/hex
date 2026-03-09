package me.chr.hex.core.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @Author: CHR
 * @Date: create in 2025/9/23
 */
@Configuration
public class SpringDocConfig {

    @Autowired
    private Environment environment;


    @Bean
    public OpenAPI customOpenAPI() {
        // 1. 创建Components对象并清空schemas
        Components components = new Components();
//        components.setSchemas(null); // 核心：移除所有模型定义

        // 2. 构建OpenAPI并设置components
        return new OpenAPI()
                .components(components)
                // 可选：添加文档信息（标题、描述等）
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title(environment.getProperty("spring.application.name")+"接口文档")
                        .description("API接口文档")
                        .version("1.0.0"));
    }

}
