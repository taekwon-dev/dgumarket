package com.springboot.dgumarket.repository.product;

import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

/**
 * Created by TK YOUN (2020-12-22 오후 10:06)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    ProductCategory findById(int id);

    // 비로그인 상태 -> /api/product/index 요청 시 활용, 인기 카테고리 (category_type = 1)
    Page<ProductCategory> findAllByCategoryTypeAndIdGreaterThanOrderByIdAsc(int category_type, int last_id, Pageable pageable);


    // 로그인 상태 -> /api/product/index 요청 시 활용, 유저의 관심 카테고리
    // last_id : 최초 /api/product/index 요청 시 -> 0 (default)
    // 무한 스크롤링 감지 후 /api/product/index 요청 시 -> (Client side) 응답 받은 카테고리의 중 가장 아래에 위치하는 카테고리 고유 id
    Page<ProductCategory> findByMembersAndIdGreaterThanOrderByIdAsc(Member member, int last_id, Pageable pageable);

    // 회원가입 3단계 - 유저의 관심카테고리에 해당하는 카테고리 목록 한 번에 조회
    // for loop으로 하나씩 조회하는 과정에서 처리시간 문제
    Set<ProductCategory> findByIdIn(Iterable<Integer> ids);

    // https://stackoverflow.com/questions/35119544/java-spring-repositories-findby-method-using-set-of-ids-values
    // findAll(Iterable<ID> ids)



}