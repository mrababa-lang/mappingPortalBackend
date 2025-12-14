package com.slashdata.vehicleportal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkActionRequest {

    public enum Action {APPROVE, REJECT}

    @NotNull
    private Action action;

    @NotEmpty
    private List<Long> ids;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
