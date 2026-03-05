package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.models.audit.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditLogEntity, UUID> {
}
