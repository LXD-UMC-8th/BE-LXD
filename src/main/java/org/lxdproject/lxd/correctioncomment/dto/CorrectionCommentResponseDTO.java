package org.lxdproject.lxd.correctioncomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CorrectionCommentResponseDTO {
    private Long commentId;
    private int userId;
    private String nickname;
    private String profileImage;
    private String content;
    private Long parentId;
    private int likeCount;
    private boolean isLiked;
    private LocalDateTime createdAt;
}
