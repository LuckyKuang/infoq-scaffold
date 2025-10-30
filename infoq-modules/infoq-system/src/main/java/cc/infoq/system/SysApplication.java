package cc.infoq.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动程序
 *
 * @author Lion Li
 */
@SpringBootApplication(scanBasePackages = "cc.infoq.**")
public class SysApplication {

    public static void main(String[] args) {
        SpringApplication.run(SysApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  infoq-scaffold 启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
