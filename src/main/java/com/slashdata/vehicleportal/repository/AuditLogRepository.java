package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.AuditAction;
import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, String>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityIdAndEntityTypeOrderByTimestampAsc(String entityId, AuditEntityType entityType);

    Optional<AuditLog> findTopByEntityTypeAndEntityIdAndActionInAndTimestampLessThanEqualOrderByTimestampDesc(
        AuditEntityType entityType,
        String entityId,
        List<AuditAction> actions,
        LocalDateTime timestamp);
}
