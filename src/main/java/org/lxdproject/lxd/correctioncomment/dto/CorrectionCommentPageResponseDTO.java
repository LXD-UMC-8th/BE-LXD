package org.lxdproject.lxd.correctioncomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CorrectionCommentPageResponseDTO {
    private List<CorrectionCommentResponseDTO> replies;
    private long totalElements;
}

