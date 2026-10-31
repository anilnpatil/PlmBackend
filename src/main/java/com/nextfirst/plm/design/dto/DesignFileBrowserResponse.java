package com.nextfirst.plm.design.dto;

import java.util.List;

public record DesignFileBrowserResponse(
        List<DesignFileBrowserRoot> roots,
        Long selectedRootId,
        String currentDirectory,
        String parentDirectory,
        List<DesignFileBrowserEntry> entries) {
}
