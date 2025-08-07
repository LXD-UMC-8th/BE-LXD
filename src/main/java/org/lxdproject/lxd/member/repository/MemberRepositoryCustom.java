package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.friend.dto.FriendSearchResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface MemberRepositoryCustom {
    Page<FriendSearchResponseDTO.MemberInfo> searchByQuery(String query, Long currentUserId, Set<Long> friendIds, Pageable pageable);
}
