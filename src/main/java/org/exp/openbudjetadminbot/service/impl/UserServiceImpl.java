package org.exp.openbudjetadminbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.models.Vote;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.repository.VoteRepository;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

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
    public List<String> checkUserVoted(String phoneNumber) {
        String last6Digits = phoneNumber.length() >= 6 ? phoneNumber.substring(phoneNumber.length() - 6) : phoneNumber;
        return voteRepository.findAllByVoterPhoneLast6Digit(last6Digits).stream().map(Vote::getVoterPhoneLast6Digit).toList();
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
                .admin(user.id().equals(6513286717L))
                .fullName(
                        user.lastName() != null && !user.lastName().isBlank()
                                ? user.firstName() + " " + user.lastName()
                                : user.firstName()
                )
                .username(user.username())
                .build();
    }
}
