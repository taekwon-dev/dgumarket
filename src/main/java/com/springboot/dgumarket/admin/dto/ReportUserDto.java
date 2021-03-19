package com.springboot.dgumarket.admin.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ReportUserDto {
    int user_id;
    String user_nickname;
}
