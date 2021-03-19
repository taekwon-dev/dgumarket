package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.chat.ChatRoomTradeHistoryDto;
import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.product.ProductDeleteDto;
import com.springboot.dgumarket.dto.product.ProductModifyDto;
import com.springboot.dgumarket.dto.product.ProductReadOneDto;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.request.PagingIndexRequest;
import com.springboot.dgumarket.payload.request.product.LikeRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.chat.ChatRoomService;
import com.springboot.dgumarket.service.product.ProductService;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by TK YOUN (2020-12-22 오후 10:08)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private ProductService productService;

    @Autowired
    private ChatRoomService chatRoomService;

    // 상품 업로드 API
    @PostMapping("/upload")
    public ResponseEntity<ApiResponseEntity> doUploadProduct(Authentication authentication, @RequestBody ProductCreateDto productCreateDto) {

        // 예외처리 (API 요청 시 인증 절차에서 문제가 있는 경우) + 상품 업로드 상황에서.
        if (authentication == null) return null;

        // 업로드 이후 -> 해당 상품의 상세 정보 페이지로 이동한다.
        // 상품 상세정보 URL : http://dgumarket.co.kr/product/`4` = 변수 (product_id)
        // 클라이언트 측에서 해당 페이지로 이동할 수 있도록 product_id를 보내줘야 한다.
        // 예외처리 ; 서비스 레이어에서 처리 예정
        Product product = productService.doUplaodProduct(productCreateDto);

        // 상품 고유 아이디 반환 -> (해당 페이지로 이동할 수 있도록)
        int productId = product.getId();

        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("상품 업로드 성공")
                .data(productId) // 상품 고유 아이디 반환
                .status(200)
                .build();

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.CREATED);


    }

    @PostMapping("/modify")
    public ResponseEntity<ApiResponseEntity> doModifyProduct(Authentication authentication, @RequestBody ProductModifyDto productModifyDto) {

        // init
        // 상품 정보를 수정하는 상황 -> `어떤` 물건
        int productId = 0;

        // 예외처리 (API 요청 시 인증 절차에서 문제가 있는 경우) + 상품 수정 상황에서.
        if (authentication == null) return null;

        Optional<Product> product = productService.doUpdateProduct(productModifyDto);

        // 상품 고유 아이디 반환 -> (해당 페이지로 이동할 수 있도록)
        productId = product.get().getId();

        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("상품 정보 업데이트 성공")
                .data(productId) // 상품 고유 아이디 반환
                .status(200)
                .build();

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }


    @PostMapping("/delete")
    public ResponseEntity<ApiResponseEntity> doDeleteProduct(Authentication authentication, @RequestBody ProductDeleteDto productDeleteDto) {

        // API 인증 예외 -
        if (authentication == null) return null;

        // `어떤` 상품을 삭제하는 지 (via 상품 고유 아이디를 받아서 처리)
        // 상품의 Status 값을 삭제 값으로 수정 {product_status : 1 ; 삭제}
        productService.doDeleteProduct(productDeleteDto.getProductId());

        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("상품 삭제 성공")
                .data(null)
                .status(200)
                .build();

       return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 개별 물건 조회하기
    @GetMapping("/{productId}/info")
    @CheckProductDeleted
    public ResponseEntity<?> getProductInfo(
            Authentication authentication,
            @PathVariable("productId") int productId) throws CustomControllerExecption {

        if(authentication != null){ // 로그인 유저
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ProductReadOneDto readOneDto = productService.getProductInfo(userDetails, productId);
            if(readOneDto.getUserId() == userDetails.getId()){ // 내 물건에 들어가는 경우
                log.info("readOneDto.getUserId() " + readOneDto.getUserId());
                log.info("userDetails.getId() " + userDetails.getId());
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("my_product")
                        .status(200)
                        .data(readOneDto).build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("user_product")
                    .status(200)
                    .data(readOneDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        ProductReadOneDto readOneDto = productService.getProductInfo(null, productId);
        log.info("auth x - readOneDto.getUserId() " + readOneDto.getUserId());
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("user_product")
                .status(200)
                .data(readOneDto).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 전체 물건 조회
    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts(
            Authentication authentication,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "createDatetime", direction = Sort.Direction.DESC) Pageable pageable){

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ShopProductListDto shopProductListDto = productService.getAllProducts(userDetails, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("전체 물건 조회")
                    .status(200)
                    .data(shopProductListDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        ShopProductListDto shopProductListDto = productService.getAllProducts(null, pageable);
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("전체 물건 조회")
                .status(200)
                .data(shopProductListDto).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 좋아요 기능
    @PatchMapping("/like")
    public ResponseEntity<?> cancelLikeProduct(
            Authentication authentication,
            @Valid @RequestBody LikeRequest likeRequest, Errors errors) throws CustomControllerExecption {
        if (authentication == null) throw new CustomControllerExecption("로그인이 필요한 서비스", HttpStatus.UNAUTHORIZED);
        if(authentication != null){
            // 유효성 에러
            if (errors.hasErrors()) { // 400
                throw new CustomControllerExecption(Objects.requireNonNull(errors.getFieldError()).getDefaultMessage(), HttpStatus.BAD_REQUEST);
            }
            String resultMessage;
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            String currentStatus = productService.changeLikeProduct(userDetails, likeRequest);
            if (currentStatus.equals("like")){
                resultMessage = "좋아요 요청";
            }else{
                resultMessage = "좋아요 취소 요청";
            }
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .status(200)
                    .message(resultMessage)
                    .data("current_status_" + currentStatus).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        return null;
    }

    @GetMapping
    public void searchProduct(
            Authentication authentication,
            @RequestParam String q,
            @RequestParam(required = false) int category_id){
    }

    // 채팅으로거래하기 클릭시 해당 물건과 채팅 중인지 아닌 지 확인하기
    @GetMapping("/{productId}/chat-history")
    public ResponseEntity<?> doCheckChatroomHistory(
            Authentication authentication,
            @PathVariable("productId") int productId) throws CustomControllerExecption {
        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ChatRoomTradeHistoryDto chatRoomTradeHistoryDto = chatRoomService.checkChatHistory(userDetails.getId(), productId); // 이전과 채팅한 적 있는 지 체크하기
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .status(200)
                    .message("이전에 채팅거래했는지 조회")
                    .data(chatRoomTradeHistoryDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }


        return null;
    }
}
