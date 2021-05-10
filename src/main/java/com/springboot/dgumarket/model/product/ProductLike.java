package com.springboot.dgumarket.model.product;


import com.springboot.dgumarket.model.member.Member;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Table(name = "product_like")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @CreationTimestamp
    @Column(name = "created", updatable = false)
    private LocalDateTime likedTime;

    @Builder
    public ProductLike(Member member, Product product) {
        this.member = member;
        this.product = product;
    }
}
