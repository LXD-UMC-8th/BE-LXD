package org.lxdproject.lxd.domain.correctioncomment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.global.common.entity.BaseEntity;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.lxdproject.lxd.domain.correction.entity.Correction;

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
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public String getDisplayContent() {
        return isDeleted() ? "삭제된 댓글입니다." : this.content;
    }

}

