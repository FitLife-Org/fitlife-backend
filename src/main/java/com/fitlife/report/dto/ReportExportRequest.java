package com.fitlife.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReportExportRequest {
    private String reportType;
    private LocalDate fromDate;
    private LocalDate toDate;
}
