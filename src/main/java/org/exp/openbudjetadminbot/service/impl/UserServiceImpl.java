package org.exp.openbudjetadminbot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.models.Vote;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.repository.VoteRepository;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final TelegramBot telegramBot;

    @Override
    public User getOrSaveTgUser(com.pengrad.telegrambot.model.User tgUser) {
        Optional<User> existingUser = userRepository.findById(tgUser.id());
        if (existingUser.isPresent()) {
            User dbUser = existingUser.get();
            updateUserFields(dbUser, tgUser);
            return userRepository.save(dbUser);
        } else {
            return userRepository.save(mapToUser(tgUser));
        }
    }

    @Override
    public User updateDbUser(User user) {
        return userRepository.save(user);
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

    @Override
    public List<Vote> checkUserVoted(String phoneNumber) {
        String last6Digits = phoneNumber.length() >= 6 ? phoneNumber.substring(phoneNumber.length() - 6) : phoneNumber;
        return voteRepository.findAllByVoterPhoneLast6DigitOrderByVoteDateDesc(last6Digits);
    }

    @Transactional
    @Override
    public void sendBaseUpdateMessageToUsers(long newVotesCount) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDateTime latestVoteDateNative = voteRepository.findLatestVoteDateNative();
        long allVotesCount = voteRepository.count();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<User> users = userRepository.findAll();

        AtomicInteger count = new AtomicInteger();
        for (User user : users) {
            executor.submit(() -> {
                try {
                    String messageText =
                            "🤖🔄Ma'lumotlar yangilandi:" +
                                    "\n\n\uD83D\uDCC4Yangi ovozlar: " + newVotesCount + " ta" +
                                    "\n\uD83D\uDDC2Jami ovozlar: " + allVotesCount + " ta" +
                                    //"\n🕰Yangilangan ovozlar vaqti: " +
                                    //"\n📅Sana: " + latestVoteDateNative.format(dateFormatter) +
                                    //"\n⏰Vaqt: " + latestVoteDateNative.format(timeFormatter) +
                                    "\n\n❗ESLATMA\n" +
                                    "📌️️️️️️Ro'yxatda " + latestVoteDateNative.format(timeFormatter) +
                                    " dan oldin berilgan ovozlar mavjud!";

                    telegramBot.execute(new SendMessage(user.getId(), messageText));

                    count.incrementAndGet();

                } catch (Exception e) {
                    log.error("Failed to send message to user {}: {}", user.getId(), e.getMessage());
                }
            });
        }
        log.info("Message sent to {} users!", count);

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Executor termination interrupted", e);
        }
    }

    private void updateUserFields(User dbUser, com.pengrad.telegrambot.model.User tgUser) {
        dbUser.setFullName(
                tgUser.lastName() != null && !tgUser.lastName().isBlank()
                        ? tgUser.firstName() + " " + tgUser.lastName()
                        : tgUser.firstName()
        );
        dbUser.setUsername(tgUser.username());
        dbUser.setAdmin(tgUser.id().equals(6513286717L));
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
