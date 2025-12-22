package com.slashdata.vehicleportal.specification;

import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditLog;
import com.slashdata.vehicleportal.entity.AuditSource;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> hasUserId(Long userId) {
        return (root, query, builder) -> userId == null
            ? builder.conjunction()
            : builder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<AuditLog> hasSource(AuditSource source) {
        return (root, query, builder) -> source == null
            ? builder.conjunction()
            : builder.equal(root.get("source"), source);
    }

    public static Specification<AuditLog> withinDateRange(LocalDateTime from, LocalDateTime to) {
        return (root, query, builder) -> {
            if (from == null && to == null) {
                return builder.conjunction();
            }
            if (from != null && to != null) {
                return builder.between(root.get("timestamp"), from, to);
            }
            if (from != null) {
                return builder.greaterThanOrEqualTo(root.get("timestamp"), from);
            }
            return builder.lessThanOrEqualTo(root.get("timestamp"), to);
        };
    }

    public static Specification<AuditLog> hasEntityType(AuditEntityType entityType) {
        return (root, query, builder) -> entityType == null
            ? builder.conjunction()
            : builder.equal(root.get("entityType"), entityType);
    }
}
