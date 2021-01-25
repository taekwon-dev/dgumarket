package com.springboot.dgumarket.dto.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProductReviewDto {
    String review_nickname;
    String review_comment;
    LocalDateTime review_date;
}
