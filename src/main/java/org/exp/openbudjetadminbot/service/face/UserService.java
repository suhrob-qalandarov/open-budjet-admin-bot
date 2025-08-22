package org.exp.openbudjetadminbot.service.face;

import org.exp.openbudjetadminbot.models.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    User getOrSaveTelegramUser(com.pengrad.telegrambot.model.User user);
}
