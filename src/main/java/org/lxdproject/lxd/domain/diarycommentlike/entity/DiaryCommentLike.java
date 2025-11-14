package org.lxdproject.lxd.domain.diarycommentlike.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.global.common.entity.BaseEntity;
import org.lxdproject.lxd.domain.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.domain.member.entity.Member;

@Entity
@Table(name = "diary_comment_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "comment_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DiaryCommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private DiaryComment comment;

}

