package me.chr.hex.codeGenerator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: CHR
 * @Date: create in 2025/9/22
 */
@Data
@Slf4j
@Component
public class GeneratorProperties {

    @Value("${spring.application.name}")
    private String name;
    /* ---------- DataSourceConfig:spring.datasource 前缀 ---------- */
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /* ---------- GlobalConfig:硬编码 ---------- */
//    private String author;

    private String outputDir = "src/main/java";

//    private String dateType;

//    private String commentDate;

    /* ---------- PackageConfig:构造&硬编码 ---------- */

    private String parentPackage;

    private String dataDir = "general";

    private String mapperXmlDir = "src/main/resources/mapper/general";

    /* ---------- 生成文件引用:硬编码 ---------- */
    private String response;
    private String service;
    private String entity;


    public GeneratorProperties() {
        String currentPackage = this.getClass().getPackage().getName();
        this.parentPackage = currentPackage.substring(0, currentPackage.lastIndexOf("."));

        this.response = parentPackage + ".core.R.Response";
        this.service = parentPackage + ".general.service";
        this.entity = parentPackage + ".general.entity";
    }

}

