package org.exp.openbudjetadminbot.repository;

import jakarta.transaction.Transactional;
import org.exp.openbudjetadminbot.models.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    Optional<User> findById(Long id);

    User save(User user);

    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE users
            SET full_name   = :fullName,
                username    = :username,
                phone_number = :phoneNumber
            WHERE id = :id
            RETURNING *
        """,
            nativeQuery = true
    )
    User updateAndReturnUserInfo(
            @Param("id") Long id,
            @Param("fullName") String fullName,
            @Param("username") String username,
            @Param("phoneNumber") String phoneNumber
    );
}
