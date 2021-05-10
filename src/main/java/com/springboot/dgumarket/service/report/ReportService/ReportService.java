package com.springboot.dgumarket.service.report.ReportService;

import com.springboot.dgumarket.payload.request.report.ReportRequest;

public interface ReportService {

    void postReport(int id, ReportRequest reportRequest);
}
