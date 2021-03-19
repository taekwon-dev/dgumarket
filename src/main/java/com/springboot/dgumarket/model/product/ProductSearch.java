package com.springboot.dgumarket.model.product;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import static com.springboot.dgumarket.utils.ProductSpec.withCategory;
import static org.springframework.data.jpa.domain.Specification.where;
import static com.springboot.dgumarket.utils.ProductSpec.titleLike;
import static com.springboot.dgumarket.utils.ProductSpec.notDeleted;
import static com.springboot.dgumarket.utils.ProductSpec.productStatusEq;
import static com.springboot.dgumarket.utils.ProductSpec.onlyUser;
import static com.springboot.dgumarket.utils.ProductSpec.withCategory;

@Getter
@Setter
public class ProductSearch {
    private String productName;
    private ProductCategory productCategory;
    private int productTransactionId;

    public Specification<Product> toSpecification(){
        return where(titleLike(productName)) // 검색쿼리
                .and(notDeleted()) // 물건상태(삭제X)
                .and(onlyUser()) // 가입한유저
                .and(productStatusEq(productTransactionId)) // 물건거래상태-판매중만
                .and(withCategory(productCategory)); // 카테고리
    }
}
