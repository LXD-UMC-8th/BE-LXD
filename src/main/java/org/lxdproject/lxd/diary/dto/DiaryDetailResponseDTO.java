package org.lxdproject.lxd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Visibility;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryDetailResponseDTO {
    private Long diaryId;
    private Visibility visibility;
    private String title;
    private Language language;
    private String profileImg;
    private String writerNickName;
    private String writerUserName;
    private LocalDateTime createdAt;
    private int commentCount;
    private int likeCount;
    private int correctCount;
    private String content;
}
