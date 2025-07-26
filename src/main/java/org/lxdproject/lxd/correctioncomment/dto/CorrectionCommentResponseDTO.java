package org.lxdproject.lxd.correctioncomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CorrectionCommentResponseDTO {
    private Long commentId;
    private Long parentId; // 현재 구조에서는 null (확장 시 대댓글 가능)
    private String nickname;
    private String profileImage;
    private String content;
    private int likeCount;
    private boolean isLiked; // 추후 구현->좋아요
    private LocalDateTime createdAt;
}

