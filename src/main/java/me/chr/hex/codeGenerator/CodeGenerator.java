package me.chr.hex.codeGenerator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: CHR
 * @Date: create in 2025/9/22
 */
@Slf4j
//@Component // 开启/关闭 代码生成器
public class CodeGenerator implements CommandLineRunner {

    @Autowired
    private GeneratorProperties generatorProperties;

    @Override
    public void run(String @NonNull ... args) {
        log.info("========== 开始代码生成 ==========");
        log.info("项目前缀: {}", generatorProperties.getName());
        log.info("所有配置项: {}", generatorProperties.toString());

        /*
        ========== 阶段 1: 生成 Entity 和 Mapper/XML (允许覆盖) ==========
            注意:entity实体层于数据库强关联 可能伴随高频修改 因此需要覆盖操作
                general包下的实体对象不应该进行编码级编译 正确的使用方式为extend包下使用子对象继承
                因此general包下的xml文件也不应该修改 而是使用继承子对象创建extend目录下的xml文件进行拓展
         */

//        String prefix = generatorProperties.getName() + "_";
//        String tPattern = prefix + "t_.*";   // 如 yili_t_order
        String prefix = "";
        FastAutoGenerator
                // 1. 配置数据源
                .create(this.dataSourceConfig())
                // 2. 全局配置
                .globalConfig(this::globalConf)
                // 3. 包配置
                .packageConfig(this::packageConf)
                // 4. 策略配置
                .strategyConfig(builder -> builder
                        .addInclude(prefix + ".*_default$")
                        .addTablePrefix(prefix)
                        .addTableSuffix("_default")
                        .entityBuilder()
                        .enableLombok()
                        .logicDeleteColumnName("is_deleted")
                        .addIgnoreColumns("ykzd_rksj")
                        .javaTemplate("/templates/entity")
                        .enableFileOverride()//覆盖原文件
                        .mapperBuilder()
                        .enableFileOverride()
                        .enableBaseResultMap()
                        .enableBaseColumnList()
                        .mapperAnnotation(org.apache.ibatis.annotations.Mapper.class)
                        // 其余三层关闭
                        .controllerBuilder().disable()
                        .serviceBuilder().disable()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        /* ========== 阶段 2: 生成 Service 和 Controller (禁止覆盖) ========== */
        FastAutoGenerator
                // 1. 配置数据源
                .create(this.dataSourceConfig())
                // 2. 全局配置
                .globalConfig(this::globalConf)
                // 3. 包配置
                .packageConfig(this::packageConf)
                // 4. 策略配置
                .strategyConfig(builder -> builder
                                .addInclude(prefix + ".*_default$")
                                .addTableSuffix("_default")
                                .entityBuilder().disable()
                                .mapperBuilder().disable()
                                .controllerBuilder().disable()
//                                .template("/templates/controller.java")
                                .serviceBuilder()
                )
                .injectionConfig(builder -> {
                    Map<String, Object> customMap = new HashMap<>();
                    customMap.put("importResponse", generatorProperties.getResponse());
                    customMap.put("importService", generatorProperties.getService());
                    customMap.put("importEntity", generatorProperties.getEntity());
                    customMap.put("projectName", generatorProperties.getName());
                    builder.customMap(customMap);
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        log.info("========== 代码生成完成 ==========");
    }

    private DataSourceConfig.Builder dataSourceConfig() {
        return new DataSourceConfig.Builder(
                generatorProperties.getDbUrl(),
                generatorProperties.getDbUsername(),
                generatorProperties.getDbPassword()
        );
    }

    private void globalConf(GlobalConfig.Builder builder) {
        builder
//                .author(generatorProperties.getAuthor())
                .enableSpringdoc()
                .disableOpenDir()
                .outputDir(generatorProperties.getOutputDir())
//                .dateType(DateType.valueOf(generatorProperties.getDateType()))
//                .commentDate(generatorProperties.getCommentDate())
        ;
    }

    private void packageConf(PackageConfig.Builder builder) {
        String datadir = generatorProperties.getDataDir();
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

}
