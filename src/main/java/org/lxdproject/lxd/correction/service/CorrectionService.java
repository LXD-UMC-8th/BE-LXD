package org.lxdproject.lxd.correction.service;

import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correction.entity.mapping.MemberSavedCorrection;
import org.lxdproject.lxd.correction.repository.MemberSavedCorrectionRepository;
import org.lxdproject.lxd.correction.util.DateFormatUtil;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private final CorrectionRepository correctionRepository;
    private final DiaryRepository diaryRepository;
    private final MemberSavedCorrectionRepository memberSavedCorrectionRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public CorrectionResponseDTO.DiaryCorrectionsResponseDTO getCorrectionsByDiaryId(
            Long diaryId, int page, int size) {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));


        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Correction> correctionSlice = correctionRepository.findByDiaryId(diaryId, pageable);

        if (correctionSlice.isEmpty()) {
            return emptyDiaryCorrectionsResponse(diaryId);
        }

        Set<Long> likedIds = findLikedCorrectionIds(member, correctionSlice.getContent());

        List<CorrectionResponseDTO.CorrectionDetailDTO> correctionDetailList = correctionSlice.stream()
                .map(correction -> CorrectionResponseDTO.CorrectionDetailDTO.builder()
                        .correctionId(correction.getId())
                        .diaryId(correction.getDiary().getId())
                        .createdAt(DateFormatUtil.formatDate(correction.getCreatedAt()))
                        .original(correction.getOriginalText())
                        .corrected(correction.getCorrected())
                        .commentText(correction.getCommentText())
                        .likeCount(correction.getLikeCount())
                        .commentCount(correction.getCommentCount())
                        .isLikedByMe(likedIds.contains(correction.getId()))
                        .member(CorrectionResponseDTO.MemberInfo.builder()
                                .memberId(correction.getAuthor().getId())
                                .userId(correction.getAuthor().getUsername())
                                .nickname(correction.getAuthor().getNickname())
                                .profileImageUrl(correction.getAuthor().getProfileImg())
                                .build())
                        .build())
                .toList();

        return CorrectionResponseDTO.DiaryCorrectionsResponseDTO.builder()
                .diaryId(diaryId)
                .totalCount(correctionDetailList.size())
                .hasNext(correctionSlice.hasNext())
                .corrections(correctionDetailList)
                .build();
    }

    @Transactional
    public CorrectionResponseDTO.CorrectionLikeResponseDTO toggleLikeCorrection(
            Long correctionId
    ) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Correction correction = correctionRepository.findByIdWithPessimisticLock(correctionId).orElseThrow(()
                -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));
        Member member = memberRepository.findById(currentMemberId).orElseThrow(()
                -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        final boolean[] liked = new boolean[1];

        memberSavedCorrectionRepository.findByCorrectionIdAndMember_Id(correctionId, currentMemberId)
                .ifPresentOrElse(existing -> {
                    memberSavedCorrectionRepository.delete(existing);
                    correction.decreaseLikeCount();
                    liked[0] = false;
                }, () -> {
                    memberSavedCorrectionRepository.save(MemberSavedCorrection.builder()
                            .member(member)
                            .correction(correction)
                            .memo(null)
                            .build());
                    correction.increaseLikeCount();
                    liked[0] = true;
                });

        return CorrectionResponseDTO.CorrectionLikeResponseDTO.builder()
                .correctionId(correction.getId())
                .memberId(currentMemberId)
                .liked(liked[0])
                .likeCount(correction.getLikeCount())
                .build();
    }


    @Transactional
    public CorrectionResponseDTO.CorrectionDetailDTO createCorrection(
            CorrectionRequestDTO.CreateRequestDTO requestDto
            ) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(requestDto.getDiaryId())
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Correction correction = Correction.builder()
                .diary(diary)
                .author(member)
                .originalText(requestDto.getOriginal())
                .corrected(requestDto.getCorrected())
                .commentText(requestDto.getCommentText())
                .likeCount(0)
                .commentCount(0)
                .build();

        Correction saved = correctionRepository.save(correction);
        diary.increaseCorrectionCount();

        return CorrectionResponseDTO.CorrectionDetailDTO.builder()
                .correctionId(saved.getId())
                .diaryId(saved.getDiary().getId())
                .createdAt(DateFormatUtil.formatDate(saved.getCreatedAt()))
                .original(saved.getOriginalText())
                .corrected(saved.getCorrected())
                .commentText(saved.getCommentText())
                .likeCount(saved.getLikeCount())
                .commentCount(saved.getCommentCount())
                .isLikedByMe(false)
                .member(CorrectionResponseDTO.MemberInfo.builder()
                        .memberId(member.getId())
                        .userId(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImg())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public CorrectionResponseDTO.ProvidedCorrectionsResponseDTO getMyProvidedCorrections(int page, int size) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Correction> corrections = correctionRepository.findByAuthor(member, pageable);

        List<CorrectionResponseDTO.ProvidedCorrectionItem> items = corrections.getContent().stream()
                .map(CorrectionResponseDTO.ProvidedCorrectionItem::from)
                .toList();

        return CorrectionResponseDTO.ProvidedCorrectionsResponseDTO.from(
                member,
                items,
                page,
                size,
                corrections.hasNext()
        );
    }

    private CorrectionResponseDTO.DiaryCorrectionsResponseDTO emptyDiaryCorrectionsResponse(Long diaryId) {
        return CorrectionResponseDTO.DiaryCorrectionsResponseDTO.builder()
                .diaryId(diaryId)
                .totalCount(0)
                .hasNext(false)
                .corrections(Collections.emptyList())
                .build();
    }

    private Set<Long> findLikedCorrectionIds(Member member, List<Correction> corrections) {
        List<Long> correctionIds = corrections.stream()
                .map(Correction::getId)
                .toList();

        return new HashSet<>(memberSavedCorrectionRepository
                .findLikedCorrectionIdsByMember(member, correctionIds));
    }
}