package org.exp.openbudjetadminbot.service.feign;

import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.FetchState;
import org.exp.openbudjetadminbot.models.Vote;
import org.exp.openbudjetadminbot.models.dto.response.VoteApiResponse;
import org.exp.openbudjetadminbot.repository.FetchStateRepository;
import org.exp.openbudjetadminbot.repository.VoteRepository;
import org.springframework.stereotype.Service;
import feign.FeignException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final ApiFeignClient apiFeignClient;
    private final VoteRepository voteRepository;
    private final FetchStateRepository fetchStateRepository;

    private String getNewUuid() {
        return "68a8bd4bec67bd58e289395a"; // Sizning UUID
    }

    public void fetchNewVotes() {
        FetchState state = fetchStateRepository.findById("default")
                .orElseGet(() -> {
                    FetchState newState = new FetchState();
                    newState.setId("default");
                    newState.setCurrentUuid(getNewUuid());
                    newState.setLastPage(0);
                    newState.setLastUpdate(LocalDateTime.now());
                    return newState;
                });

        if (state.getCurrentUuid() == null) {
            state.setCurrentUuid(getNewUuid());
            state.setLastPage(0);
            state.setLastUpdate(LocalDateTime.now());
        }

        fetchStateRepository.save(state);

        int page = state.getLastPage();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Vote> newVotes = new ArrayList<>();
        String uuid = state.getCurrentUuid();
        int totalPages = Integer.MAX_VALUE;

        while (page < totalPages) {
            try {
                System.out.println("Fetching page: " + page + " with UUID: " + uuid);
                VoteApiResponse response = apiFeignClient.getVotes(uuid, page);

                if (response.getContent() == null || response.getContent().isEmpty()) {
                    System.out.println("No more data");
                    break;
                }

                totalPages = response.getTotalPages();
                System.out.println("Total pages: " + totalPages);

                for (VoteApiResponse.VoteContent item : response.getContent()) {
                    LocalDateTime voteDate = LocalDateTime.parse(item.getVoteDate(), formatter);
                    String cleanedPhone = item.getPhoneNumber().replaceAll("[^0-9]", "");
                    String last6Digits = cleanedPhone.length() >= 6
                            ? cleanedPhone.substring(cleanedPhone.length() - 6)
                            : cleanedPhone;

                    // Duplikat tekshirish
                    Optional<Vote> existingVote = voteRepository.findByVoterPhoneLast6DigitAndVoteDate(last6Digits, voteDate);
                    if (existingVote.isEmpty()) {
                        Vote vote = Vote.builder()
                                .voterPhoneLast6Digit(last6Digits)
                                .voteDate(voteDate)
                                .votedUserPhoneNumbers(List.of(cleanedPhone))
                                .build();
                        newVotes.add(vote);
                    } else {
                        // Update qilish
                        Vote vote = existingVote.get();
                        vote.setVotedUserPhoneNumbers(List.of(cleanedPhone));
                        newVotes.add(vote);
                    }
                }

                state.setLastPage(page + 1);
                state.setLastUpdate(LocalDateTime.now());
                fetchStateRepository.save(state);

                page++;
                Thread.sleep(10000); // 10 sekund delay (API cheklovi)

            } catch (FeignException e) {
                if (e.status() == 411 || e.status() == 429 || e.status() == 404) {
                    System.out.println("Error: " + e.getMessage() + ". Trying new UUID.");
                    uuid = getNewUuid();
                    state.setCurrentUuid(uuid);
                    state.setLastPage(0);
                    fetchStateRepository.save(state);
                    continue;
                }
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (!newVotes.isEmpty()) {
            System.out.println("Saving " + newVotes.size() + " votes");
            voteRepository.saveAll(newVotes);
        } else {
            System.out.println("No new votes to save");
        }
    }
}