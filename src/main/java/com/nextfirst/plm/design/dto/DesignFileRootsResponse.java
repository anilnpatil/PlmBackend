package com.nextfirst.plm.design.dto;

import java.util.List;

public record DesignFileRootsResponse(List<DesignFileRootResponse> roots, boolean canManage) {
}
