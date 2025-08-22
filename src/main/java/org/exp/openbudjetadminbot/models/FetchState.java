package org.exp.openbudjetadminbot.models;

import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FetchState {
    @Id
    private String id = "default"; // Yagona row uchun fixed ID
    private String currentUuid; // Oxirgi ishlatilgan UUID
    private int lastPage; // Oxirgi saqlangan page
    private LocalDateTime lastUpdate; // Oxirgi yangilangan vaqt
}