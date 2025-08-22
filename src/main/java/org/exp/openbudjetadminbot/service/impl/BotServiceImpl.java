package org.exp.openbudjetadminbot.service.impl;

import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.service.face.BotService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    @Override
    public SendResponse sendMessage(SendMessage message) {
        return null;
    }

    @Override
    public SendResponse editMessage(EditMessageText editMessageText) {
        return null;
    }
}
