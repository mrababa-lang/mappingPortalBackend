package com.slashdata.vehicleportal.entity;

public enum Role {
    ADMIN,
    MAPPING_ADMIN,
    MAPPING_USER;

    public String toAuthority() {
        return "ROLE_" + name();
    }
}
