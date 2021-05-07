package com.springboot.dgumarket.model.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.springboot.dgumarket.model.member.Member;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by TK YOUN (2020-12-22 오후 10:05)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Builder
@AllArgsConstructor
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "product_category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_type")
    private int categoryType;

    /**
     *  - 양방향 관계, 종속 관계 (mappedBy 명시, 조인 컬럼 Product 테이블에서 관리)
     *  - ProductCategory -> Product 방향으로 객체 탐색
     *      - 예) [카테고리 별 상품 조회] 기능
     *  - ProductCategory : Product = One to Many
     *  - 상품 정보가 삭제했을 때 영향 받지 않는다. (= 상품 카테고리는 시스템 로직으로 인해서 삭제되는 대상이 아님)
     *  - 상품 카테고리 테이블은 시스템 로직으로 인한 자동적 데이터 입출력이 없으므로 영속성 전이 옵션을 사용할 필요 없다고 판단
     *  - Fetch 옵션은 @OneToMany 디폴트 옵션인 LAZY 사용
     * */
    @OneToMany(mappedBy = "productCategory")
    @JsonIgnore
    private Set<Product> products;

    @ManyToMany(mappedBy = "productCategories")
    private Set<Member> members = new HashSet<>();

}
