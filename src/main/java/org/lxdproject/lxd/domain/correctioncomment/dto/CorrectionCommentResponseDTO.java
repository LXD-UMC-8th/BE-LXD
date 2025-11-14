package org.lxdproject.lxd.domain.correctioncomment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrectionCommentResponseDTO {
    private Long commentId;
    private MemberProfileDTO memberProfile;
    private String content;
    private String createdAt;
}
