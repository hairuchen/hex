package me.chr.hex.codeGenerator;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: CHR
 * @Date: create in 2025/9/22
 */
@Component
@Data
public class GeneratorProperties {
    /* ---------- generator 前缀 ---------- */
    @Value("${generator.author}")
    private String author;

    @Value("${generator.outputDir}")
    private String outputDir;

    @Value("${generator.dateType}")
    private String dateType;

    @Value("${generator.commentDate}")
    private String commentDate;

    @Value("${generator.parentPackage}")
    private String parentPackage;

    @Value("${generator.datadir}")
    private String datadir;

    @Value("${generator.mapperXmlDir}")
    private String mapperXmlDir;

    /* ---------- spring.datasource 前缀 ---------- */
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /* ---------- 内部类 ---------- */
    @Autowired
    private Project project;
    @Autowired
    private Package pkg;
    @Autowired
    private Import importCfg;

    @Data
    @Component
    public static class Project {
        @Value("${project.name}")
        private String name;
    }


    @Data
    @Component
    public static class Package {
        @Value("${generator.package.parent}")
        private String parent;
        @Value("${generator.package.parent}")
        private String module;
    }

    @Data
    @Component
    public static class Import {
        @Value("${generator.import.base}")
        private String base;
        @Value("${generator.import.response}")
        private String response;
        @Value("${generator.import.service}")
        private String service;
        @Value("${generator.import.entity}")
        private String entity;
    }
}
