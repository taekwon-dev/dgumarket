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

    @OneToMany(mappedBy = "productCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Product> products;

    @ManyToMany(mappedBy = "productCategories")
    private Set<Member> members = new HashSet<>();

}
