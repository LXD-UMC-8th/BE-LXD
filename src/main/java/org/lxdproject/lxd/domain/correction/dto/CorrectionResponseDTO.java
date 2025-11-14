package org.lxdproject.lxd.domain.correction.dto;

import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.global.common.dto.PageDTO;
import org.lxdproject.lxd.domain.correction.entity.Correction;
import org.lxdproject.lxd.global.common.util.DateFormatUtil;
import org.lxdproject.lxd.domain.diary.entity.Diary;


public class CorrectionResponseDTO {

    @Getter
    @Builder
    public static class DiaryCorrectionsResponseDTO {
        private Long diaryId;
        private PageDTO<CorrectionDetailDTO> corrections;
    }

    @Getter
    @Builder
    public static class CorrectionDetailDTO {
        private Long correctionId;
        private Long diaryId;
        private String createdAt;
        private MemberProfileDTO memberProfile;
        private String original;
        private String corrected;
        private String commentText;
        private int likeCount;
        private int commentCount;
        private boolean isLikedByMe;
    }

    @Getter
    @Builder
    public static class DiaryInfo {
        private Long diaryId;
        private String title;
        private String thumbImg;
        private String createdAt;
        private String username;
        private String userProfileImg;

        public static DiaryInfo from(Diary diary) {
            return DiaryInfo.builder()
                    .diaryId(diary.getId())
                    .title(diary.getTitle())
                    .thumbImg(diary.getThumbImg())
                    .createdAt(DateFormatUtil.formatDate(diary.getCreatedAt()))
                    .username(diary.getMember().getUsername())
                    .userProfileImg(diary.getMember().getProfileImg())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProvidedCorrectionItem {
        private Long correctionId;
        private String createdAt;
        private String originalText;
        private String corrected;
        private String commentText;
        private Integer commentCount;
        private Integer likeCount;
        private DiaryInfo diaryInfo;

        public static ProvidedCorrectionItem from(Correction correction) {
            Diary diary = correction.getDiary();

            DiaryInfo diaryInfo = (diary == null || diary.isDeleted())
                    ? null
                    : DiaryInfo.from(diary);

            return ProvidedCorrectionItem.builder()
                    .correctionId(correction.getId())
                    .createdAt(DateFormatUtil.formatDate(correction.getCreatedAt()))
                    .originalText(correction.getOriginalText())
                    .corrected(correction.getCorrected())
                    .commentText(correction.getCommentText())
                    .commentCount(correction.getCommentCount())
                    .likeCount(correction.getLikeCount())
                    .diaryInfo(diaryInfo)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProvidedCorrectionsResponseDTO {
        private MemberProfileDTO memberProfile;
        private PageDTO<ProvidedCorrectionItem> corrections;
    }

    @Getter
    @Builder
    public static class CorrectionLikeResponseDTO {
        private Long correctionId;
        private Long memberId;
        private Integer likeCount;
        private Boolean liked;
    }
}
