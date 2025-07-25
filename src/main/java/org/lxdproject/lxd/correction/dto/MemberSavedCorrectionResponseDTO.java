package org.lxdproject.lxd.correction.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class MemberSavedCorrectionResponseDTO {

    @Getter
    @Builder
    public static class SavedListResponseDTO {
        private Long memberId;
        private List<SavedCorrectionItem> savedCorrections;
        private int page;
        private int size;
        private boolean hasNext;

        @Getter
        @Builder
        public static class SavedCorrectionItem {
            private Long savedCorrectionId;
            private String memo;
            private CorrectionInfo correction;
            private DiaryInfo diary;
            private AuthorInfo author;

            @Getter
            @Builder
            public static class CorrectionInfo {
                private Long correctionId;
                private String originalText;
                private String corrected;
                private String commentText;
                private String correctionCreatedAt;
            }

            @Getter
            @Builder
            public static class DiaryInfo {
                private Long diaryId;
                private String diaryTitle;
                private String diaryCreatedAt;
            }

            @Getter
            @Builder
            public static class AuthorInfo {
                private Long memberId;
                private String userId;
                private String nickname;
                private String profileImageUrl;
            }
        }
    }

    @Getter
    @Builder
    public static class CreateMemoResponseDTO {

        private Long memberSavedCorrectionId;
        private String createdMemo;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class UpdateMemoResponseDTO {

        private String updatedMemo;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    public static class DeleteMemoResponseDTO {

        private Long memberSavedCorrectionId;
        private LocalDateTime deletedAt;
        private String message;
    }
}
