package org.lxdproject.lxd.diarycomment.dto;

import lombok.Getter;

@Getter
public class DiaryCommentRequestDTO {
    private String commentText;
    private Long parentId; // null이면 일반 댓글
}

