package org.lxdproject.lxd.diarycomment.dto;

import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DiaryCommentResponseDTO {
    private Long commentId; // 댓글 ID
    private Long memberId; // 작성자 ID
    private String username;
    private String nickname;  // 작성자 닉네임
    private Long diaryId;
    private Long parentId; // nullable
    private String profileImage;
    private String commentText;
    private int replyCount;
    private int likeCount;
    private boolean isLiked;
    private String createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Comment {
        private Long commentId;
        private Long parentId;
        private Long memberId;
        private String username;
        private String nickname;
        private String profileImage;
        private String content;
        private int likeCount;
        private boolean isLiked;
        private String createdAt;
        private int replyCount;  //대댓글 개수
        private List<Comment> replies;  //대댓글
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentList {
        private List<Comment> content;
        private long totalParentComments; // 부모 댓글 총 개수
        private int totalElements;
        private int pageItemCount; // 이 페이지에 포함된 실제 댓글수(부모+대댓글)
    }

    public record ExtendedPageResponse<T>(
            Long totalElements,
            Long parentTotalElements,
            Integer page,
            Integer size,
            Integer totalPages,
            Integer pageItemCount,
            Boolean hasNext,
            List<T> contents
    ) {}
}
