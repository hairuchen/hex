package me.chr.hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.PrintStream;

@SpringBootApplication
public class HexApplication {

    private static final Logger logger = LoggerFactory.getLogger(HexApplication.class);

    public static void main(String[] args) {
        System.setOut(createMyPrintStream(System.out));
        System.setErr(createMyPrintStream(System.err));

        SpringApplication.run(HexApplication.class, args);
    }

    /**
     * 把System.out和System.err替换为新的PrintStream,用logger.info代替System.out.print和System.err.print
     * @param SysPrintStream  可以是System.out和System.err
     * @return  preprintStream
     */
    public static PrintStream createMyPrintStream(final PrintStream SysPrintStream) {
        return new PrintStream(SysPrintStream) {
            public void print(final String string) {
                logger.info(string);
            }
            public void println(final String string) {
                logger.info(string);
            }
        };
    }

}
