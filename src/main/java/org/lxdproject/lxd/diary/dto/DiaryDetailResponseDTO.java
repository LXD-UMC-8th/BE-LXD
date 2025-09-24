package org.lxdproject.lxd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.member.entity.Member;

@Getter
@AllArgsConstructor
@Builder
public class DiaryDetailResponseDTO {
    private Long diaryId;
    private Visibility visibility;
    private String title;
    private Language language;
    private MemberProfileDTO memberProfile;
    private String createdAt;
    private int commentCount;
    private int likeCount;
    private int correctCount;
    private String content;
    private String diffHtml;
    private CommentPermission commentPermission;
    private String thumbnail;
    private Boolean isLiked;

    public static DiaryDetailResponseDTO from(Diary diary, String diffHtml, boolean liked) {
        Member member = diary.getMember();

        return new DiaryDetailResponseDTO(
                diary.getId(),
                diary.getVisibility(),
                diary.getTitle(),
                diary.getLanguage(),
                MemberProfileDTO.from(member),
                DateFormatUtil.formatDate(diary.getCreatedAt()),
                diary.getCommentCount(),
                diary.getLikeCount(),
                diary.getCommentCount(),
                diary.getContent(),
                diffHtml,
                diary.getCommentPermission(),
                diary.getThumbImg(),
                liked
        );
    }
}
