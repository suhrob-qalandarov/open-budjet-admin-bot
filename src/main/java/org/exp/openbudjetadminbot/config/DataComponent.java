package org.exp.openbudjetadminbot.config;

import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.Content;
import org.exp.openbudjetadminbot.models.FetchState;
import org.exp.openbudjetadminbot.repository.ContentRepository;
import org.exp.openbudjetadminbot.repository.FetchStateRepository;
import org.exp.openbudjetadminbot.service.feign.VoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataComponent implements CommandLineRunner {

    private final ContentRepository contentRepository;
    private final VoteService voteService;
    private final FetchStateRepository fetchStateRepository;

    @Override
    public void run(String... args) throws Exception {

        fetchVotesJob();

        if (fetchStateRepository.count() == 0) {
            FetchState state = new FetchState();
            state.setId("default");
            state.setCurrentUuid("68a8b348ec67bd58e2890afd");
            state.setLastPage(0);
            state.setLastUpdate(LocalDateTime.now());
            fetchStateRepository.save(state);
        }

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

    @Scheduled(fixedRate = 1000)
    public void fetchVotesJob() {
        String uuid = "68a8bd4bec67bd58e289395a";
        System.out.println("Fetching votes for UUID: " + uuid);
        voteService.fetchNewVotes();
    }
}
