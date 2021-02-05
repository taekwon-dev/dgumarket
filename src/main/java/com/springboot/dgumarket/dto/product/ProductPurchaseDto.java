package com.springboot.dgumarket.dto.product;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ProductPurchaseDto {
    int purchase_product_id;
    String purchase_title;
    String purchase_price;
    String purchase_product_img;
    boolean is_review;
    int purchase_like_num;
    int purchase_chat_num;
    LocalDateTime purchase_date;
    LocalDateTime purchase_review_date;
}
