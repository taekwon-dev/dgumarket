package com.springboot.dgumarket.controller.member;

import com.springboot.dgumarket.dto.block.BlockUserListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.block.BlockUserRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.block.UserBlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemberBlockController {


    private static final int DEFAULT_PAGE_SIZE = 10;
    @Autowired
    UserBlockService userBlockService;

    // 유저차단하기
    @PostMapping("/block")
    public String blockUser(Authentication authentication, @RequestBody BlockUserRequest blockUserRequest) throws CustomControllerExecption {

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            boolean isBlock = userBlockService.blockUser(userDetails.getId(), blockUserRequest.getBlock_user());
            if(!isBlock){
                throw new CustomControllerExecption("You can't block it because you have a transaction history with the other person", HttpStatus.ACCEPTED);
            }
            return "block success"; // 유저차단 성공

        }

        return null;
    }

    // 유저차단 해제하기
    @DeleteMapping("/unblock/{userId}")
    public String unblockUser(Authentication authentication, @PathVariable("userId") int unblockUserId){
        if(authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            userBlockService.unBlockUser(userDetails.getId(), unblockUserId);
            return "unblock success";
        }
        return null;
    }

    // 차단유무 확인하기(내가상대방차단 , 상대방이나를 :2 , 그외 경우 :3)
    @GetMapping("/block/read/{userId}")
    public ResponseEntity<ApiResponseEntity> checkBlockStatusByUser(Authentication authentication, @PathVariable("userId") int targetUserId){

        if(authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .status(200)
                    .message("유저 차단 유무 조회")
                    .data(userBlockService.checkBlockStatus(userDetails.getId(), targetUserId)).build();
            return new ResponseEntity<ApiResponseEntity>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }

    // 유저의 차단리스트 가져오기
    @GetMapping("/blocklist")
    public ResponseEntity<ApiResponseEntity> getBlockList(
            Authentication authentication,
            @SortDefault(sort = "block_date", direction = Sort.Direction.DESC) // 차단일 내림차순
            Pageable pageable){
        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            BlockUserListDto blockUserListDto = userBlockService.getUserBlockList(userDetails.getId(), pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .status(200)
                    .message("차단 리스트 조회")
                    .data(blockUserListDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }
}
