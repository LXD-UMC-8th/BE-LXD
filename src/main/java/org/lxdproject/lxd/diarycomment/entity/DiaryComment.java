package org.lxdproject.lxd.diarycomment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.common.entity.BaseEntity;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.member.entity.Member;

@Entity
@Table(name = "diary_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DiaryComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DiaryComment parent; // 대댓글용 자기 참조

    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentText;

    private int likeCount;

    private int replyCount;

    public void increaseReplyCount(){
        this.replyCount +=1;
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(this.likeCount - 1, 0);
    }

    public String getCommentText() {
        if (isDeleted()) {
            return "삭제된 댓글입니다.";
        }
        return this.commentText;
    }

}