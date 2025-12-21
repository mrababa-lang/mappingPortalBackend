package com.slashdata.vehicleportal.specification;

import com.slashdata.vehicleportal.entity.ADPMaster;
import org.springframework.data.jpa.domain.Specification;

public final class ADPMasterSpecifications {

    private ADPMasterSpecifications() {
    }

    public static Specification<ADPMaster> textSearch(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + query.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("adpMakeId")), like),
                cb.like(cb.lower(root.get("adpModelId")), like),
                cb.like(cb.lower(root.get("makeEnDesc")), like),
                cb.like(cb.lower(root.get("modelEnDesc")), like)
            );
        };
    }
}
