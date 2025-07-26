package org.lxdproject.lxd.correctioncomment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.common.entity.BaseEntity;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.correction.entity.Correction;

import java.time.LocalDateTime;

@Entity
@Table(name = "correction_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CorrectionComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계: 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 연관관계: 교정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_id", nullable = false)
    private Correction correction;

    // 댓글 내용
    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

    // 좋아요 수
    @Column(name = "like_count", nullable = false)
    private int likeCount;

    // 좋아요 증가
    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    // 좋아요 감소
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }


    //baseEntity상속으로 변경
    public String getCommentText() {
        if (isDeleted()) {
            return "삭제된 댓글입니다.";
        }
        return this.commentText;
    }
}

