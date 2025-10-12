package org.lxdproject.lxd.diarycommentlike.dto;

import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.MemberProfileDTO;

@Getter
@Builder
public class DiaryCommentLikeResponseDTO {
    private Long commentId;
    private MemberProfileDTO memberProfile;
    private boolean liked;
    private int likeCount;
}
