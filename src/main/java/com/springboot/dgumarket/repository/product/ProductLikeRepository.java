package com.springboot.dgumarket.repository.product;

import com.drew.lang.annotations.Nullable;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.ProductLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Integer> {

    // 관심목록 조회하기
    List<ProductLike> findAllByMember(Member member, @Nullable Pageable pageable);
}
