package org.exp.openbudjetadminbot.models.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class VoteApiResponse {
    private List<VoteContent> content;
    private Pageable pageable;
    private int totalPages;
    private long totalElements;
    private boolean last;
    private Sort sort;
    private int size;
    private int number;
    private int numberOfElements;
    private boolean first;
    private boolean empty;

    @Data
    public static class VoteContent {
        private String phoneNumber;
        private String voteDate; // "2025-08-22 20:36"
    }

    @Data
    public static class Pageable {
        private Sort sort;
        private long offset;
        private int pageNumber;
        private int pageSize;
        private boolean paged;
        private boolean unpaged;
    }

    @Data
    public static class Sort {
        private boolean sorted;
        private boolean unsorted;
        private boolean empty;
    }
}