package org.lxdproject.lxd.domain.diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.domain.diary.entity.enums.Language;
import org.lxdproject.lxd.domain.diary.entity.enums.Visibility;

@Getter
@AllArgsConstructor
@Builder
public class DiarySummaryResponseDTO {
    private MemberProfileDTO writerMemberProfile;
    private Long diaryId;
    private String createdAt;
    private String title;
    private Visibility visibility;
    private String thumbnailUrl;
    private int likeCount;
    private int commentCount;
    private int correctionCount;
    private String contentPreview;
    private Language language;
    @JsonProperty("isLiked")
    private boolean liked;
}

