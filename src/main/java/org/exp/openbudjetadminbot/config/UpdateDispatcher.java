package org.exp.openbudjetadminbot.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.exp.openbudjetadminbot.handlers.CallbackQueryHandler;
import org.exp.openbudjetadminbot.handlers.MessageHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateDispatcher implements CommandLineRunner {

    private final TelegramBot telegramBot;
    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;

    @Override
    public void run(String... args) throws Exception {
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (update.message() != null) messageHandler.accept(update.message());
                else if (update.callbackQuery() != null) callbackQueryHandler.accept(update.callbackQuery());
                else log.info("Unknown update {}", update);
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
