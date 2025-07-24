package org.lxdproject.lxd.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import org.lxdproject.lxd.common.entity.BaseEntity;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.entity.mapping.DiaryLike;
import org.lxdproject.lxd.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Table(name = "일기")
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
    private String content;

    @Enumerated(EnumType.STRING)
    private Style style;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    private CommentPermission commentPermission;

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

    public void update(DiaryRequestDTO dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.style = dto.getStyle();
        this.visibility = dto.getVisibility();
        this.commentPermission = dto.getCommentPermission();
        this.language = dto.getLanguage();
        this.thumbImg = dto.getThumbImg();
    }

}

