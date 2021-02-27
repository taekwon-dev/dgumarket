package com.springboot.dgumarket.repository.product;

import com.drew.lang.annotations.Nullable;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.ProductLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Integer> {


    // 관심목록 조회하기
    @Query("select p from ProductLike p " +
            "where p.member=:member " +
            "and p.product.productStatus=0 " +
            "and p.product.member.isWithdrawn=0 and p.product.member not in (:blockUsers) and p.product.member not in (:userBlocked)")
    List<ProductLike> findAllByMember(Member member, Set<Member> blockUsers, Set<Member> userBlocked, @Nullable Pageable pageable);

}
