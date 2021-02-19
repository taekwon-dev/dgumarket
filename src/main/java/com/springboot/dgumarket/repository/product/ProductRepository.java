package com.springboot.dgumarket.repository.product;

import com.drew.lang.annotations.Nullable;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.model.product.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by TK YOUN (2020-12-22 오후 10:06)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT * FROM (SELECT *, RANK() OVER (PARTITION BY category_id ORDER BY id ASC)rank_id FROM product WHERE category_id IN (:categories)) ranked WHERE rank_id <= 4", nativeQuery = true)
    List<Product> apiProductIndex(@Param("categories") List<ProductCategory> categories);


    // shop, 유저 전체 판매물건 조회
    @Query("select p from Product p where p.member =:member and p.productStatus = 0")
    List<Product> findAllByMember(Member member, @Nullable Pageable pageable);

    // shop, 유저 특정상태 판매물건 조회
    @Query("select p from Product p where p.member =:member and p.productStatus = 0 and p.transactionStatusId =:transactionStatusId")
    List<Product> findAllByMemberWithSort(Member member, int transactionStatusId, Pageable pageable);

    // 카테고리별 물건 가져오기 ( feat, 거래중 )
    @Query("select p from Product p where p.productCategory.id =:categoryId and p.transactionStatusId = 0")
    List<Product> getProductsByCategoryId (int categoryId, Pageable pageable);

    // 전체 물건 가져오기
    List<Product> getAllByTransactionStatusIdEquals(int transactionStatusId, Pageable pageable);
}