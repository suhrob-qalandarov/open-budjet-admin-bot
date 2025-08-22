package org.exp.openbudjetadminbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getOrSaveTgUser(com.pengrad.telegrambot.model.User tgUser) {
        return userRepository.findById(tgUser.id()).orElse(
                userRepository.save(mapToUser(tgUser))
        );
    }

    @Override
    public boolean checkUserIsVoted(User dbUser) {
        return false;
    }

    @Override
    public List<String> checkUserVoted(User dbUser) {

        return List.of();
    }

    public User updateDbUser(com.pengrad.telegrambot.model.User tgUser) {
        User newUser = mapToUser(tgUser);
        return userRepository.updateAndReturnUserInfo(
                newUser.getId(),
                newUser.getFullName(),
                newUser.getUsername(),
                newUser.getPhoneNumber()
        );
    }

    private User mapToUser(com.pengrad.telegrambot.model.User user) {
        return User.builder()
                .id(user.id())
                .fullName(
                        user.lastName() != null && !user.lastName().isBlank()
                                ? user.firstName() + " " + user.lastName()
                                : user.firstName()
                )
                .username(user.username())
                .build();
    }
}
