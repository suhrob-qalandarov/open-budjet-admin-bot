package org.exp.openbudjetadminbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(unique = true)
    private Long id;

    @Builder.Default
    private Boolean isVoted = false;

    private String fullName;
    private String username;

    private String phoneNumber;

    private Integer lastMessageId;

    @Builder.Default
    private Boolean enabled = false;

    @Builder.Default
    private Boolean admin = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
