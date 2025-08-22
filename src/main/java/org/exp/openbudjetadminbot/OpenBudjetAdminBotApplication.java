package org.exp.openbudjetadminbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "org.exp.openbudjetadminbot.service.feign")

public class OpenBudjetAdminBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenBudjetAdminBotApplication.class, args);
    }

}
