package org.lxdproject.lxd.domain.diarycomment.dto;

import lombok.*;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;


import java.util.List;

@Getter
@Builder
public class DiaryCommentResponseDTO {
    private Long commentId; // 댓글 ID
    private Long diaryId;
    private Long parentId; // nullable
    private MemberProfileDTO memberProfile;
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
        private MemberProfileDTO memberProfile;
        private String content;
        private int likeCount;
        private boolean isLiked;
        private String createdAt;
        private int replyCount;  //대댓글 개수
        private List<Comment> replies;  //대댓글
    }

}
