package org.lxdproject.lxd.correctioncomment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class CorrectionCommentPageResponseDTO {
    private List<Comment> replies;
    private Long totalElements;

    @Getter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Comment {
        private Long commentId;
        private Long parentId;
        private Long userId;
        private String nickname;
        private String profileImage;
        private String content;
        private int likeCount;
        private boolean isLiked;
        private LocalDateTime createdAt;
        private List<Comment> replies;  //대댓글
    }

}



