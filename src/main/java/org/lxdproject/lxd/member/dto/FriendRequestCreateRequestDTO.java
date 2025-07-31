package org.lxdproject.lxd.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FriendRequestCreateRequestDTO {

    @NotNull
    private Long receiverId;
}

