package org.lxdproject.lxd.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.diary.entity.enums.RelationType;
import org.lxdproject.lxd.member.entity.enums.FriendRequestStatus;

@Getter
@Builder
public class MemberDiarySummaryResponseDTO {
    private String profileImg;
    private String username;
    private String nickname;
    private Long diaryCount;
    private Integer friendCount;
    private RelationType relation;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FriendRequestStatus status;
}
