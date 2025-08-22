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
    private Long telegramId;

    private String fullName;
    private String username;

    private String phoneNumber;

    private String lastAction;

    @Builder.Default
    private Boolean enabled = false;

    @Builder.Default
    private Boolean admin = false;

    private LocalDateTime lastActionDate = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
}
