package org.lxdproject.lxd.diary.dto;

import lombok.Getter;
import lombok.Setter;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;

@Getter @Setter
public class DiaryRequestDTO {
    private Long memberId; // 추후 인증 기반 처리 예정
    private String title;
    private String content;
    private Style style;
    private Visibility visibility;
    private CommentPermission commentPermission;
    private Language language;
    private String thumbImg;
}
