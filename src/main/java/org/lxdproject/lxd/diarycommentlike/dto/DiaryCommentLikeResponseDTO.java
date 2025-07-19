package org.lxdproject.lxd.diarycommentlike.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryCommentLikeResponseDTO {
    private Long commentId;
    private Long memberId;
    private boolean liked;
    private int likeCount;
}
