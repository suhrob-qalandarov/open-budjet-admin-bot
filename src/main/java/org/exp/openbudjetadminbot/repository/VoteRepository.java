package org.exp.openbudjetadminbot.repository;

import org.exp.openbudjetadminbot.models.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT MAX(v.voteDate) FROM Vote v")
    Optional<LocalDateTime> findMaxVoteDate();

    Optional<Vote> findByVoterPhoneLast6DigitAndVoteDate(String voterPhoneLast6Digit, LocalDateTime voteDate);

}
