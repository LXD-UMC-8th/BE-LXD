package org.lxdproject.lxd.diarycomment.repository;

import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
    // 부모 댓글 페이징
    Page<DiaryComment> findByDiaryIdAndParentIsNull(Long diaryId, Pageable pageable);

    // 자식 댓글 일괄 조회
    List<DiaryComment> findByParentIdIn(List<Long> parentIds);

}