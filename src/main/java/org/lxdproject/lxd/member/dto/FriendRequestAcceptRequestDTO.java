package org.lxdproject.lxd.member.dto;

import lombok.Getter;
import jakarta.validation.constraints.NotNull;

@Getter
public class FriendRequestAcceptRequestDTO {

    @NotNull
    private Long requesterId;
}