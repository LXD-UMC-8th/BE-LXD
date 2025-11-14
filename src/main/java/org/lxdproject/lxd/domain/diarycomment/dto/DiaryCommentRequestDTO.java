package org.lxdproject.lxd.domain.diarycomment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryCommentRequestDTO {
    private Long parentId; // 대댓글일 경우 필요
    private String commentText;
}


