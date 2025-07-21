package org.lxdproject.lxd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.member.entity.Member;

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
    private CommentPermission commentPermission;
    private String thumbnail;

    public static DiaryDetailResponseDTO from(Diary diary) {
        Member member = diary.getMember();

        return new DiaryDetailResponseDTO(
                diary.getId(),
                diary.getVisibility(),
                diary.getTitle(),
                diary.getLanguage(),
                member.getProfileImg(),
                member.getNickname(),
                member.getUsername(),
                diary.getCreatedAt(),
                diary.getCommentCount(),
                diary.getLikeCount(),
                diary.getCorrectionCount(),
                diary.getContent(),
                diary.getCommentPermission(),
                diary.getThumbImg()
        );
    }

}
