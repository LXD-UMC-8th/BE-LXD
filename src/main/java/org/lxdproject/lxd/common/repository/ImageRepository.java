package org.lxdproject.lxd.common.repository;

import org.lxdproject.lxd.common.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
