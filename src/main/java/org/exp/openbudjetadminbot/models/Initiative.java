package org.exp.openbudjetadminbot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "initiatives")
public class Initiative {

    @Id
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String publicId;

    private String type;

    private String regionTitle;
    private String districtTitle;
    private String quarterTitle;

    private Long categoryId;
    private String categoryTitle;

    private String authorFullname;
    private Boolean authorIsVerified;

    private String stage;

    private UUID author;

    private Long grantedAmount;
    private Long requestedAmount;

    private Long boardId;
    private String boardTitle;
    private String boardType;
    private Integer boardYear;
    private Integer boardSeason;

    private Long regionId;
    private Long districtId;
    private Long quarterId;

    private String quality;

    @Column(columnDefinition = "TEXT")
    private String moderatorComment;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private Integer roadLength;

    private String roadType;

    private Long customerId;

    private String moderationSubStage;
}