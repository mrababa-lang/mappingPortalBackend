package com.slashdata.vehicleportal.specification;

import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.MappingStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ADPMappingSpecifications {

    private ADPMappingSpecifications() {
    }

    public static Specification<ADPMapping> reviewStatus(String reviewStatus) {
        return (root, cq, cb) -> {
            var mappedStatusPredicate = cb.equal(root.get("status"), MappingStatus.MAPPED);
            if (reviewStatus == null) {
                return mappedStatusPredicate;
            }
            return switch (reviewStatus.toLowerCase()) {
                case "pending" -> cb.and(mappedStatusPredicate, cb.isNull(root.get("reviewedAt")));
                case "reviewed" -> cb.and(mappedStatusPredicate, cb.isNotNull(root.get("reviewedAt")));
                default -> mappedStatusPredicate;
            };
        };
    }

    public static Specification<ADPMapping> mappingType(MappingStatus status) {
        return (root, cq, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<ADPMapping> updatedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, cq, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("updatedAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("updatedAt"), from);
            }
            return cb.lessThanOrEqualTo(root.get("updatedAt"), to);
        };
    }
}
