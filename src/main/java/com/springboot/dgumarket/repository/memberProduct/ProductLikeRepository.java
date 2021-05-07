package com.springboot.dgumarket.repository.memberProduct;

import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductLikeRepository extends JpaRepository<ProductLike, Integer> {

    ProductLike findByMemberAndProduct(Member member, Product product);

}
