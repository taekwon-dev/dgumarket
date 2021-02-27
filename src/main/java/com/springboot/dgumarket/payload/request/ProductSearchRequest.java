package com.springboot.dgumarket.payload.request;

import lombok.Getter;

@Getter
public class ProductSearchRequest {
    private String q;
    private int category_id;
}
