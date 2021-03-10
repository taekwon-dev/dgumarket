package com.springboot.dgumarket.service.Validation;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.request.chat.ValidationRequest;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        // 물건이미지클릭시
        if (validationRequest.getProduct_id().isPresent()) {
            int productId = validationRequest.getProduct_id().get();

            Optional<Product> product = productRepository.findById(productId);
            product.orElseThrow(() -> new CustomControllerExecption("해당물건은 존재하지 않습니다.", HttpStatus.NOT_FOUND));
            if (product.get().getProductStatus() == PRODUCT_REMOVE) { throw new CustomControllerExecption("해당물건은 존재하지 않습니다.", HttpStatus.NOT_FOUND); }
            if (product.get().getProductStatus() == PRODUCT_BLIND) { throw new CustomControllerExecption("해당물건은 관리자에의해 블라인드 처리되었습니다.", HttpStatus.NOT_FOUND); }
            if (product.get().getProductStatus() == PRODUCT_SALE) {

                // 해당물건의 유저의 유효성체크
                return isValidateUser(member, product.get().getMember(), true);
            }
        }

        // 유저프로필클릭시
        if (validationRequest.getUser_id().isPresent()) {
            System.out.println("유저프로필클릭 : " + validationRequest.getUser_id().get());
            Member targetUser = memberRepository.findById(validationRequest.getUser_id().get().intValue());
            return isValidateUser(member, targetUser, false);
        }
        return null;
    }




    public String isValidateUser(Member member, Member targetUser, boolean fromProduct) throws CustomControllerExecption {

        // 유저제재확인, 유저탈퇴확인, 차단까지 확인
        if (targetUser == null) { throw new CustomControllerExecption("해당유저는 존재하지 않습니다.", HttpStatus.NOT_FOUND); }
        if (targetUser.getIsEnabled() == USER_NOT_ENABLED) {
            if(fromProduct){
                throw new CustomControllerExecption("관리자로부터 제재를 받고 있는 유저의 물건은 조회하실 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            throw new CustomControllerExecption("관리자로부터 제재를 받고 있는 유저의 상점을 조회할 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        if (targetUser.getIsWithdrawn() == USER_WITHDRAWN) {
            if(fromProduct){
                throw new CustomControllerExecption("탈퇴한 유저입니다.", HttpStatus.NOT_FOUND);
            }
            throw new CustomControllerExecption("탈퇴한 유저입니다.", HttpStatus.NOT_FOUND);
        }
        if (targetUser.getIsWithdrawn() == USER_NOT_WITHDRAWN && member.getIsEnabled() == USER_ENABLED){ // 유저제재X, 탈퇴X

            if(member.getBlockUsers().contains(targetUser)){
                if(fromProduct){
                    throw new CustomControllerExecption("차단한 유저의 물건은 조회하실 수 없습니다.", HttpStatus.FORBIDDEN);
                }
                throw new CustomControllerExecption("차단한 유저의 상점은 조회하실 수 없습니다.", HttpStatus.FORBIDDEN);
            }
            if(member.getUserBlockedMe().contains(targetUser)){
                if(fromProduct){
                    throw new CustomControllerExecption("차단당한 유저의 물건은 조회할 수 없습니다.", HttpStatus.FORBIDDEN);
                }
                throw new CustomControllerExecption("차단당한 유저의 상점은 조회할 수 없습니다.", HttpStatus.FORBIDDEN);
            }
            // 차단하거나 차단된 유저도 아닐 경우
            if(!(member.getBlockUsers().contains(targetUser)) && !(member.getUserBlockedMe().contains(targetUser))){
                return "ok";
            }
        }
        return null;
    }
}
