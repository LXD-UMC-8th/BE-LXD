package org.lxdproject.lxd.correction.repository;

import org.lxdproject.lxd.correction.entity.mapping.MemberSavedCorrection;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberSavedCorrectionRepository extends JpaRepository<MemberSavedCorrection, Long> {

    @Query("SELECT msc.correction.id FROM MemberSavedCorrection msc " +
            "WHERE msc.member = :member AND msc.correction.id IN :correctionIds")
    List<Long> findLikedCorrectionIdsByMember(
            @Param("member") Member member,
            @Param("correctionIds") List<Long> correctionIds
    );

    Slice<MemberSavedCorrection> findByMember_Id(Long memberId, Pageable pageable);
}

