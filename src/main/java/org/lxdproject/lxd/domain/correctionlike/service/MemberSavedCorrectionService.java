package org.lxdproject.lxd.domain.correctionlike.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.global.common.dto.PageDTO;
import org.lxdproject.lxd.global.config.security.SecurityUtil;
import org.lxdproject.lxd.domain.correctionlike.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.domain.correctionlike.dto.MemberSavedCorrectionResponseDTO;
import org.lxdproject.lxd.domain.correction.entity.Correction;
import org.lxdproject.lxd.domain.correctionlike.entity.MemberSavedCorrection;
import org.lxdproject.lxd.domain.correctionlike.repository.MemberSavedCorrectionRepository;
import org.lxdproject.lxd.global.common.util.DateFormatUtil;
import org.lxdproject.lxd.domain.diary.entity.Diary;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberSavedCorrectionService {

    private final MemberSavedCorrectionRepository memberSavedCorrectionRepository;

    @Transactional(readOnly = true)
    public MemberSavedCorrectionResponseDTO.SavedListResponseDTO getMySavedCorrections(
            int page, int size
    ) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MemberSavedCorrection> savedPage =
                memberSavedCorrectionRepository.findByMemberId(currentMemberId, pageable);

        List<MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem> savedCorrectionDTOs =
                savedPage.stream()
                        .map(this::toSavedCorrectionDTO)
                        .toList();

        PageDTO<MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem> pageDTO =
                new PageDTO<>(
                        savedPage.getTotalElements(),
                        savedCorrectionDTOs,
                        savedPage.getNumber() + 1,
                        savedPage.getSize(),
                        savedPage.hasNext()
                );

        return MemberSavedCorrectionResponseDTO.SavedListResponseDTO.builder()
                .memberId(currentMemberId)
                .savedCorrections(pageDTO)
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO createMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberSavedCorrection entity = getSavedCorrectionOrThrow(request.getSavedCorrectionId());

        validateMemberAccess(entity, currentMemberId);

        if (entity.getMemo() != null && !entity.getMemo().isBlank()) {
            throw new CorrectionHandler(INVALID_CORRECTION_MEMO);
        }

        entity.setMemo(request.getMemo());
        return MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO.builder()
                .memberSavedCorrectionId(entity.getId())
                .createdMemo(request.getMemo())
                .createdAt(DateFormatUtil.formatDate(LocalDateTime.now()))
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO updateMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberSavedCorrection entity = getSavedCorrectionOrThrow(request.getSavedCorrectionId());

        validateMemberAccess(entity, currentMemberId);

        if (entity.getMemo() == null || entity.getMemo().isBlank()) {
            throw new CorrectionHandler(MEMO_NOT_FOUND);
        }

        entity.setMemo(request.getMemo());
        return MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO.builder()
                .updatedMemo(request.getMemo())
                .updatedAt(DateFormatUtil.formatDate(entity.getUpdatedAt()))
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO deleteMemo(Long memberSavedCorrectionId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberSavedCorrection entity = getSavedCorrectionOrThrow(memberSavedCorrectionId);

        validateMemberAccess(entity, currentMemberId);

        entity.setMemo(null);

        return MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO.builder()
                .memberSavedCorrectionId(entity.getId())
                .deletedAt(DateFormatUtil.formatDate(LocalDateTime.now()))
                .message("메모가 성공적으로 삭제되었습니다.")
                .build();
    }

    private MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem toSavedCorrectionDTO(MemberSavedCorrection entity) {
        Correction correction = Optional.ofNullable(entity.getCorrection())
                .orElseThrow(() -> new CorrectionHandler(CORRECTION_NOT_FOUND));

        Member member = correction.getAuthor();

        Diary diary = Optional.ofNullable(correction.getDiary())
                .orElseThrow(() -> new DiaryHandler(DIARY_NOT_FOUND));

        return MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.builder()
                .savedCorrectionId(entity.getId())
                .memo(entity.getMemo())
                .correction(MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.CorrectionInfo.builder()
                        .correctionId(correction.getId())
                        .originalText(correction.getOriginalText())
                        .corrected(correction.getCorrected())
                        .commentText(correction.getCommentText())
                        .likeCount(correction.getLikeCount())
                        .commentCount(correction.getCommentCount())
                        .correctionCreatedAt(DateFormatUtil.formatDate(correction.getCreatedAt()))
                        .build())
                .diary(MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.DiaryInfo.builder()
                        .diaryId(diary.getId())
                        .diaryTitle(diary.getTitle())
                        .diaryCreatedAt(DateFormatUtil.formatDate(diary.getCreatedAt()))
                        .thumbImg(diary.getThumbImg())
                        .diaryWriterId(diary.getMember().getId())
                        .build())
                .memberProfile(MemberProfileDTO.from(member))
                .build();
    }

    private MemberSavedCorrection getSavedCorrectionOrThrow(Long id) {
        return memberSavedCorrectionRepository.findById(id)
                .orElseThrow(() -> new CorrectionHandler(CORRECTION_NOT_FOUND));
    }

    private void validateMemberAccess(MemberSavedCorrection entity, Long currentMemberId) {
        if (!entity.getMember().getId().equals(currentMemberId)) {
            throw new AuthHandler(NOT_RESOURCE_OWNER);
        }
    }
}
