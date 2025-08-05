package org.lxdproject.lxd.diarylike.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.diarylike.dto.DiaryLikeResponseDTO;
import org.lxdproject.lxd.diarylike.entity.DiaryLike;
import org.lxdproject.lxd.diarylike.repository.DiaryLikeRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryLikeService {

    private final DiaryLikeRepository diaryLikeRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryLikeResponseDTO.ToggleDiaryLikeResponseDTO toggleDiaryLike(Long diaryId) {

        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() ->  new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

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
                .memberId(memberId)
                .liked(liked)
                .likedCount(diary.getLikeCount())
                .build();

    }
}
