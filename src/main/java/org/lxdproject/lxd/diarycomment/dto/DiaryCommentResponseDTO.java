package org.lxdproject.lxd.diarycomment.dto;

import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DiaryCommentResponseDTO {
    private Long commentId; // 댓글 ID
    private Long userId; // 작성자 ID
    private String nickname;  // 작성자 닉네임
    private Long diaryId;
    private String profileImage;
    private String commentText;
    private Long parentId; // nullable
    private int likeCount;
    private boolean isLiked;
    private LocalDateTime createdAt;

    @Getter
    @Builder
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

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentList {
        private List<Comment> content;
        private int totalElements;
    }

}
