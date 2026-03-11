package me.chr.hex;

import lombok.extern.slf4j.Slf4j;
import me.chr.hex.extend.service.FileParse;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.PrintStream;

@SpringBootApplication
@EnableAsync
@Slf4j
public class HexApplication {

    @Autowired
    private static FileParse fileParse;

    public static void main(String[] args) {
        System.setOut(createMyPrintStream(System.out));
        System.setErr(createMyPrintStream(System.err));

        ConfigurableApplicationContext context =SpringApplication.run(HexApplication.class, args);

        FileParse fileParse = context.getBean(FileParse.class);

        // 3. 打印解析器信息
        log.info("========================================");
        log.info("✅ 当前启用的文件解析器类型：{}", fileParse.getType());
        log.info("✅ 解析器具体实现类：{}", fileParse.getClass().getCanonicalName());
        log.info("========================================");
    }

    /**
     * 把System.out和System.err替换为新的PrintStream,用logger.info代替System.out.print和System.err.print
     * @param SysPrintStream  可以是System.out和System.err
     * @return  preprintStream
     */
    public static PrintStream createMyPrintStream(final PrintStream SysPrintStream) {
        return new PrintStream(SysPrintStream) {
            public void print(final String string) {
                log.info(string);
            }
            public void println(final String string) {
                log.info(string);
            }
        };
    }

}
