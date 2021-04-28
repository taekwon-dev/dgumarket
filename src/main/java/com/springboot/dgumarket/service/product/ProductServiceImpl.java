package com.springboot.dgumarket.service.product;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.dto.product.*;
import com.springboot.dgumarket.dto.shop.ShopFavoriteListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.NotFoundException.ProductNotFoundException;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.payload.request.product.LikeRequest;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.CustomProductRepository;
import com.springboot.dgumarket.repository.product.ProductCategoryRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by TK YOUN (2020-12-22 오후 10:09)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private ProductCategoryRepository productCategoryRepository;
    private ModelMapper modelMapper;
    private ProductListIndex productListIndex;
    private MemberRepository memberRepository;
    private CustomProductRepository customProductRepository; // 새롭게 적용되는 리포지토리

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ModelMapper modelMapper, MemberRepository memberRepository,  CustomProductRepository customProductRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.modelMapper = modelMapper;
        this.memberRepository = memberRepository;
        this.customProductRepository = customProductRepository;
    }

    // 상품 업로드
    // 예외처리
    @Override
    public Product doUplaodProduct(ProductCreateDto productCreateDto) {

        ProductCategory productCategory = productCategoryRepository.findById(productCreateDto.getProductCategory());


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Authentication 객체 NPE 미리 방지 (= JwtInterceptor)
        // Authentication Provider가 객체 주입을 실패하는 경우 -> Spring Cloud Gateway 서버에서 미리 점검
        // 예외적인 상황으로 Dgumarket 서버로 직접 접근하는 경우에도 JwtInterceptor에서 필터 역학을 한다. (인증 실패로 Authentication 주입 실패 시 -> 로그인 페이지 리다이렉트)
        // 단, 로그인 페이지 JS 파일이 참조하는 경로가 SCG로 향할 수 있게 조치해야 할 것
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Member member = Member.builder()
                .id(userDetails.getId())
                .build();

        Product product = Product.builder()
                .member(member)                                                 // Member : Product = One : Many
                .title(productCreateDto.getTitle())                             // 상품 타이틀
                .information(productCreateDto.getInformation())                 // 상품 정보
                .price(productCreateDto.getPrice())                             // 상품 가격
                .imgDirectory(productCreateDto.getImgDirectory())               // 상품 이미지 저장 경로
                .productCategory(productCategory)                               // Product : ProductCategory = Many : One
                .isNego(productCreateDto.getIsNego())                           // 가격 조율 가능 여부 (0 : 가능, 1 : 불가능)
                .transactionStatusId(productCreateDto.getTransactionStatusId()) // 거래 상태 코드
                .transactionModeId(productCreateDto.getTransactionModeId())     // 거래 방식 코드
                .selfProductStatus(1)                                           // 판매자 자체 상품 상태 평가 (1로 고정 - 활용X)
                .build();

        productRepository.save(product);
        // 응답 -> 업로드한 상푸의 고유 아이디를 반환

        Product productForId = productRepository.findTopByMemberOrderByCreateDatetimeDesc(member);
        return productForId;
    }

    // 상품 수정
    @Transactional
    @Override
    public Product doUpdateProduct(ProductModifyDto productModifyDto) {

        // 예외처리 해당 상품을 찾을 수 없는 경우 예외처리 포인트 (NPE)
        Product product = productRepository.findByIdNotOptional(productModifyDto.getProductId());

        if (product == null) throw new ProductNotFoundException(errorResponse("수정하려는 상품 정보를 찾을 수 없는 경우", 305, "/api/product/modify"));

        // 상품의 '카테고리'를 수정
        // 바뀐 카테고리 코드 값을 활용해서 카테고리 오브젝트를 불러온 후 수정
        ProductCategory updatedCategory = productCategoryRepository.findById(productModifyDto.getProductCategory());

        // @Transactional -> Update Query 확인 (데이터가 다른 경우)
        product.updateProductTitle(productModifyDto.getTitle());
        product.updateProductPrice(productModifyDto.getPrice());
        product.updateProductInfo(productModifyDto.getInformation());
        product.updateProductImgDir(productModifyDto.getImgDirectory());
        product.updateProductIsNego(productModifyDto.getIsNego());
        product.updateProductTranactionMode(productModifyDto.getTransactionModeId());
        product.updateProductTransactionStatus(productModifyDto.getTransactionStatusId());
        product.updateProductCategory(updatedCategory);

        return product;
    }

    @Transactional
    @Override
    public void doDeleteProduct(int productId) {

        // 예외 해당 상품에 맞는 데이터가 없는 경우 (예외처리)
        // 상품 고유 아이디로 해당 엔티티를 불러온다.
        Product product = productRepository.findByIdNotOptional(productId);

        if (product == null) throw new ProductNotFoundException(errorResponse("삭제하려는 상품 정보를 찾을 수 없는 경우", 305, "/api/product/delete"));

        // productStatus {0 : 등록, 1 : 삭제}
        product.updateProductStatus(1);

    }


    // 메인 페이지 (index) : 비로그인 상태로 접속한 경우 -> 인기 카테고리, 카테고리 별 상품 리스트 리턴.
    @Override
    public List<ProductListIndex> indexNotLoggedIn(int lastCategoryId) {
        // init
        List<ProductListIndex> productListIndexList = new ArrayList<>();
        modelMapper = new ModelMapper();

        org.modelmapper.PropertyMap<Product, ProductReadListDto> map_product = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {

                // [이미지 디렉토리] source (= product)에서 메인 이미지 출력 후 thumbnail에 매핑.
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                // 메인 페이지 물건리스트 업로드 시간 (업로드 시간 기준으로 정렬)
                // 2021-02-21
                map().setUploadDatetime(source.getUpdateDatetime());
                map().setThumbnailImg(source.getImgDirectory());
                map().setCategory_id(source.getProductCategory().getId());
            }
        };

        modelMapper.addMappings(map_product); // 상품 정보에서 인덱스 화면에 보여질 데이터만 활용하기 위해 DTO로 변환

        // Pageable 오브젝트 생성
        Pageable pageable = PageRequest.of(0, 3);

        // {category_type : 1} 카테고리 중, `인기` 카테고리를 조회한다. (실제 서비스를 운영하면서 인기 카테고리가 변경될 수 있으므로 동적으로 처리)
        // {lastCategoryId}  순서상 마지막 카테고리 고유 id를 의미하고, 초기값은 0이다. 초기값 또는 순서상 마지막 카테고리 고유 id를 기준으로 다음 세 가지 항목을 가져오는 쿼리가 수행된다.
        // 인기카테고리는 (우선) 3가지 항목이므로, 이 기준에서는 최초 초기값 0이 요청이 된 이후 반환되는 값은 없다. (총 가지수가 세 가지 이므로)
        // {pageable} : 페이징 오브젝트로, 클라이언트 측에서 무한 스크롤 기반 페이징을 제공하므로, 페이지는 0으로 고정, 사이즈는 3으로 설정되어 있다
        List<ProductCategory> popularCategories = productCategoryRepository.findAllByCategoryTypeAndIdGreaterThanOrderByIdAsc(1, lastCategoryId, pageable).toList();

        // `인기 카테고리`기준 (도서 / 의류 / 기프티콘) 각 카테고리 별 최대 4개 상품을 조회
        // 비로그인 상태로 인덱스 화면에 접속하는 경우 호출된다.

        // 인기 카테고리 수 만큼 반복문
        // 최종적으로 반환 할 `List<ProductListIndex>` 만든다.
        for (int i = 0; i < popularCategories.size(); i++) {
            // DTO 형태 만들고, 리스트 형태로 해당 디티오들을 담는다.
            // DTO를 담고 있는 리스트에서 STREAM을 통해 카테고리별로 상품을 분리하는 로직을 통과하고,
            // 최종 보여질 데이터를 생성한다. (반환데이터)
            List<ProductReadListDto> productReadListDtos = customProductRepository.findIndexProductsByCategory(popularCategories.get(i))
                    .stream()
                    .map(productList -> modelMapper.map(productList, ProductReadListDto.class))
                    .collect(Collectors.toList());
            productListIndex = ProductListIndex.builder()
                    .category_id(popularCategories.get(i).getId())
                    .category_name(popularCategories.get(i).getCategoryName())
                    .productsList(productReadListDtos)
                    .build();
            productListIndexList.add(productListIndex);
        }

        // 결과 반환
        return productListIndexList;
    }


    // 관심 카테고리 (유저 별)
    @Override
    public List<ProductListIndex> indexLoggedIn(UserDetailsImpl userDetails, int lastCategoryId) {

        // init
        // '유저' 별 관심 카테고리를 추출하기 위해 로그인한 유저의 고유 아이디를 가진 Member 객체 생성
        Member member = Member.builder()
                .id(userDetails.getId())
                .build();
        Member member1 = memberRepository.findById(userDetails.getId());

        // ModelMapper 객체 생성
        modelMapper = new ModelMapper();

        // 최종 반환 할 오브젝트 : 관심 카테고리 별 상품 리스트 (최대 4개)
        List<ProductListIndex> productListIndexList = new ArrayList<>();

        // Pageable 오브젝트 생성 (무한 스크롤 형식이므로, page : 0으로 고정, 사이즈는 3 : 카테고리를 최대 3개까지 보여준다)
        Pageable pageable = PageRequest.of(0, 3);

        // Product -> ProductReadListDto 매핑
        org.modelmapper.PropertyMap<Product, ProductReadListDto> map = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {
                // [이미지 디렉토리] source (= product)에서 메인 이미지 출력 후 thumbnail에 매핑.
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                // 메인 페이지 물건리스트 업로드 시간 (업로드 시간 기준으로 정렬)
                // 2021-02-21
                map().setUploadDatetime(source.getUpdateDatetime());
                map().setThumbnailImg(source.getImgDirectory());
                map().setCategory_id(source.getProductCategory().getId());
            }
        };
        modelMapper.addMappings(map);

        // 유저의 관심 카테고리 목록을 불러온다.
        List<ProductCategory> interestedCategories = productCategoryRepository.findByMembersAndIdGreaterThanOrderByIdAsc(member1, lastCategoryId, pageable).toList();

        // 유저의 관심 카테고리 (최대 3개 - 페이징 적용)
        // 유저의 관심 카테고리 수 만큼 반복문이 돌면서, 최종 반환될 카테고리 별 상품 리스트를 조합
        for (int i = 0; i < interestedCategories.size(); i++) {

            // DTO 형태 만들고, 리스트 형태로 해당 디티오들을 담는다.
            // DTO를 담고 있는 리스트에서 STREAM을 통해 카테고리별로 상품을 분리하는 로직을 통과하고,
            // 최종 보여질 데이터를 생성한다. (반환데이터)
            List<ProductReadListDto> productReadListDtos = customProductRepository.findIndexProductsByCategoryLogin(member1, interestedCategories.get(i))
                    .stream()
                    .map(productList -> modelMapper.map(productList, ProductReadListDto.class))
                    .collect(Collectors.toList());

            productListIndex = ProductListIndex.builder()
                    .category_id(interestedCategories.get(i).getId())
                    .category_name(interestedCategories.get(i).getCategoryName())
                    .productsList(productReadListDtos)
                    .build();
            productListIndexList.add(productListIndex);
        }


        return productListIndexList;
    }

    //************************************************ [ 유저 SHOP ] ***************************************************

    // 유저의 판매물품 조회
    @Override
    public ShopProductListDto getUserProducts(UserDetailsImpl loginUser, Integer userId, String productSet, Pageable pageable, Integer exceptPid) {
        Member loginMember = null;
        Member targerMember = null;
        Product exceptProduct = null; // 유저가 올린 다른 물건들조회시 제외할 물건

        if(userId != null){ // 로그인한유저가 본인것조회
            targerMember = memberRepository.findById(userId.intValue());
        }
        if(loginUser != null){ // 로그인한맴버
            loginMember = memberRepository.findById(loginUser.getId());
        }
        if(exceptPid != null){ // 제외할 물건(개별물건페이지의 물건고유번호)
            exceptProduct = productRepository.getOne(exceptPid);
        }

        modelMapper = new ModelMapper();
        // User -> UserDTO 매핑설정
        org.modelmapper.PropertyMap<Product, ProductReadListDto> listDtoPropertyMap = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {
                // [이미지 디렉토리] source (= product)에서 메인 이미지 출력 후 thumbnail에 매핑.
                map().setThumbnailImg(source.getImgDirectory());
                map().setTitle(source.getTitle());
                map().setPrice(source.getPrice());
                map().setId(source.getId());
                map().setChatroomNums(source.getChatroomNums());
                map().setLikeNums(source.getLikeNums());
                map().setUploadDatetime(source.getCreateDatetime());
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                map().setTransaction_status_id(source.getTransactionStatusId());
            }
        };
        modelMapper.addMappings(listDtoPropertyMap);

        PageImpl<Product> userProducts = customProductRepository.findUserProducts(loginMember, targerMember, productSet, pageable, exceptProduct);
        List<ProductReadListDto> productReadListDtos = userProducts.getContent().stream()
                .map(product -> modelMapper.map(product, ProductReadListDto.class))
                .collect(Collectors.toList());


        return ShopProductListDto.builder()
                .total_size((int) userProducts.getTotalElements())
                .page_size(productReadListDtos.size())
                .productsList(productReadListDtos)
                .build();
    }

    // 유저 관심물건 조회하기
    @Override
    @Transactional
    public ShopFavoriteListDto getFavoriteProducts(UserDetailsImpl userDetails, Pageable pageable) {

        Member member = memberRepository.findById(userDetails.getId());
        PropertyMap<Product, ProductReadListDto> listDtoPropertyMap = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setTitle(source.getTitle());
                map().setPrice(source.getPrice());
                map().setThumbnailImg(source.getImgDirectory());
                map().setChatroomNums(source.getChatroomNums());
                map().setLikeNums(source.getLikeNums());
                map().setUploadDatetime(source.getCreateDatetime());
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                map().setTransaction_status_id(source.getTransactionStatusId());
                map().setLikeStatus("like");
            }
        };
        modelMapper = new ModelMapper();
        modelMapper.addMappings(listDtoPropertyMap);
        Pageable productPageable = null;

        if (productPageable == null){ // 아무런 조건을 주지 않았을 경우
            productPageable = pageable;
        }

        PageImpl<Product> productLikes = customProductRepository.findAllFavoriteProducts(member, productPageable);
        List<ProductReadListDto> productReadListDtos =
                productLikes.getContent().stream()
                        .map(product -> modelMapper.map(product, ProductReadListDto.class))
                        .collect(Collectors.toList());

        return ShopFavoriteListDto.builder()
                .productsList(productReadListDtos)
                .page_size(productReadListDtos.size())
                .total_size((int)productLikes.getTotalElements()).build(); // 총 관심물건 개수
    }



    //***************************************************************************************************************************

    // 카테고리별 물품 조회하기
    // 회원 탈퇴 적용(21.02.26)
    // 유저제재, 탈퇴, 블라인드, 차단 적용 ( 3.12 )
    @Override
    public ShopProductListDto getProductsByCategory(UserDetailsImpl userDetails, int categoryId, Pageable pageable, Integer exceptPid) {
        Member member = null;
        Product execptProduct = null;
        if(userDetails != null){
            member = memberRepository.findById(userDetails.getId());
        }

        if(exceptPid != null){ // 개별물건페이지, [카테고리]의 최신물건들(현재 물건상세페이지에 있는 물건 제외)
            execptProduct = productRepository.getOne(exceptPid);
        }

        ModelMapper modelMapper = new ModelMapper();
        // Product -> ProductReadListDto
        org.modelmapper.PropertyMap<Product, ProductReadListDto> listDtoPropertyMap = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {
                // [이미지 디렉토리] source (= product)에서 메인 이미지 출력 후 thumbnail에 매핑.
                map().setThumbnailImg(source.getImgDirectory());
                map().setTitle(source.getTitle());
                map().setPrice(source.getPrice());
                map().setId(source.getId());
                map().setChatroomNums(source.getChatroomNums());
                map().setLikeNums(source.getLikeNums());
                map().setUploadDatetime(source.getCreateDatetime());
                map().setTransaction_status_id(source.getTransactionStatusId());
                when(Conditions.isNull()).skip().setLastUpdatedDatetime(source.getUpdateDatetime());
            }
        };
        modelMapper.addMappings(listDtoPropertyMap);
        PageImpl<Product> products = customProductRepository.findAllPagingByCategory(member, categoryId, pageable, execptProduct);
        List<ProductReadListDto> productReadListDtos = products.stream()
                .map(product -> modelMapper.map(product, ProductReadListDto.class))
                .collect(Collectors.toList());

        ShopProductListDto shopProductListDto = ShopProductListDto.builder()
                .page_size(productReadListDtos.size())
                .productsList(productReadListDtos).build();
        if(pageable.getPageSize() == 4){
            shopProductListDto.setTotal_size((int) products.getTotalElements());
        }
        return shopProductListDto;
    }

    // 전체 물건 보기
    // 회원 탈퇴 적용(21.02.26)
    // 유저제재, 탈퇴, 블라인드, 차단 적용 ( 3. 12 )
    @Override
    public ShopProductListDto getAllProducts(UserDetailsImpl userDetails, Pageable pageable) {
        ModelMapper modelMapper = new ModelMapper();
        Member member = null;
        if(userDetails != null){
            member = memberRepository.findById(userDetails.getId());
        }
        // Product -> ProductReadListDto
        org.modelmapper.PropertyMap<Product, ProductReadListDto> listDtoPropertyMap = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {
                // [이미지 디렉토리] source (= product)에서 메인 이미지 출력 후 thumbnail에 매핑.
                map().setThumbnailImg(source.getImgDirectory());
                map().setTitle(source.getTitle());
                map().setPrice(source.getPrice());
                map().setId(source.getId());
                map().setChatroomNums(source.getChatroomNums());
                map().setLikeNums(source.getLikeNums());
                map().setUploadDatetime(source.getCreateDatetime());
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                map().setTransaction_status_id(source.getTransactionStatusId());
            }
        };
        modelMapper.addMappings(listDtoPropertyMap);
        PageImpl<Product> products = customProductRepository.findAllPaging(member, pageable);
        List<ProductReadListDto> productReadListDtos = products.getContent()
                .stream()
                .map(product -> modelMapper.map(product, ProductReadListDto.class))
                .collect(Collectors.toList());
        return ShopProductListDto.builder()
                .total_size(null)
                .page_size(products.getNumberOfElements())
                .productsList(productReadListDtos).build();
    }

    // 물건 정보 보기
    @Override
    public ProductReadOneDto getProductInfo(UserDetailsImpl userDetails, int productId) throws CustomControllerExecption {
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomControllerExecption("존재하지 유저입니다.", HttpStatus.NOT_FOUND, null));
        if(product.getProductStatus() == 1){
            throw new CustomControllerExecption("삭제된 물건입니다.",HttpStatus.NOT_FOUND, null);
        }

        PropertyMap<Product, ProductReadOneDto> propertyMap = new PropertyMap<Product, ProductReadOneDto>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setUserId(source.getMember().getId());
                map().setUserNickName(source.getMember().getNickName());
                map().setProfileImgDirectory(source.getMember().getProfileImageDir());
                map().setIsNego(source.getIsNego());
                map().setTitle(source.getTitle());
                map().setImgDirectories(source.getImgDirectory());
                map().setCategoryId(source.getProductCategory().getId());
                map().setProductCategory(source.getProductCategory().getCategoryName());
                map().setPrice(source.getPrice());
                map().setInformation(source.getInformation());
                map().setLikeNums(source.getLikeNums());
                map().setChatroomNums(source.getChatroomNums());
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                map().setUploadDatetime(source.getCreateDatetime());
                map().setWarn(source.getMember().checkWarnActive()); // 물건올린유저의 경고유무
            }
        };
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(propertyMap);

        if(userDetails != null){ // 로그인유저

            // 차단된 사용자의 물건에 접속시 예외처리
            Member member = memberRepository.findById(userDetails.getId());
            if(member.getBlockUsers().contains(product.getMember()) || member.getUserBlockedMe().contains(product.getMember())){
                throw new CustomControllerExecption("차단한 유저 혹은 차단된 유저의 물건에 접근할 수 없습니다.", HttpStatus.GONE, null);
            }

            ProductReadOneDto readOneDto = modelMapper.map(product, ProductReadOneDto.class);
            if (member.getLikeProducts().contains(product)) { // 내가 좋아요 한 물건
                readOneDto.setIsLiked("like");
            }
            return readOneDto;
        }
        return modelMapper.map(product, ProductReadOneDto.class);
    }


    //**************************************************[ 물건 추가 기능들 ]**************************************************

    // 좋아요 기능
    @Override
    @Transactional
    public String changeLikeProduct(UserDetailsImpl userDetails, LikeRequest likeRequest) throws CustomControllerExecption{
        Member member = memberRepository.findById(userDetails.getId());
        Product product = productRepository.findById(likeRequest.getProduct_id()).orElseThrow(() -> new CustomControllerExecption("not found result", HttpStatus.NOT_FOUND, null)); // 좋음! noSuch 디테일 구분 ?

        if(product.getProductStatus() == 1){ // 삭제 예외
            throw new CustomControllerExecption("해당 게시물은 존재하지 않습니다.", HttpStatus.NOT_FOUND, null);
        }

        // 차단된 사용자의 물건에 좋아요 할경우 예외처리
        if(member.getBlockUsers().contains(product.getMember()) || member.getUserBlockedMe().contains(product.getMember())){
            throw new CustomControllerExecption("차단한 유저 혹은 차단된 유저의 물건에 접근할 수 없습니다.", HttpStatus.GONE, null);
        }

        if(likeRequest.getCurrent_like_status().equals("nolike")){
            member.like(product); // 좋아요
            return "like";
        }else {
            member.unlike(product); // 좋아요 취소
            return "nolike";
        }
    }

    @Override
    public ShopProductListDto getProductBySearch(Authentication authentication, Pageable pageable, String categoryId, String keyword) {

        // init
        Member member = null;

        // ShopProductListDto
        // total_size : 검색을 통해 조회된 전체 상품 수
        // page_size
        // List<ProductReadListDto> productsList : 상품 정보 DTO 리스트

        // (로그인) ----- (리프레시 토큰 발급) ---- (리프레시 API) ---- (A 토큰 발급) ...
        // 로그인 이후 클라이언트가 A 토큰 없는 상태로 요청을 보낼 수 없는 구조

        // 로그인 여부 확인 (via Access Token)
        // JwtInterceptor를 통과한 시점
        // 로그인 상태(=A 토큰 수반한 상태로 요청)에서 서비스 레이어에 도달한 경우 로그인 상태임을 전제할 수 있다.

        // [예외처리] 요청한 유저의 회원탈퇴, 이용제재 또는 A 토큰이 유효하지 않은 경우는 SCG에서 처리

        // 로그인 상태의 경우
        if (authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            member = memberRepository.findById(userDetails.getId());
        }

        ModelMapper modelMapper = new ModelMapper();
        // Product -> ProductReadListDto
        org.modelmapper.PropertyMap<Product, ProductReadListDto> listDtoPropertyMap = new PropertyMap<Product, ProductReadListDto>() {
            @Override
            protected void configure() {
                // [이미지 디렉토리] source (= product)에서 메인 이미지 출력 후 thumbnail에 매핑.
                map().setThumbnailImg(source.getImgDirectory());
                map().setTitle(source.getTitle());
                map().setPrice(source.getPrice());
                map().setId(source.getId());
                map().setChatroomNums(source.getChatroomNums());
                map().setLikeNums(source.getLikeNums());
                map().setUploadDatetime(source.getCreateDatetime());
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                map().setTransaction_status_id(source.getTransactionStatusId());
            }
        };
        modelMapper.addMappings(listDtoPropertyMap);

        PageImpl<Product> products = customProductRepository.findAllPagingBySearch(member, pageable, categoryId, keyword);

        List<ProductReadListDto> productReadListDtos = products.getContent()
                .stream()
                .map(product -> modelMapper.map(product, ProductReadListDto.class))
                .collect(Collectors.toList());

        return ShopProductListDto.builder()
                .total_size((int)products.getTotalElements())
                .page_size(products.getNumberOfElements())
                .productsList(productReadListDtos).build();
    }

    public String errorResponse(String errMsg, int resultCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int statusCode;
        //     Date timestamp;
        //     String message;
        //     String requestPath;
        //     String pathToMove;
        // }

        // errorCode에 따라서 예외 결과 클라이언트가 특정 페이지로 요청해야 하는 경우가 있다.
        // 그 경우 pathToMove 항목을 채운다.

        // init
        ErrorMessage errorMessage = null;

        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;

        // 예외 처리 결과 클라이언트가 이동시킬 페이지 참조 값을 반환해야 하는 경우 에러 코드 범위
        // 예외처리 결과 클라이언트가 __페이지를 요청해야 하는 경우
        // 해당 페이지 정보 포함해서 에러 메시지 반환
        if (resultCode >= 300 && resultCode < 350) {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .pathToMove("/shop/main/index") // 추후 index 페이지 경로 바뀌면 해당 경로 값으로 수정 할 것.
                    .build();
        } else {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .build();

        }

        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }
}
