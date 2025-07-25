package org.lxdproject.lxd.diarycomment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class DiaryCommentDeleteResponseDTO {

    private Long commentId;
    private boolean isDeleted;
    private String content;
    private LocalDateTime deletedAt;

    public static DiaryCommentDeleteResponseDTO from(DiaryComment comment) {
        return DiaryCommentDeleteResponseDTO.of(
                comment.getId(),
                comment.isDeleted(),
                comment.getCommentText(),
                comment.getDeletedAt()
        );
    }
}

