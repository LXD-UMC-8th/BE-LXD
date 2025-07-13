package org.lxdproject.lxd.diary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;

@Table(name = "일기")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;

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

    private Integer likeCount;

    private Integer commentCount;

    private Integer correctionCount;

    @Column(columnDefinition = "TEXT")
    private String thumbImg;

    // 좋아요 연관관계
//    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<DiaryLike> likes = new ArrayList<>();
}

