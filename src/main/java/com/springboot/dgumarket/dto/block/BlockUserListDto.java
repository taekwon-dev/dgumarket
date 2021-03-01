package com.springboot.dgumarket.dto.block;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class BlockUserListDto {
    private int total_size; // 전체 상품 개수(목록별, 전체|판매중|판매완료)
    private List<BlockUserDto> blockUserDtoList;
}
