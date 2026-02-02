package me.chr.hex.codeGenerator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2025/9/22
 */
@ComponentScan(basePackages = "me.chr.hex.codeGenerator")
public class CodeGenerator implements CommandLineRunner {

    @Autowired
    private Environment environment;

    @Autowired
    private GeneratorProperties generatorProperties;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CodeGenerator.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args); // 执行完run方法后会自动退出
    }

    @Override
    public void run(String... args) {
        String proj   = initProject();
        String prefix = proj + "_";
        String tPattern = prefix + "t_.*";   // 如 yili_t_order

        /* ========== 1. 实体：全部项目前缀表 ========== */
        FastAutoGenerator.create(generatorProperties.getDbUrl(),
                        generatorProperties.getDbUsername(),
                        generatorProperties.getDbPassword())
                .globalConfig(this::globalConf)
                .packageConfig(this::packageConf)
                .strategyConfig(builder -> builder
                        .addInclude(prefix + ".*")   // 全部项目前缀表
                        .addTablePrefix(prefix)      // 去掉 前缀hex_
                        .entityBuilder()
                        .logicDeleteColumnName("is_deleted")
                        .addIgnoreColumns("ykzd_rksj")
                        .enableLombok()
//                        .enableFileOverride()//覆盖原文件
                        // 其余三层关闭
                        .controllerBuilder().disable()
                        .serviceBuilder()   .disable()
                        .mapperBuilder()
                        .enableBaseResultMap()
                        .enableBaseColumnList())
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        /* ========== 2. 三层：仅 t_ 分支表 ========== */
        FastAutoGenerator.create(generatorProperties.getDbUrl(),
                        generatorProperties.getDbUsername(),
                        generatorProperties.getDbPassword())
                .globalConfig(this::globalConf)
                .packageConfig(this::packageConf)
                .strategyConfig(builder -> builder
                        .addInclude(tPattern)        // 只选 t_ 表
                        .addTablePrefix(prefix)      // 去掉 yili_
                        .entityBuilder().disable()   // 不再生成实体
                        .controllerBuilder().disable()
//                                .template("/templates/controller.java")
                        .serviceBuilder()
                        .mapperBuilder().disable()
                        )
                .injectionConfig(builder -> {
                    Map<String, Object> customMap = new HashMap<>();
                    customMap.put("importBase", generatorProperties.getImportCfg().getBase());
                    customMap.put("importResponse", generatorProperties.getImportCfg().getResponse());
                    customMap.put("importService", generatorProperties.getImportCfg().getService());
                    customMap.put("importEntity", generatorProperties.getImportCfg().getEntity());
                    customMap.put("projectName", generatorProperties.getProject().getName());
                    builder.customMap(customMap);
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        System.out.println("代码生成完成！");
    }

    private String initProject() {
        String projectName = environment.getProperty("project.name");
        System.out.println("当前项目为：" + projectName);
        return projectName;
    }

    private void globalConf(com.baomidou.mybatisplus.generator.config.GlobalConfig.Builder builder) {
        builder.author(generatorProperties.getAuthor())
                .enableSpringdoc()
                .disableOpenDir()
                .outputDir(generatorProperties.getOutputDir())
                .dateType(DateType.valueOf(generatorProperties.getDateType()))
                .commentDate(generatorProperties.getCommentDate());
    }

    private void packageConf(com.baomidou.mybatisplus.generator.config.PackageConfig.Builder builder) {
        String datadir = generatorProperties.getDatadir();
        builder.parent(generatorProperties.getParentPackage())
                .entity(datadir + ".entity")
                .service(datadir + ".service")
                .serviceImpl(datadir + ".service.impl")
                .mapper(datadir + ".mapper")
                .xml(datadir + ".mapper.xml")
                .controller(datadir + ".controller")
                .pathInfo(Collections.singletonMap(OutputFile.xml,
                        generatorProperties.getMapperXmlDir()));
    }

    private String toPureBizName(String name) {
        String pure = name.replaceFirst("^[A-Z][a-z0-9]*_?([TR])?_?", "");
        String camel = com.baomidou.mybatisplus.core.toolkit.StringUtils.underlineToCamel(pure);
        return camel.isEmpty() ? camel : camel.substring(0, 1).toUpperCase() + camel.substring(1);
    }

    private String toEntityFullName(String name) {
        return generatorProperties.getParentPackage() + "." +
                generatorProperties.getDatadir() + ".entity." +
                toPureBizName(name);
    }
}
