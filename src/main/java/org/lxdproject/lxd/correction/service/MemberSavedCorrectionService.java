package org.lxdproject.lxd.correction.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.MemberSavedCorrectionResponseDTO;
import org.lxdproject.lxd.correction.entity.mapping.MemberSavedCorrection;
import org.lxdproject.lxd.correction.repository.MemberSavedCorrectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.lxdproject.lxd.apiPayload.code.status.ErrorStatus.CORRECTION_NOT_FOUND;
import static org.lxdproject.lxd.apiPayload.code.status.ErrorStatus.INVALID_CORRECTION_MEMO;

@Service
@RequiredArgsConstructor
public class MemberSavedCorrectionService {

    private final MemberSavedCorrectionRepository memberSavedCorrectionRepository;

    @Transactional
    public MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO createMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        MemberSavedCorrection entity = getCorrectionOrThrow(request.getCorrectionId());

        if (entity.getMemo() != null && !entity.getMemo().isBlank()) {
            throw new CorrectionHandler(INVALID_CORRECTION_MEMO);
        }

        entity.setMemo(request.getMemo());
        return MemberSavedCorrectionResponseDTO.CreateMemoResponseDTO.builder()
                .correctionId(entity.getId())
                .createdAt(entity.getUpdatedAt())
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO updateMemo(MemberSavedCorrectionRequestDTO.MemoRequestDTO request) {
        MemberSavedCorrection entity = getCorrectionOrThrow(request.getCorrectionId());

        entity.setMemo(request.getMemo());
        return MemberSavedCorrectionResponseDTO.UpdateMemoResponseDTO.builder()
                .updatedMemo(request.getMemo())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Transactional
    public MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO deleteMemo(Long correctionId) {
        MemberSavedCorrection entity = getCorrectionOrThrow(correctionId);
        entity.setMemo(null);

        return MemberSavedCorrectionResponseDTO.DeleteMemoResponseDTO.builder()
                .correctionId(entity.getId())
                .deletedAt(entity.getUpdatedAt())
                .message("메모가 성공적으로 삭제되었습니다.")
                .build();
    }

    private MemberSavedCorrection getCorrectionOrThrow(Long id) {
        return memberSavedCorrectionRepository.findById(id)
                .orElseThrow(() -> new CorrectionHandler(CORRECTION_NOT_FOUND));
    }
}
