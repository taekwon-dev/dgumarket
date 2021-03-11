package com.springboot.dgumarket.utils;

import com.drew.tools.ProcessUrlUtility;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.model.product.ProductLike;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import javax.swing.plaf.SeparatorUI;

 /**
 *  물건 명세(JPA)
 */
public class ProductSpec {


    // 타이틀 검색
    public static Specification<Product> titleLike(final String productName){
        return (Specification<Product>) (root, query, criteriaBuilder) -> {
            if (StringUtils.isEmpty(productName)) return null;
            return criteriaBuilder.like(root.get("title"), "%" + productName + "%");
        };
    }

    // 카테고리 검색
    public static Specification<Product> withCategory(ProductCategory category){
        return new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if ( category == null) return null;
                return criteriaBuilder.equal(root.get("productCategory"), category);
            }
        };
    }

    // 물건거래 상태
    public static Specification<Product> productStatusEq(int productStatus){
        return (Specification<Product>) (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("transactionStatusId"), productStatus);
    }

    // 삭제되지 않은 물건들만
    public static Specification<Product> notDeleted(){
        return (Specification<Product>) (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("productStatus"), 0);
    }

    // 오직 가입한 유저만
    public static Specification<Product> onlyUser(){
        return (Specification<Product>) (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member").get("isWithdrawn"), 0);
    }

    // 차단확인
    public static Specification<Product> onlyAllowedByUser(Member member){
        return new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

//                if (!member.getLikeProducts().contains(root.getModel()) || !(member.getUserBlockedMe().contains(root.getModel()))){


//                }


//                return criteriaBuilder.isNotMember(root.getModel(), member.getLikeProducts());
                return null;
            };
        };
    }
}
