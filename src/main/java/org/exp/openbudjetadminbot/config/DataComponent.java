package org.exp.openbudjetadminbot.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.exp.openbudjetadminbot.models.Content;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.repository.ContentRepository;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.repository.VoteRepository;
import org.exp.openbudjetadminbot.service.feign.VoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataComponent implements CommandLineRunner {

    private final ContentRepository contentRepository;
    private final VoteService voteService;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;

    private static final String uuid = "368a9dce3ca336c5f73f408d8";

    @Transactional
    @Override
    public void run(String... args) throws Exception {

        fetchVotesJob(uuid);
        removeDuplicate();
        //sendStarterMessageToUsers();

        if (contentRepository.count() == 0){
            Content content = Content.builder()
                    .id(UUID.fromString("98844e33-4586-4474-bec3-5838c3ebaab3"))
                    .boardId(52L)
                    .quarterName("Хосиён")
                    .publicId("052408466012")
                    .title("")
                    .stage("PASSED")
                    .createdDate(LocalDate.parse("2025-07-28"))
                    .districtName("Қўшкўпир тумани")
                    .categoryName("Ички йўлларни (пиёдалар йўлакчаси, йўл ўтказгичлар) таъмирлаш билан боғлиқ тадбирлар")
                    .regionName("Хоразм вилояти")
                    .description("Xosiyon mahallasi Afrosiyob ko'chasini asfalt qilish")
                    .voteCount(1079)
                    .coefficient(null)
                    .grantedAmount(1499550000L)
                    .requestedAmount(1499550000L)
                    .publicControlQuality(null)
                    .images(List.of(
                            "f87fdfbab84a927ef81746a3e27ae3e0",
                            "fb0ba3501dacc296fcd8ee39560f266c",
                            "ffebe22ee8104bc9ff5c83c1d1218900",
                            "fac9d150f70727f4f896358be21ce760"
                    ))
                    .build();
            contentRepository.save(content);
        }

    }

    @Transactional
    public void sendStarterMessageToUsers() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime latestVoteDateNative = voteRepository.findLatestVoteDateNative();
        ExecutorService executor = Executors.newFixedThreadPool(5); // 5 ta thread
        List<User> users = userRepository.findAll();
        for (User user : users) {
            executor.submit(() -> {
                try {
                    telegramBot.execute(new SendMessage(
                            user.getId(),
                            "🤖🔄Bot yangilandi qayta /start bosing!" +
                                    "\n\n🕰Oxirgi yangilangan ma'lumotlar vaqti: " + latestVoteDateNative.format(formatter)
                    ));
                    log.info("Message sent to user: {}", user.getId());
                } catch (Exception e) {
                    log.error("Failed to send message to user {}: {}", user.getId(), e.getMessage());
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Executor termination interrupted", e);
        }
    }

    //@Scheduled(fixedRate = 3000)
    public void fetchVotesJob(String id) {
        voteService.fetchNewVotes(
                id,
                0,
                10
        );
    }

    @Transactional
    public void removeDuplicate() {
        voteRepository.deleteDuplicatesByVoterPhoneLast6DigitAndVoteDate();
    }
}
