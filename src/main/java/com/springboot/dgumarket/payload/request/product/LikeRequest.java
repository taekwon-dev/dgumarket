package com.springboot.dgumarket.payload.request.product;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Getter
public class LikeRequest {
    private int product_id;

    @NotNull(message = "must not be 'Null' in current_like_status field")
    @Pattern(regexp = "(like|nolike)", message = "Invalid request value in current_like_status field, must be 'like'|'nolike'")
    private String current_like_status;
}
