package com.springboot.dgumarket.repository.member;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;

import com.springboot.dgumarket.dto.member.FindPwdDto;
import com.springboot.dgumarket.model.member.Member;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

// 특정 필드만 조회하고 싶은 경우 어떻게 개선할 수 있을까?

import static com.springboot.dgumarket.model.member.QMember.member;


@Slf4j
@Repository
public class QMemberRepositoryImpl extends QuerydslRepositorySupport implements QMemberRepository {

    public QMemberRepositoryImpl() {
        super(Member.class); // domain class
    }

    @Override
    public FindPwdDto findByWebMailForFindPwd(String webMail) {

        JPQLQuery query = from(member);

        // 비밀번호 재설정(찾기) API 대상자 중, 이용제재 또는 회원탈퇴 유저는 제외
        query
             .select(Projections.constructor(FindPwdDto.class, member.webMail, member.phoneNumber))
             .where(member.webMail.eq(webMail)
             .and(member.isEnabled.eq(0))     // 이용 제재 유저가 아닌 대상
             .and(member.isWithdrawn.eq(0))); // 회원탈퇴 유저가 아닌 대상

        // T fetchOne() throws NonUniqueResultException;
        // select member1.webMail, member1.phoneNumber from Member member1 where member1.webMail = ?1 and member1.isEnabled = ?2 and member1.isWithdrawn = ?2
        try {
            FindPwdDto findPwdDto = (FindPwdDto) query.fetchOne();
            return findPwdDto;
        } catch (NonUniqueResultException e) {
            // [발생할 수 없는 예외]
        }
        return null;
    }
}
