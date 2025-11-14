package org.lxdproject.lxd.domain.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FriendRequestCreateRequestDTO {

    @NotNull
    private Long receiverId;
}

