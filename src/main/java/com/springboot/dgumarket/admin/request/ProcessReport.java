package com.springboot.dgumarket.admin.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProcessReport {
    int process_type; // 0: none,  1: 경고, 2: 유저제재, 3: 물건 블라인드처리,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String public_content; // 신고자에게 전달하는 내용
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String target_content; // 신고당하는 사람에게 전달하는 내용
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String admin_content; // 관리자가 관리하는 내용
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    int blind_product_id; // 블라인드 물건아이디
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    int is_cancel; // 취소유무
}
