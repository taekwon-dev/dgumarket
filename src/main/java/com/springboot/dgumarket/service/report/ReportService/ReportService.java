package com.springboot.dgumarket.service.report.ReportService;

import com.springboot.dgumarket.payload.request.report.ReportRequest;
import org.springframework.stereotype.Service;

public interface ReportService {

    void postReport(int id, ReportRequest reportRequest);
}
