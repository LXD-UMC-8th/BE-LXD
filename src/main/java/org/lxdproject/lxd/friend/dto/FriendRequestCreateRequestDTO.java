package org.lxdproject.lxd.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FriendRequestCreateRequestDTO {

    @NotNull
    private Long receiverId;
}

