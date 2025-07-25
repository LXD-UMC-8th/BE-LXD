package org.lxdproject.lxd.correction.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionResponseDTO;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.entity.mapping.MemberSavedCorrection;
import org.lxdproject.lxd.correction.repository.MemberSavedCorrectionRepository;
import org.lxdproject.lxd.correction.util.DateFormatUtil;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.lxdproject.lxd.apiPayload.code.status.ErrorStatus.*;

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
        Slice<MemberSavedCorrection> slice = memberSavedCorrectionRepository.findByMember_Id(currentMemberId, pageable);

        List<MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem> savedCorrectionDTOs =
                slice.stream()
                        .map(this::toSavedCorrectionDTO)
                        .collect(Collectors.toList());

        return MemberSavedCorrectionResponseDTO.SavedListResponseDTO.builder()
                .memberId(currentMemberId)
                .savedCorrections(savedCorrectionDTOs)
                .page(page)
                .size(size)
                .hasNext(slice.hasNext())
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO createMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberSavedCorrection entity = getCorrectionOrThrow(request.getMemberSavedCorrectionId());

        validateMemberAccess(entity, currentMemberId);

        if (entity.getMemo() != null && !entity.getMemo().isBlank()) {
            throw new CorrectionHandler(INVALID_CORRECTION_MEMO);
        }

        entity.setMemo(request.getMemo());
        return MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO.builder()
                .memberSavedCorrectionId(entity.getId())
                .createdMemo(request.getMemo())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO updateMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberSavedCorrection entity = getCorrectionOrThrow(request.getMemberSavedCorrectionId());

        validateMemberAccess(entity, currentMemberId);

        if (entity.getMemo() == null || entity.getMemo().isBlank()) {
            throw new CorrectionHandler(MEMO_NOT_FOUND);
        }

        entity.setMemo(request.getMemo());
        return MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO.builder()
                .updatedMemo(request.getMemo())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO deleteMemo(Long memberSavedCorrectionId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberSavedCorrection entity = getCorrectionOrThrow(memberSavedCorrectionId);

        validateMemberAccess(entity, currentMemberId);

        entity.setMemo(null);

        return MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO.builder()
                .memberSavedCorrectionId(entity.getId())
                .deletedAt(LocalDateTime.now())
                .message("메모가 성공적으로 삭제되었습니다.")
                .build();
    }

    private MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem toSavedCorrectionDTO(MemberSavedCorrection entity) {
        Correction correction = entity.getCorrection();
        Member member = correction.getAuthor();
        Diary diary = correction.getDiary();

        return MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.builder()
                .savedCorrectionId(entity.getId())
                .memo(entity.getMemo())
                .correction(MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.CorrectionInfo.builder()
                        .correctionId(correction.getId())
                        .originalText(correction.getOriginalText())
                        .corrected(correction.getCorrected())
                        .commentText(correction.getCommentText())
                        .correctionCreatedAt(DateFormatUtil.formatDate(correction.getCreatedAt()))
                        .build())
                .diary(MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.DiaryInfo.builder()
                        .diaryId(diary.getId())
                        .diaryTitle(diary.getTitle())
                        .diaryCreatedAt(DateFormatUtil.formatDate(diary.getCreatedAt()))
                        .build())
                .author(MemberSavedCorrectionResponseDTO.SavedListResponseDTO.SavedCorrectionItem.MemberInfo.builder()
                        .memberId(member.getId())
                        .userId(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImg())
                        .build())
                .build();
    }

    private MemberSavedCorrection getCorrectionOrThrow(Long id) {
        return memberSavedCorrectionRepository.findById(id)
                .orElseThrow(() -> new CorrectionHandler(CORRECTION_NOT_FOUND));
    }

    private void validateMemberAccess(MemberSavedCorrection entity, Long currentMemberId) {
        if (!entity.getMember().getId().equals(currentMemberId)) {
            throw new AuthHandler(NOT_RESOURCE_OWNER);
        }
    }
}
