package org.lxdproject.lxd.domain.friend.dto;

import lombok.AccessLevel;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequestAcceptRequestDTO {

    @NotNull
    private Long requesterId;
}