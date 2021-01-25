package com.springboot.dgumarket.model.product;


import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;


/**
 * Created by MS KIM (2020-01-15 )
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description : 상품 거래완료시 구매리뷰에 대한 엔티티이다.
 */

@Slf4j
@Entity
@Table(name = "product_review")
@NoArgsConstructor
@Getter
@Setter
@Builder
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "writer_id", referencedColumnName = "id")
    private Member consumer;


    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    private Member seller;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Product.class)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "review_message", nullable = true, columnDefinition = "TEXT")
    private String reviewMessage;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = ChatRoom.class)
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private ChatRoom chatRoom;

    // 수정 시간
    @Column(name = "review_datetime")
    private LocalDateTime ReviewRegistrationDate;
}
