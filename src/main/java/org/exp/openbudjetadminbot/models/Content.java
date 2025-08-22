package org.exp.openbudjetadminbot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contents")
public class Content {

    @Id
    private UUID id;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "quarter_name")
    private String quarterName;

    @Column(name = "public_id")
    private String publicId;

    private String title;

    private String stage;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "region_name")
    private String regionName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "vote_count")
    private Integer voteCount;

    private Double coefficient;

    @Column(name = "granted_amount")
    private Long grantedAmount;

    @Column(name = "requested_amount")
    private Long requestedAmount;

    @Column(name = "public_control_quality")
    private String publicControlQuality;

    @ElementCollection
    @CollectionTable(name = "content_images", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "image_id")
    private List<String> images;
}