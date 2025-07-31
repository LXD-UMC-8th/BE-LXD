package org.lxdproject.lxd.diary.dto;

import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.diary.entity.enums.RelationType;

@Getter
@Builder
public class MemberDiarySummaryResponseDTO {
    private String profileImg;
    private String username;
    private String nickname;
    private Long diaryCount;
    private Long friendCount;
    private RelationType relation;
}
