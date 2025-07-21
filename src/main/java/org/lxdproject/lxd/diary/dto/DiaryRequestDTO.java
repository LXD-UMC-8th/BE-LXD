package org.lxdproject.lxd.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.validation.annotation.MaxImageCount;

@Getter @Setter
public class DiaryRequestDTO {
    @NotBlank(message = "제목을 작성해주세요.")
    private String title;
    @NotBlank(message = "내용을 작성해주세요.")
    @MaxImageCount
    private String content;
    private Style style;
    private Visibility visibility;
    private CommentPermission commentPermission;
    private Language language;
    private String thumbImg;
}
