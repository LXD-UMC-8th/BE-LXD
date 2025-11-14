package org.lxdproject.lxd.domain.diarycomment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.global.common.util.DateFormatUtil;
import org.lxdproject.lxd.domain.diarycomment.entity.DiaryComment;

@Getter
@AllArgsConstructor(staticName = "of")
public class DiaryCommentDeleteResponseDTO {

    private Long commentId;
    private boolean isDeleted;
    private String content;
    private String deletedAt;

    public static DiaryCommentDeleteResponseDTO from(DiaryComment comment) {
        return DiaryCommentDeleteResponseDTO.of(
                comment.getId(),
                comment.isDeleted(),
                comment.getCommentText(),
                DateFormatUtil.formatDate(comment.getDeletedAt())
        );
    }
}

