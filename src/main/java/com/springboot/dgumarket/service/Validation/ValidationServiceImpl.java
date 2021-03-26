package com.springboot.dgumarket.service.Validation;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.request.chat.ValidationRequest;
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
    ProductRepository productRepository;

    @Autowired
    MemberRepository memberRepository;

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
            log.info("product Id : {}", productId);

            Optional<Product> product = productRepository.findById(productId);
            product.orElseThrow(() -> new CustomControllerExecption("해당 중고물품은 판매자에 의해 삭제되었습니다.", HttpStatus.NOT_FOUND));
            log.info("product.get().getProductStatus() : {}", product.get().getProductStatus());
            if (product.get().getProductStatus() == PRODUCT_REMOVE) { throw new CustomControllerExecption("해당 중고물품은 판매자에 의해 삭제되었습니다.", HttpStatus.NOT_FOUND); }
            if (product.get().getProductStatus() == PRODUCT_BLIND) { throw new CustomControllerExecption("해당 중고물품은 관리자에 의해 비공개 처리되었습니다.", HttpStatus.NOT_FOUND); }
                // 해당물건의 유저의 유효성체크
            return isValidateUser(member, product.get().getMember());
        }

        // 유저프로필클릭시
        if (validationRequest.getUser_id().isPresent()) {
            System.out.println("유저프로필클릭 : " + validationRequest.getUser_id().get());
            Member targetUser = memberRepository.findById(validationRequest.getUser_id().get().intValue());
            if(targetUser == null || targetUser.getIsWithdrawn()==1){
                throw new CustomControllerExecption("탈퇴한 유저의 정보에 접근할 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            return isValidateUser(member, targetUser);
        }
        return null;
    }




    public String isValidateUser(Member member, Member targetUser) throws CustomControllerExecption {
        //탈퇴한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        //차단한 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )
        // 차단 당한 유저의 정보에 접근할 수 없습니다.
        // 관리자에 의해 이용제재를 받고 있는 유저의 정보에 접근할 수 없습니다. ( 프로필 / 상품 )

        // 유저제재확인, 유저탈퇴확인, 차단까지 확인
        if (targetUser == null || targetUser.getIsWithdrawn() == 1) {
            throw new CustomControllerExecption("탈퇴한 유저의 정보에 접근할 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if (targetUser.getIsEnabled() == 1) {
            throw new CustomControllerExecption("관리자에 의해 이용제재를 받고 있는 유저의 정보에 접근할 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if (targetUser.getIsWithdrawn() == 0 && targetUser.getIsEnabled() == 0){ // 유저제재X, 탈퇴X
            log.info("target :  {} ", targetUser.getIsWithdrawn());
            log.info("memberId : {} , target :  {} ", member.getId(), targetUser.getId());
            if(member.getBlockUsers().contains(targetUser)){
                System.out.println(member.getId() + " 번 유저가 " + targetUser.getId() + " 에게 메시지를 보냄 1 ");
                log.info("상대방을 차단했니? {}", member.getBlockUsers().contains(targetUser));
                throw new CustomControllerExecption("차단한 유저의 정보에 접근할 수 없습니다.", HttpStatus.FORBIDDEN);
            }
            if(member.getUserBlockedMe().contains(targetUser)){
                System.out.println(member.getId() + " 번 유저가 " + targetUser.getId() + " 에게 메시지를 보냄 2 ");
                log.info("상대방이 날 차단했니? {}", member.getUserBlockedMe().contains(targetUser));
                throw new CustomControllerExecption("차단당한 유저의 정보에 접근할 수 없습니다.", HttpStatus.FORBIDDEN);
            }
//            // 차단하거나 차단된 유저도 아닐 경우
//            if(!(member.getBlockUsers().contains(targetUser)) && !(member.getUserBlockedMe().contains(targetUser))){
//                System.out.println(member.getId() + " 번 유저가 " + targetUser.getId() + " 에게 메시지를 보냄 3 ");
//                log.info("아무도 차단 안함");
//                return "ok";
//            }
        }
        return null;
    }
}
