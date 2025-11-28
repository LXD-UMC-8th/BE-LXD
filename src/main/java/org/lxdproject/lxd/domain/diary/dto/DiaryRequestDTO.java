package org.lxdproject.lxd.domain.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.lxdproject.lxd.domain.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.domain.diary.entity.enums.Language;
import org.lxdproject.lxd.domain.diary.entity.enums.Style;
import org.lxdproject.lxd.domain.diary.entity.enums.Visibility;
import org.lxdproject.lxd.global.validation.annotation.MaxImageCount;

@Getter @Setter
public class DiaryRequestDTO {
    @NotBlank(message = "제목을 작성해주세요.")
    private String title;
    @NotBlank(message = "내용을 작성해주세요.")
    @MaxImageCount
    @Size(max = 10000, message = "내용은 최대 10,000자까지 작성 가능합니다.")
    private String content;
    private Style style;
    private Visibility visibility;
    private CommentPermission commentPermission;
    private Language language;
    private String thumbImg;
}
