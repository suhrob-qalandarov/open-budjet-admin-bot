package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class CallbackQueryHandler implements Consumer<CallbackQuery> {

    private final TelegramBot telegramBot;

    @Override
    public void accept(CallbackQuery callbackQuery) {

        SendResponse execute = (SendResponse) telegramBot.execute(new EditMessageText("", 123, ""));
    }
}
