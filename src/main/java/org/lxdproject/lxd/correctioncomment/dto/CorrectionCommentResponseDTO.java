package org.lxdproject.lxd.correctioncomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CorrectionCommentResponseDTO {
    private Long commentId;
    private int memberId;
    private Long parentId;
    private String nickname;
    private String profileImage; 필요없음
    private String content;
    private int likeCount;
    private boolean isLiked;
    private LocalDateTime createdAt;
}
