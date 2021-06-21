package com.springboot.dgumarket.service.Validation;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.request.chat.ValidationRequest;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ValidationServiceImpl implements ValidationService {

    private static final int PRODUCT_SALE = 0; // 물건판매중
    private static final int PRODUCT_REMOVE = 1; // 물건삭제
    private static final int PRODUCT_BLIND = 2; // 물건블라인드

    private static final int USER_NOT_WITHDRAWN = 0;
    private static final int USER_WITHDRAWN = 1; // 유저 탈퇴
    private static final int USER_NOT_ENABLED = 1; // 유저 제재중
    private static final int USER_ENABLED = 0; // 이용가능

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Override
    public String checkValidateForChatroom(int userId, ValidationRequest validationRequest) throws CustomControllerExecption {
        Member member = memberRepository.findById(userId);
        //탈퇴한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        //차단한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        //차단 당한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        //관리자에 의해 이용제재를 받고 있는 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        //해당 중고물품은 관리자에 의해 비공개 처리되었습니다. ( 상품 )
        //해당 중고물품은 판매자에 의해 삭제되었습니다. ( 상품 )

        // 물건이미지클릭시
        if (validationRequest.getProduct_id().isPresent()) {
            int productId = validationRequest.getProduct_id().get();

            Product product = productRepository.findById(productId);
            if( product == null || product.getProductStatus() == PRODUCT_REMOVE){
                throw new CustomControllerExecption("해당 중고물품은 판매자에 의해 삭제되었습니다.", HttpStatus.BAD_REQUEST, null, 100);
            }

            if (product.getProductStatus() == PRODUCT_BLIND) { throw new CustomControllerExecption("해당 중고물품은 관리자에 의해 비공개 처리되었습니다.", HttpStatus.BAD_REQUEST, null, 101); }
                // 해당물건의 유저의 유효성체크
            return isValidateUser(member, product.getMember());
        }

        // 유저프로필클릭시
        if (validationRequest.getUser_id().isPresent()) {

            Member targetUser = memberRepository.findById(validationRequest.getUser_id().get().intValue());
            if(targetUser == null || targetUser.getIsWithdrawn()==1){
                throw new CustomControllerExecption("탈퇴한 유저의 정보에 접근할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
            }
            return isValidateUser(member, targetUser);
        }
        return null;
    }




    public String isValidateUser(Member loginUser, Member targetUser) throws CustomControllerExecption {
        //탈퇴한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        //차단한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        // 차단 당한 유저의 정보에 접근할 수 없습니다.
        // 관리자에 의해 이용제재를 받고 있는 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )

        // 유저제재확인, 유저탈퇴확인, 차단까지 확인
        if (targetUser == null || targetUser.getIsWithdrawn() == 1) {
            throw new CustomControllerExecption("탈퇴한 유저의 정보에 접근할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
        }

        if (targetUser.getIsEnabled() == 1) {
            throw new CustomControllerExecption("관리자에 의해 이용제재를 받고 있는 유저의 정보에 접근할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);
        }

        // 탈퇴 유저 && 서비스 이용 제재 받은 유저가 아닌 경우
        if (targetUser.getIsWithdrawn() == 0 && targetUser.getIsEnabled() == 0){

            // loginUser.getBlockUsers() : where user_id = loginUser.id
            // (= 로그인한 유저가 차단한 유저 리스트)
            BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, targetUser);

            // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
            // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
            BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(targetUser, loginUser);


            if (loginUser.getBlockUsers().contains(blockUser)) {
                throw new CustomControllerExecption("차단한 유저의 정보에 접근할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
            }
            if (loginUser.getUserBlockedMe().contains(blockedUser)) {
                throw new CustomControllerExecption("나를 차단한 유저의 정보에 접근할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
            }
        }

        return null;
    }
}
