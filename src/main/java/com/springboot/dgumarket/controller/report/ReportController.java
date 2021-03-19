package com.springboot.dgumarket.controller.report;

import com.springboot.dgumarket.payload.request.report.ReportRequest;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.report.ReportService.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/report")
    public ResponseEntity<?> postReport(@RequestBody ReportRequest reportRequest, Authentication authentication){
        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            reportService.postReport(userDetails.getId(), reportRequest);
            return new ResponseEntity<>("Reported successfully", HttpStatus.OK);
        }
        return null;
    }
}
