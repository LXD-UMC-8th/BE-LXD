package org.lxdproject.lxd.domain.diarylike.service;

import org.lxdproject.lxd.global.authz.guard.MemberGuard;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.global.config.security.SecurityUtil;
import org.lxdproject.lxd.domain.diary.entity.Diary;
import org.lxdproject.lxd.domain.diary.repository.DiaryRepository;
import org.lxdproject.lxd.domain.diarylike.dto.DiaryLikeResponseDTO;
import org.lxdproject.lxd.domain.diarylike.entity.DiaryLike;
import org.lxdproject.lxd.domain.diarylike.repository.DiaryLikeRepository;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.lxdproject.lxd.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryLikeService {

    private final DiaryLikeRepository diaryLikeRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final MemberGuard memberGuard;

    public DiaryLikeResponseDTO.ToggleDiaryLikeResponseDTO toggleDiaryLike(Long diaryId) {

        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() ->  new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));
        memberGuard.checkOwnerIsNotDeleted(diary.getMember());

        DiaryLike diaryLike = diaryLikeRepository.findByMemberAndDiary(member, diary)
                .orElse(null);

        Boolean liked;

        // 기존에 사용자가 좋아요를 한 경우 (좋아요 취소)
        if(diaryLike != null) {
            diaryLikeRepository.delete(diaryLike);

            diary.decreaseLikeCount();
            liked = Boolean.FALSE;

        }else{ // 기존에 사용자가 좋아요를 하지 않은 경우 (좋아요)
            DiaryLike newDiaryLike = DiaryLike.builder()
                    .member(member)
                    .diary(diary)
                    .build();

            diaryLikeRepository.save(newDiaryLike);

            diary.increaseLikeCount();
            liked = Boolean.TRUE;

        }

        return DiaryLikeResponseDTO.ToggleDiaryLikeResponseDTO.builder()
                .diaryId(diaryId)
                .memberProfile(MemberProfileDTO.from(member))
                .liked(liked)
                .likedCount(diary.getLikeCount())
                .build();

    }
}
