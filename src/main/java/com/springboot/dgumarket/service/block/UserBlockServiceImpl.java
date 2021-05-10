package com.springboot.dgumarket.service.block;

import com.springboot.dgumarket.dto.block.BlockStatusDto;
import com.springboot.dgumarket.dto.block.BlockUserDto;
import com.springboot.dgumarket.dto.block.BlockUserListDto;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberQueryRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserBlockServiceImpl implements UserBlockService{

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    // 유저 차단하기
    @Override
    @Transactional
    public boolean blockUser(int userId, int blockUserId) {

        // 로그인 유저 (= '차단' 주체)
        Member member = memberRepository.findById(userId);

        // 타겟 유저 (= '차단' 대상)
        Member targetMember = memberRepository.findById(blockUserId);

        // 유저가 차단한 유저 목록 테이블 저장(추가)을 위해
        // 해당 객체 생성
        BlockUser blockUser = BlockUser.builder()
                .user(member) // 로그인 유저
                .blockedUser(targetMember) // 타겟 유저
                .build();

        List<ProductReview> productReviewList = productReviewRepository.checkTradeHistory(member, targetMember);
        // 거래내역중에 상대방과 거래한 내역이 없을 경우 차단가능
        if (productReviewList.size() == 0) {
            // 영속성 전이 - PERSIST를 통해서 INSERT문 생성
            member.blockUser(blockUser, member, targetMember);
            return true;
        }
        return false;
    }

    // 유저차단 해제하기
    @Override
    @Transactional
    public void unBlockUser(int userId, int unblockUserId) {

        // 타겟 유저 (= '차단해제' 주제)
        Member member = memberRepository.findById(userId);

        // 타겟 유저 (= '차단해제' 대상)
        Member targetMember = memberRepository.findById(unblockUserId);

        // 유저가 차단한 유저 목록 테이블에서 해당 테이블 삭제하기 위해
        // 해당 객체 생성
        BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(member, targetMember);

        // 두 엔티티 관계를 끊는다.
        // for 영속성 관리
        member.unblockUser(blockUser);

        // 삭제 쿼리
        blockUserRepository.delete(blockUser);
    }


    // 유저 차단 상태 조회하기
    @Override
    @Transactional
    public BlockStatusDto checkBlockStatus(int userId, int targetUserId) {

        // 로그인 유저
        Member loginUser = memberRepository.findById(userId);

        // 차단 여부 조회 대상 유저
        Member targetMember = memberRepository.findById(targetUserId);

        // loginUser.getBlockUsers() : where user_id = loginUser.id
        // (= 로그인한 유저가 차단한 유저 리스트)
        BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, targetMember);

        // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
        // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
        BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(targetMember, loginUser);

        return BlockStatusDto.builder()
                .block_status(loginUser.checkBlockStatus(blockUser, blockedUser))
                .build();
    }

    // 유저 차단 리스트 조회하기
    @Override
    public BlockUserListDto getUserBlockList(int userId, Pageable pageable) {
        //given
        Member member = memberRepository.findById(userId);

        // mapping config
        PropertyMap<com.springboot.dgumarket.model.member.BlockUser, BlockUserDto> propertyMap = new PropertyMap<com.springboot.dgumarket.model.member.BlockUser, BlockUserDto>() {
            @Override
            protected void configure() {
                map().setId(source.getBlockedUser().getId());
                map().setNickName(source.getBlockedUser().getNickName());
                map().setProfileImageDir(source.getBlockedUser().getProfileImageDir());
                map().setIsBlock(1);
            }
        };
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(propertyMap);

        // fetch data
        PageImpl<BlockUser> blockUsers = memberQueryRepository.findBlockUserByMember(member, pageable);


        // entity -> dto
        List<BlockUserDto> blockUserDtos = blockUsers.getContent()
                .stream().map(mem -> modelMapper.map(mem, BlockUserDto.class)).collect(Collectors.toList());

        return BlockUserListDto.builder()
                .total_size((int)blockUsers.getTotalElements())
                .blockUserDtoList(blockUserDtos)
                .build();
    }
}
