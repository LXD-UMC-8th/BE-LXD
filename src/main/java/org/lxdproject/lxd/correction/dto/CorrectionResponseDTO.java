package org.lxdproject.lxd.correction.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CorrectionResponseDTO {

    @Getter
    @Builder
    public static class CreateResponseDTO {
        private Long correctionId;
        private Long diaryId;
        private String createdAt;
        private AuthorDTO author;
        private String original;
        private String corrected;
        private String commentText;
        private int likeCount;
        private int commentCount;
        private boolean isLikedByMe;
    }

    @Getter
    @Builder
    public static class AuthorDTO {
        private Long memberId;
        private String userId;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    public static class CorrectionItem {
        private Long correctionId;
        private Long diaryId;
        private String diaryTitle;
        private String diaryCreatedAt;
        private String createdAt;
        private String original;
        private String corrected;
        private String commentText;
    }

    @Getter
    @Builder
    public static class ProvidedCorrectionsResponseDTO {
        private AuthorDTO member;
        private List<CorrectionItem> corrections;
        private int page;
        private int size;
        private int totalCount;
        private boolean hasNext;
    }
}