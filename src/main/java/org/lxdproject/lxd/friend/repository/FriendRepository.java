package org.lxdproject.lxd.friend.repository;

import org.lxdproject.lxd.friend.entity.FriendRequest;
import org.lxdproject.lxd.friend.entity.Friendship;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface FriendRepository extends JpaRepository<Friendship, Long>, FriendRepositoryCustom {

    @Query("""
    SELECT CASE 
        WHEN f.requester.id = :id THEN f.receiver.id 
        ELSE f.requester.id 
    END
    FROM Friendship f
    WHERE f.requester.id = :id OR f.receiver.id = :id
""")
    Set<Long> findFriendIdsByMemberId(@Param("id") Long memberId);

}
