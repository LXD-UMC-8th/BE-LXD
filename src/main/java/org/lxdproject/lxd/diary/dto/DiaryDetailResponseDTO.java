package org.lxdproject.lxd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
    private Long writerId;
    private String profileImg;
    private String writerNickName;
    private String writerUserName;
    private Long writerId;
    private String createdAt;
    private int commentCount;
    private int likeCount;
    private int correctCount;
    private String content;
    private String diffHtml;
    private CommentPermission commentPermission;
    private String thumbnail;

    // 일반 조회용
    public static DiaryDetailResponseDTO from(Diary diary) {
        Member member = diary.getMember();

        return new DiaryDetailResponseDTO(
                diary.getId(),
                diary.getVisibility(),
                diary.getTitle(),
                diary.getLanguage(),
                member.getId(),
                member.getProfileImg(),
                member.getNickname(),
                member.getUsername(),
                member.getId(),
                DateFormatUtil.formatDate(diary.getCreatedAt()),
                diary.getCommentCount(),
                diary.getLikeCount(),
                diary.getCorrectionCount(),
                diary.getContent(),
                null, // diffHtml은 null 처리
                diary.getCommentPermission(),
                diary.getThumbImg()
        );
    }

    // diff 결과를 포함한 응답 생성용
    public static DiaryDetailResponseDTO fromWithDiff(Diary diary, String diffHtml) {
        Member member = diary.getMember();

        return new DiaryDetailResponseDTO(
                diary.getId(),
                diary.getVisibility(),
                diary.getTitle(),
                diary.getLanguage(),
                member.getId(),
                member.getProfileImg(),
                member.getNickname(),
                member.getUsername(),
                member.getId(),
                DateFormatUtil.formatDate(diary.getCreatedAt()),
                diary.getCommentCount(),
                diary.getLikeCount(),
                diary.getCorrectionCount(),
                diary.getContent(),
                diffHtml, // diff 결과 포함
                diary.getCommentPermission(),
                diary.getThumbImg()
        );
    }
}
