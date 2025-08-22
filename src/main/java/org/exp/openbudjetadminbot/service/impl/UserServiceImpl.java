package org.exp.openbudjetadminbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getOrSaveTelegramUser(com.pengrad.telegrambot.model.User user) {
        return null;
    }
}
