package com.nextfirst.plm.project.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ProjectSearchRequest {

    private String projectNumber;
    private String projectName;
    private String customerName;
    private LocalDate targetStartDateFrom;
    private LocalDate targetStartDateTo;
    private LocalDate targetEndDateFrom;
    private LocalDate targetEndDateTo;

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getTargetStartDateFrom() {
        return targetStartDateFrom;
    }

    public void setTargetStartDateFrom(LocalDate targetStartDateFrom) {
        this.targetStartDateFrom = targetStartDateFrom;
    }

    public LocalDate getTargetStartDateTo() {
        return targetStartDateTo;
    }

    public void setTargetStartDateTo(LocalDate targetStartDateTo) {
        this.targetStartDateTo = targetStartDateTo;
    }

    public LocalDate getTargetEndDateFrom() {
        return targetEndDateFrom;
    }

    public void setTargetEndDateFrom(LocalDate targetEndDateFrom) {
        this.targetEndDateFrom = targetEndDateFrom;
    }

    public LocalDate getTargetEndDateTo() {
        return targetEndDateTo;
    }

    public void setTargetEndDateTo(LocalDate targetEndDateTo) {
        this.targetEndDateTo = targetEndDateTo;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
