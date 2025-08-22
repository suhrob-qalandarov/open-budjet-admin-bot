package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MessageHandler implements Consumer<Message> {

    private final UserService userService;
    private final TelegramBot telegramBot;

    @Override
    public void accept(Message message) {
        String text = message.text();
        User dbUser = userService.getOrSaveTelegramUser(message.from());

        if (text.equals("/start")) {
            SendResponse response = telegramBot.execute(new SendMessage("", ""));
        }

    }
}
