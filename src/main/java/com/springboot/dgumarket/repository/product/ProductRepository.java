package com.springboot.dgumarket.repository.product;

import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}