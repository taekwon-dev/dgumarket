package com.springboot.dgumarket.model.member;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


@Slf4j
@Entity
@Table(name = "user_block")
@NoArgsConstructor
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Member user;


    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "blocked_user_id", referencedColumnName = "id")
    private Member blockedUser;

    @CreationTimestamp
    @Column(name = "block_date", updatable = false)
    private LocalDateTime block_date;

    @Builder
    public BlockUser(Member user, Member blockedUser) {
        this.user = user;
        this.blockedUser = blockedUser;
    }

    public void setBlocker(Member member) {
        this.user = member;
    }

    public void setBlockedUser(Member member) {
        this.blockedUser = member;
    }

}
