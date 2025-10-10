package org.lxdproject.lxd.diary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import org.lxdproject.lxd.common.entity.BaseEntity;
import org.lxdproject.lxd.diary.dto.DiaryUpdateRequestDTO;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diarylike.entity.DiaryLike;
import org.lxdproject.lxd.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // 최초 작성 원문 내용

    @Column(columnDefinition = "TEXT")
    private String diffContent; // diff (<del>, <ins>) 포함된 최종 본문

    @Column(columnDefinition = "TEXT")
    private String modifiedContent; // diff 없는 최종 본문

    @Column(columnDefinition = "TEXT")
    private String previewContent; // 일기 수정 후 요약 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Style style;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    @Builder.Default
    private CommentPermission commentPermission = CommentPermission.ALL;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Builder.Default
    private Integer likeCount=0;

    @Builder.Default
    private Integer commentCount=0;

    @Builder.Default
    private Integer correctionCount=0;

    @Column(columnDefinition = "TEXT")
    private String thumbImg;

    // 좋아요 연관관계
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryLike> likes = new ArrayList<>();

    public void update(DiaryUpdateRequestDTO dto, String diffContent, String modifiedContent, String previewContent) {
        this.title = dto.getTitle();
        this.diffContent = diffContent;
        this.modifiedContent = modifiedContent;
        this.previewContent = previewContent;
        this.visibility = dto.getVisibility();
        this.commentPermission = dto.getCommentPermission();
        this.language = dto.getLanguage();
        this.thumbImg = dto.getThumbImg();
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void increaseCorrectionCount() {
        this.correctionCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }


}

