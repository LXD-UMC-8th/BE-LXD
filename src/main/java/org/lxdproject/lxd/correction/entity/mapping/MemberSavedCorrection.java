package org.lxdproject.lxd.correction.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSavedCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_id", nullable = false)
    private Correction correction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "memo")
    private String memo;
}

