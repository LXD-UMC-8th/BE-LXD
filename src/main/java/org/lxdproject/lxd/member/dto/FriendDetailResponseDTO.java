package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.diary.dto.MyDiarySummaryResponseDTO;

import java.util.List;

@Getter
@AllArgsConstructor
public class FriendDetailResponseDTO {
    private String username;
    private String nickname;
    private List<MyDiarySummaryResponseDTO> diaries;
}
