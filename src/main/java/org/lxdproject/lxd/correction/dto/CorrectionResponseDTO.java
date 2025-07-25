package org.lxdproject.lxd.correction.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CorrectionResponseDTO {

    @Getter
    @Builder
    public static class DiaryCorrectionsResponseDTO{
        private Long diaryId;
        private int totalCount; // 전체 교정 수
        private boolean hasNext; // 다음 페이지 존재 여부
        private List<CorrectionDetailDTO> corrections;
    }

    @Getter
    @Builder
    public static class CorrectionDetailDTO {
        private Long correctionId;
        private Long diaryId;
        private String createdAt;
        private MemberInfo member;
        private String original;
        private String corrected;
        private String commentText;
        private int likeCount;
        private int commentCount;
        private boolean isLikedByMe;
    }

    @Getter
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String userId;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    public static class ProvidedCorrectionItem {
        private Long correctionId;
        private Long diaryId;
        private String diaryTitle;
        private String diaryCreatedAt;
        private String createdAt;
        private String originalText;
        private String corrected;
        private String commentText;
        private Integer commentCount;
        private Integer likeCount;
    }

    @Getter
    @Builder
    public static class ProvidedCorrectionsResponseDTO {
        private MemberInfo member;
        private List<ProvidedCorrectionItem> corrections;
        private int page;
        private int size;
        private int totalCount;
        private boolean hasNext;
    }


    @Getter
    @Builder
    public static class CorrectionLikeResponseDTO {
        private Long correctionId;
        private Integer likeCount;
        private Boolean liked;
    }
}