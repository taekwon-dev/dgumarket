package com.springboot.dgumarket.repository.product;


import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;


/**
 * Created by TK YOUN (2020-12-22 오후 10:06)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface ProductRepository extends JpaRepository<Product, Integer>{

    // 인덱스 화면에서 전시될 상품 리스트 입니다.
    // 상품 리스트는 카테고리 별 최대 네 가지 항목을 조회합니다.
    List<Product> findTop4ByProductCategoryOrderByCreateDatetimeDesc(ProductCategory productCategory);

    // 상품 정보가 데이터베이스에 저장된 시점 이후, 해당 상품의 고유 ID를 반환 -> 해당 상품 상세 페이지로 이동시키기 위함
    Product findTopByMemberOrderByCreateDatetimeDesc(Member member);

    @Query("Select p from Product p Where p.id = :id And p.productStatus = 0")
    Product findByIdNotOptional(int id);

    Product findById(int id);



}
