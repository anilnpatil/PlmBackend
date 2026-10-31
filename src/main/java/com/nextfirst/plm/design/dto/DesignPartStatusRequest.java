package com.nextfirst.plm.design.dto;

import com.nextfirst.plm.design.entity.PartStatus;
import jakarta.validation.constraints.NotNull;

public record DesignPartStatusRequest(@NotNull PartStatus partStatus) {
}
