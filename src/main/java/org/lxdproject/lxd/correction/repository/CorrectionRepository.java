package org.lxdproject.lxd.correction.repository;

import org.lxdproject.lxd.correction.entity.Correction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
}