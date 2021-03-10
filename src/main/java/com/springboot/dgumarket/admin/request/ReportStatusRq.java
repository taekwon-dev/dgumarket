package com.springboot.dgumarket.admin.request;

import lombok.Getter;

@Getter
public class ReportStatusRq {
    int current_status; // 0 : 신고막접수상태, 1 : 신고 접수 상태, 2 : 신고처리완료상태
}
