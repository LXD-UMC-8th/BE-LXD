package org.lxdproject.lxd.correction.dto;

import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageResponse;

public class MemberSavedCorrectionResponseDTO {

    @Getter
    @Builder
    public static class SavedListResponseDTO {
        private Long memberId;
        private PageResponse<SavedCorrectionItem> savedCorrections;

        @Getter
        @Builder
        public static class SavedCorrectionItem {
            private Long savedCorrectionId;
            private String memo;
            private CorrectionInfo correction;
            private DiaryInfo diary;
            private MemberInfo member;

            @Getter
            @Builder
            public static class CorrectionInfo {
                private Long correctionId;
                private String originalText;
                private String corrected;
                private String commentText;
                private String correctionCreatedAt;
                private Integer commentCount;
                private Integer likeCount;
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
            public static class MemberInfo {
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
        private String createdAt;
    }

    @Getter
    @Builder
    public static class UpdateMemoResponseDTO {

        private String updatedMemo;
        private String updatedAt;
    }

    @Getter
    @Builder
    public static class DeleteMemoResponseDTO {
        private Long memberSavedCorrectionId;
        private String deletedAt;
        private String message;
    }
}
