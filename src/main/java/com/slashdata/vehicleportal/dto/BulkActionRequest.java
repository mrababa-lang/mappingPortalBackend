package com.slashdata.vehicleportal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkActionRequest {

    public enum Action {APPROVE, REJECT}

    @NotNull
    private Action action;

    @NotEmpty
    private List<String> ids;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
