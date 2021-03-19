package com.springboot.dgumarket.model.member;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import javax.persistence.*;
import java.time.LocalDateTime;


@Slf4j
@Entity
@Table(name = "user_block")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockUser {

    @Id
    @Column
    private int id;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Member user;


    @OneToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "blocked_user_id", referencedColumnName = "id")
    private Member blockedUser;

    @Column(name = "block_date")
    private LocalDateTime block_date;
}
