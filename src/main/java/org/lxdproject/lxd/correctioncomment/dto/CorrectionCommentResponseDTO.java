package org.lxdproject.lxd.correctioncomment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.common.dto.MemberProfileView;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrectionCommentResponseDTO {
    private Long commentId;
    private MemberProfileView memberProfileView;
    private String content;
    private String createdAt;
}
