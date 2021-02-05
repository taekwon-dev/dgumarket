package com.springboot.dgumarket.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ProductReviewDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int review_user_id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String review_user_icon;
    private String review_nickname;
    private String review_comment;
    private LocalDateTime review_date;
}
