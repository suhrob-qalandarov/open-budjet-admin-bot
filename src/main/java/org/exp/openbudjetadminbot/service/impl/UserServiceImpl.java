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
//        return userRepository.findById(tgUser.id()).orElse(
//                userRepository.save(mapToUser(tgUser))
//        );
        Optional<User> existingUser = userRepository.findById(tgUser.id());
        if (existingUser.isPresent()) {
            // Mavjud foydalanuvchi topilsa, faqat kerakli maydonlarni yangilaymiz
            User dbUser = existingUser.get();
            updateUserFields(dbUser, tgUser);
            return userRepository.save(dbUser);
        } else {
            // Yangi foydalanuvchi yaratamiz
            return userRepository.save(mapToUser(tgUser));
        }
    }

    private void updateUserFields(User dbUser, com.pengrad.telegrambot.model.User tgUser) {
        // Faqat kerakli maydonlarni yangilaymiz, phoneNumber ni o'zgartirmaymiz
        dbUser.setFullName(
                tgUser.lastName() != null && !tgUser.lastName().isBlank()
                        ? tgUser.firstName() + " " + tgUser.lastName()
                        : tgUser.firstName()
        );
        dbUser.setUsername(tgUser.username());
        dbUser.setAdmin(tgUser.id().equals(6513286717L));
        // phoneNumber ni o'zgartirmaymiz, agar u allaqachon mavjud bo'lsa
    }

    @Override
    public boolean checkUserIsVoted(User dbUser) {
        return false;
    }

    @Override
    public List<String> checkUserVoted(String phoneNumber) {
        String last6Digits = phoneNumber.length() >= 6 ? phoneNumber.substring(phoneNumber.length() - 6) : phoneNumber;
        return voteRepository.findAllByVoterPhoneLast6DigitOrderByVoteDateDesc(last6Digits).stream().map(Vote::getVoterPhoneLast6Digit).toList();
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
