package org.lxdproject.lxd.correction.dto;

import lombok.Builder;
import lombok.Getter;

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
}