package com.springboot.dgumarket.service.block;

import com.springboot.dgumarket.dto.block.BlockStatusDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class UserBlockServiceImpl implements UserBlockService{

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductReviewRepository productReviewRepository;


    // 유저 차단하기
    @Override
    @Transactional
    public boolean blockUser(int userId, int blockUserId) {
        Member member = memberRepository.findById(userId);
        Member targetMember = memberRepository.findById(blockUserId);
        List<ProductReview> productReviewList = productReviewRepository.checkTradeHistory(member, targetMember);
        // 거래내역중에 상대방과 거래한 내역이 없을 경우 차단가능
        if (productReviewList.size() == 0){
            member.blockUser(targetMember);
            return true;
        }
        return false;
    }

    // 유저차단 해제하기
    @Override
    @Transactional
    public void unBlockUser(int userId, int unblockUserId) {
        Member member = memberRepository.findById(userId);
        Member targetMember = memberRepository.findById(unblockUserId);
        member.unblockUser(targetMember);
    }


    // 유저 차단 상태 조회하기
    @Override
    @Transactional
    public BlockStatusDto checkBlockStatus(int userId, int targetUserId) {
        Member member = memberRepository.findById(userId);
        Member targetMember = memberRepository.findById(targetUserId);

        return BlockStatusDto.builder().block_status(member.checkBlockStatus(targetMember)).build();
    }
}
