package org.lxdproject.lxd.domain.correctionlike.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.global.common.entity.BaseEntity;
import org.lxdproject.lxd.domain.correction.entity.Correction;
import org.lxdproject.lxd.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSavedCorrection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_id", nullable = false)
    private Correction correction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Setter
    @Column(name = "memo")
    private String memo;

}

