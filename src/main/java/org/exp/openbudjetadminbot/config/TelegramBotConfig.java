package org.exp.openbudjetadminbot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    public String botUsername;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botToken);
    }
}
