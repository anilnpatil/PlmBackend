package com.nextfirst.plm.assembly.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextfirst.plm.assembly.entity.AssemblyStatus;
import com.nextfirst.plm.design.entity.ModeOfPurchase;

public record AssemblyComponentResponse(
        Long designPartId,
        Long mainAssemblyId,
        String mainAssemblyNumber,
        String mainAssemblyName,
        Long subAssemblyId,
        String subAssemblyNumber,
        String subAssemblyName,
        String partNumber,
        String partName,
        BigDecimal quantity,
        ModeOfPurchase modeOfPurchase,
        LocalDate assemblyStartDate,
        LocalDate assemblyCompleteDate,
        AssemblyStatus assemblyStatus) {
    public AssemblyComponentResponse {
        if (assemblyStatus == null) {
            assemblyStatus = AssemblyStatus.NOT_STARTED;
        }
    }
}
