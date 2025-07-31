package org.lxdproject.lxd.correctioncomment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrectionCommentResponseDTO {
    private Long commentId;
    private Long memberId;
    private String nickname;
    private String profileImage;
    private String content;
    private Long parentId;
    private int likeCount;
    private boolean isLiked;
    private LocalDateTime createdAt;
}
