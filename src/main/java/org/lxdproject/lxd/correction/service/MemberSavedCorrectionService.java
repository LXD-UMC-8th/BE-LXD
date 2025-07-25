package org.lxdproject.lxd.correction.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionResponseDTO;
import org.lxdproject.lxd.correction.entity.mapping.MemberSavedCorrection;
import org.lxdproject.lxd.correction.repository.MemberSavedCorrectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.lxdproject.lxd.apiPayload.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class MemberSavedCorrectionService {

    private final MemberSavedCorrectionRepository memberSavedCorrectionRepository;

    @Transactional
    public MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO createMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        MemberSavedCorrection entity = getCorrectionOrThrow(request.getMemberSavedCorrectionId());

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
        MemberSavedCorrection entity = getCorrectionOrThrow(request.getMemberSavedCorrectionId());

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
        MemberSavedCorrection entity = getCorrectionOrThrow(memberSavedCorrectionId);
        entity.setMemo(null);

        return MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO.builder()
                .memberSavedCorrectionId(entity.getId())
                .deletedAt(LocalDateTime.now())
                .message("메모가 성공적으로 삭제되었습니다.")
                .build();
    }

    private MemberSavedCorrection getCorrectionOrThrow(Long id) {
        return memberSavedCorrectionRepository.findById(id)
                .orElseThrow(() -> new CorrectionHandler(CORRECTION_NOT_FOUND));
    }
}
