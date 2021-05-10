package com.springboot.dgumarket.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.springboot.dgumarket.model.member.QBlockUser.blockUser;

@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {
    @PersistenceContext
    private EntityManager em;

    public PageImpl<BlockUser> findBlockUserByMember(Member member, Pageable pageable){
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        List<BlockUser> blockUserList = queryFactory.selectFrom(blockUser)
                .where(blockUser.user.eq(member)
                        .and(blockUser.blockedUser.isEnabled.eq(0)
                                .and(blockUser.blockedUser.isWithdrawn.eq(0)))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

        long totalCount = queryFactory.selectFrom(blockUser)
                .where(blockUser.user.eq(member)
                        .and(blockUser.blockedUser.isEnabled.eq(0)
                                .and(blockUser.blockedUser.isWithdrawn.eq(0)))
                ).fetchCount();

        return new PageImpl<>(blockUserList, pageable, totalCount);
    }
}
