package org.lxdproject.lxd.correction.service;

import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.authz.guard.MemberGuard;
import org.lxdproject.lxd.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.common.dto.PageDTO;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correctionlike.entity.MemberSavedCorrection;
import org.lxdproject.lxd.correctionlike.repository.MemberSavedCorrectionRepository;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.service.NotificationService;
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
    private final NotificationService notificationService;
    private final MemberGuard memberGuard;

    @Transactional(readOnly = true)
    public CorrectionResponseDTO.DiaryCorrectionsResponseDTO getCorrectionsByDiaryId(Long diaryId, int page, int size) {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));
        memberGuard.checkOwnerIsNotDeleted(diary.getMember());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Correction> correctionPage = correctionRepository.findByDiaryId(diaryId, pageable);

        Set<Long> likedIds = findLikedCorrectionIds(member, correctionPage.getContent());

        List<CorrectionResponseDTO.CorrectionDetailDTO> correctionDetailList = correctionPage.stream()
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
                        .memberProfile(MemberProfileDTO.from(correction.getAuthor()))
                        .build())
                .toList();

        PageDTO<CorrectionResponseDTO.CorrectionDetailDTO> pageDTO = new PageDTO<>(
                correctionPage.getTotalElements(),
                correctionDetailList,
                page + 1, // 클라이언트는 1부터
                size,
                correctionPage.hasNext()
        );

        return CorrectionResponseDTO.DiaryCorrectionsResponseDTO.builder()
                .diaryId(diaryId)
                .corrections(pageDTO)
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
        memberGuard.checkOwnerIsNotDeleted(diary.getMember());

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

        Member diaryOwner = diary.getMember();
        if (!member.equals(diaryOwner)) {
            NotificationRequestDTO dto = NotificationRequestDTO.builder()
                    .receiverId(diaryOwner.getId())
                    .notificationType(NotificationType.CORRECTION_ADDED)
                    .targetType(TargetType.CORRECTION)
                    .targetId(saved.getId())
                    .redirectUrl("/diaries/" + correction.getDiary().getId() + "/corrections/" + correction.getId())
                    .build();

            notificationService.saveAndPublishNotification(dto);
        }

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
                .memberProfile(MemberProfileDTO.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImage(member.getProfileImg())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public CorrectionResponseDTO.ProvidedCorrectionsResponseDTO getMyProvidedCorrections(int page, int size) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Correction> correctionPage = correctionRepository.findByAuthor(member, pageable);

        List<CorrectionResponseDTO.ProvidedCorrectionItem> items = correctionPage.getContent().stream()
                .map(CorrectionResponseDTO.ProvidedCorrectionItem::from)
                .toList();

        PageDTO<CorrectionResponseDTO.ProvidedCorrectionItem> pageDTO = new PageDTO<>(
                correctionPage.getTotalElements(),
                items,
                correctionPage.getNumber() + 1,
                correctionPage.getSize(),
                correctionPage.hasNext()
        );

        return CorrectionResponseDTO.ProvidedCorrectionsResponseDTO.builder()
                .memberProfile(MemberProfileDTO.from(member))
                .corrections(pageDTO)
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