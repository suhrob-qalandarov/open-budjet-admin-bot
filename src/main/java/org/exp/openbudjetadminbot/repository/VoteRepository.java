package org.exp.openbudjetadminbot.repository;

import org.exp.openbudjetadminbot.models.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT MAX(v.voteDate) FROM Vote v")
    Optional<LocalDateTime> findMaxVoteDate();

    Optional<Vote> findByVoterPhoneLast6DigitAndVoteDate(String voterPhoneLast6Digit, LocalDateTime voteDate);

    boolean existsByVoterPhoneLast6Digit(String voterPhoneLast6Digit);

    List<Vote> findAllByVoterPhoneLast6DigitOrderByVoteDateDesc(String voterPhoneLast6Digit);

    @Query("SELECT v FROM Vote v WHERE v.voterPhoneLast6Digit LIKE %:text% ORDER BY v.voteDate DESC")
    List<Vote> findAllByVoterPhoneLast6DigitContaining(@Param("text") String text);

    @Modifying
    @Query(value = """
        DELETE FROM votes
        WHERE id IN (
            SELECT id
            FROM (
                SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY voter_phone_last6digit, vote_date
                           ORDER BY id
                       ) AS row_num
                FROM votes
            ) t
            WHERE t.row_num > 1
        )
        """, nativeQuery = true)
    void deleteDuplicatesByVoterPhoneLast6DigitAndVoteDate();

    @Query(value = "SELECT MAX(vote_date) FROM votes", nativeQuery = true)
    LocalDateTime findLatestVoteDateNative();
}
