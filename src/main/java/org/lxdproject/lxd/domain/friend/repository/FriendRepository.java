package org.lxdproject.lxd.domain.friend.repository;

import org.lxdproject.lxd.domain.friend.entity.Friendship;
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
