package org.exp.openbudjetadminbot.service.face;

import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;

@Service
public interface BotResponseService {

    SendResponse sendMessage(SendMessage message);

    SendResponse editMessage(EditMessageText editMessageText);

}
