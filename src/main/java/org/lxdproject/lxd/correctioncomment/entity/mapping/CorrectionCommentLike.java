package org.lxdproject.lxd.correctioncomment.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.common.entity.BaseEntity;

@Entity
@Table(name = "correction_comment_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "correction_comment_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CorrectionCommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 좋아요 누른 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 좋아요 대상 교정 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_comment_id", nullable = false)
    private CorrectionComment correctionComment;
}

