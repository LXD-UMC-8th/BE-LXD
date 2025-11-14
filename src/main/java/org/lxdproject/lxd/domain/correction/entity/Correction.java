package org.lxdproject.lxd.domain.correction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.global.common.entity.BaseEntity;
import org.lxdproject.lxd.domain.correctionlike.entity.MemberSavedCorrection;
import org.lxdproject.lxd.domain.diary.entity.Diary;
import org.lxdproject.lxd.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(name = "original_text", nullable = false)
    private String originalText;

    @Column(name = "corrected", length = 300, nullable = false)
    private String corrected;

    // 교정에 대한 코멘트 내용
    @Column(name = "comment_text", length = 500)
    private String commentText;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberSavedCorrection> savedByMembers = new ArrayList<>();

    public void decreaseLikeCount(){
        if (this.likeCount > 0) this.likeCount--;
    }

    public void increaseLikeCount(){
        this.likeCount++;
    }

    public void decreaseCommentCount(){
        if (this.commentCount > 0) this.commentCount--;
    }

    public void increaseCommentCount(){
        this.commentCount++;
    }
}