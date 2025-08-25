package org.exp.openbudjetadminbot.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.exp.openbudjetadminbot.service.feign.VoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataComponent implements CommandLineRunner {

    private final VoteService voteService;

    private static final String uuid = "68abda77ec67bd58e294251a";
    private final UserService userService;
    private final TelegramBot telegramBot;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        long newVotesCount = voteService.fetchNewVotes(uuid, 0, 10);
        if (newVotesCount == -1) {
            telegramBot.execute(new SendMessage(
                    3L,
                    "💢Ma'lumotlarni o'qishda xatolik!"
            ));
            return;
        }
        userService.sendBaseUpdateMessageToUsers(newVotesCount);
    }
}

/*
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
*/

/*
@Transactional
    public void sendStarterMessageToUsers() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime latestVoteDateNative = voteRepository.findLatestVoteDateNative();
        ExecutorService executor = Executors.newFixedThreadPool(5); // 5 ta thread
        List<User> users = userRepository.findAll();
        for (User user : users) {
            executor.submit(() -> {
                try {
                    telegramBot.execute(new SendMessage(
                            user.getId(),
                            //"🤖🔄Bot yangilandi qayta /start bosing!" +
                                    "\n\uD83D\uDDDEYangi ovozlar ro'yhati olindi: " + count + " ta" +
                                    "\n🕰Yangilangan ovozlar vaqti: " +
                                    "\n📅Sana: " + latestVoteDateNative.format(dateFormatter) +
                                    "\n⏰Vaqt: " + latestVoteDateNative.format(timeFormatter) +
                                    "\n\nESLATMA!\n" + latestVoteDateNative.format(timeFormatter) +
                                            " dan oldingi ovozlar ro'yhati mavjud!"
                    )
                                    //.replyMarkup(new ReplyKeyboardRemove())
                    );
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
*/