package org.exp.openbudjetadminbot.service;

import com.pengrad.telegrambot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateDispatcher implements CommandLineRunner {

    private final TelegramBot telegramBot;

    @Override
    public void run(String... args) throws Exception {
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {

            });
            return 0;
        });
    }
}
