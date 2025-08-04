package org.lxdproject.lxd.notification.repository;

import org.lxdproject.lxd.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
    @Query("""
    select n from Notification n
    join fetch n.sender
    join fetch n.receiver
    where n.id = :id
""")
    Optional<Notification> findWithSenderAndReceiverById(@Param("id") Long id);

}