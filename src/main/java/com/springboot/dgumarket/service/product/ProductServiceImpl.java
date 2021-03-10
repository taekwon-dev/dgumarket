package com.springboot.dgumarket.service.product;


import com.springboot.dgumarket.dto.product.*;
import com.springboot.dgumarket.dto.shop.ShopFavoriteListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.payload.request.product.LikeRequest;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductCategoryRepository;
import com.springboot.dgumarket.repository.product.ProductLikeRepository;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    private ProductLikeRepository productLikeRepository;

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ModelMapper modelMapper, MemberRepository memberRepository, ProductLikeRepository productLikeRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.modelMapper = modelMapper;
        this.memberRepository = memberRepository;
        this.productLikeRepository = productLikeRepository;
    }

    // 상품 업로드
    // 예외처리
    @Override
    public Product doUplaodProduct(ProductCreateDto productCreateDto) {

        ProductCategory productCategory = productCategoryRepository.findById(productCreateDto.getProductCategory());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


        // 예외처리
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
                    .selfProductStatus(productCreateDto.getSelfProductStatus())     // 판매자 자체 상품 상태 평가
                    .build();

        // 예외처리
        productRepository.save(product);

        // 응답 -> 업로드한 상푸의 고유 아이디를 반환
        Product productForId = productRepository.findTopByMemberOrderByCreateDatetimeDesc(member); // 예외처리
        return productForId;
    }

    // 상품 수정
    @Transactional
    @Override
    public Optional<Product> doUpdateProduct(ProductModifyDto productModifyDto) {

        // 예외처리 해당 상품을 찾을 수 없는 경우 예외처리 포인트 (orElseThorw 활용)
        Optional<Product> product = productRepository.findById(productModifyDto.getProductId());

        // 상품의 '카테고리'를 수정
        // 바뀐 카테고리 코드 값을 활용해서 카테고리 오브젝트를 불러온 후 수정
        ProductCategory updatedCategory = productCategoryRepository.findById(productModifyDto.getProductCategory());

        // @Transactional -> Update Query 확인
        // 엔티티 영속성 및 JPA 관련 공부 필수
        product.ifPresent(productForUpdate -> {
            productForUpdate.setTitle(productModifyDto.getTitle());
            productForUpdate.setPrice(productModifyDto.getPrice());
            productForUpdate.setInformation(productModifyDto.getInformation());
            productForUpdate.setImgDirectory(productModifyDto.getImgDirectory());
            productForUpdate.setProductCategory(updatedCategory);
            productForUpdate.setIsNego(productModifyDto.getIsNego());
            productForUpdate.setTransactionModeId(productModifyDto.getTransactionModeId());
            productForUpdate.setTransactionStatusId(productModifyDto.getTransactionStatusId());
            productForUpdate.setSelfProductStatus(productModifyDto.getSelfProductStatus());
        });

        return product;
    }

    @Transactional
    @Override
    public void doDeleteProduct(int productId) {

        // 예외 해당 상품에 맞는 데이터가 없는 경우 (예외처리)
        // 상품 고유 아이디로 해당 엔티티를 불러온다.
        Optional<Product> product = productRepository.findById(productId);

        // 컨트롤러에서 인자로 받은 상품 고유아이디의 Status 값을 변경
        product.ifPresent(productForDelete -> {
            // {productStatus : 1 ; 삭제}
            productForDelete.setProductStatus(1);
        });

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
            List<ProductReadListDto> productReadListDtos = productRepository.findTop4ByProductCategoryOrderByCreateDatetimeDesc(popularCategories.get(i))
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
        List<ProductCategory> interestedCategories = productCategoryRepository.findByMembersAndIdGreaterThanOrderByIdAsc(member, lastCategoryId, pageable).toList();

        // 유저의 관심 카테고리 (최대 3개 - 페이징 적용)
        // 유저의 관심 카테고리 수 만큼 반복문이 돌면서, 최종 반환될 카테고리 별 상품 리스트를 조합
        for (int i = 0; i < interestedCategories.size(); i++) {

            // DTO 형태 만들고, 리스트 형태로 해당 디티오들을 담는다.
            // DTO를 담고 있는 리스트에서 STREAM을 통해 카테고리별로 상품을 분리하는 로직을 통과하고,
            // 최종 보여질 데이터를 생성한다. (반환데이터)
            List<ProductReadListDto> productReadListDtos = productRepository.findTop4ByProductCategoryOrderByCreateDatetimeDesc(interestedCategories.get(i))
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
    public ShopProductListDto getUserProducts(int userId, String productSet, Pageable pageable) {
        Member member = memberRepository.findById(userId);

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



        List<ProductReadListDto> productReadListDtos;
        ShopProductListDto shopProductListDto;
        Comparator<ProductReadListDto> readListDtosComparator = Comparator.comparing((ProductReadListDto o) -> parseStringToInt(o.getPrice()));
        boolean isPriceDesc; // 가격 내림차순(고가순)
        boolean isPriceAsc; // 가격 오름차순(저가순)
        switch (productSet)
        {
            case "total": // 전체
                productReadListDtos = productRepository.findAllByMember(member, pageable)
                        .stream()
                        .map(product -> modelMapper.map(product, ProductReadListDto.class))
                        .collect(Collectors.toList());
                log.info("1");

                isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
                isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
                checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);
                shopProductListDto = ShopProductListDto.builder()
                        .page_size(productReadListDtos.size())
                        .total_size((int) member.getProducts()
                                .stream()
                                .filter(e->e.getProductStatus() == 0)
                                .count())
                        .productsList(productReadListDtos).build();

                break;
            case "sale": // 판매중
                productReadListDtos = productRepository.findAllByMemberWithSort(member,0, pageable)
                        .stream()
                        .map(product -> modelMapper.map(product, ProductReadListDto.class))
                        .collect(Collectors.toList());

                isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
                isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
                checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);

                shopProductListDto = ShopProductListDto.builder()
                        .page_size(productReadListDtos.size())
                        .total_size(
                                (int) member.getProducts()
                                        .stream()
                                        .filter(e -> (e.getTransactionStatusId() == 0 && e.getProductStatus() == 0))
                                        .count()
                        )
                        .productsList(productReadListDtos).build();
                break;
            case "sold": // 거래완료
                productReadListDtos = productRepository.findAllByMemberWithSort(member,2, pageable)
                        .stream()
                        .map(product -> modelMapper.map(product, ProductReadListDto.class))
                        .collect(Collectors.toList());

                isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
                isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
                checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);

                shopProductListDto = ShopProductListDto.builder()
                        .page_size(productReadListDtos.size())
                        .total_size(
                                (int) member.getProducts()
                                        .stream()
                                        .filter(e -> (e.getTransactionStatusId() == 2 && e.getProductStatus() == 0))
                                        .count()
                        )
                        .productsList(productReadListDtos).build();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + productSet);
        }

        return shopProductListDto;
    }

    // 유저 관심물건 조회하기
    @Override
    @Transactional
    public ShopFavoriteListDto getFavoriteProducts(UserDetailsImpl userDetails, Pageable pageable) {

        Member member = memberRepository.findById(userDetails.getId());
        // 좋아요 물건의 유저가 나를 차단했다면 제외
        int totalSize = (int) member.getLikeProducts().stream() // 총관심물건수
                .filter(product -> product.getMember().getIsWithdrawn() != 1) // 탈퇴유저 제외
                .filter(product -> product.getProductStatus() != 1) // 물건 삭제 제외
                .filter(product -> !(member.getBlockUsers().contains(product.getMember()))) // 좋아요 물건에 차단한 유저의 물품이 있다면 제외
                .filter(product -> !(member.getUserBlockedMe().contains(product.getMember()))).count();

        Comparator<ProductReadListDto> readListDtosComparator = Comparator.comparing((ProductReadListDto o) -> parseStringToInt(o.getPrice()));
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

        if(pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("createdDate"))){
            if(pageable.getSort().getOrderFor("createdDate").getDirection().isAscending()){
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.createDatetime").ascending());
            }else{
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.createDatetime").descending());
            }
        }

        if(pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("chatroomNums"))){
            if(pageable.getSort().getOrderFor("chatroomNums").getDirection().isAscending()){
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.chatroomNums").ascending());
            }else{
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.chatroomNums").descending());
            }
        }

        if(pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("likeNums"))){
            if(pageable.getSort().getOrderFor("likeNums").getDirection().isAscending()){
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.likeNums").ascending());
            }else{
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.likeNums").descending());
            }
        }

        if(pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price"))){
            if(pageable.getSort().getOrderFor("price").getDirection().isAscending()){
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.price").ascending());
            }else{
                productPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("product.price").descending());
            }
        }
        if (productPageable == null){ // 아무런 조건을 주지 않았을 경우
            productPageable = pageable;
        }

        List<ProductReadListDto> productReadListDtos =
                productLikeRepository.findAllByMember(member, member.getBlockUsers(), member.getUserBlockedMe(), productPageable).stream()
                .map(productLike -> modelMapper.map(productLike.getProduct(), ProductReadListDto.class))
                .collect(Collectors.toList());

        // 정렬 중 price 가 있을 경우
        if (pageable.getSort().stream().anyMatch(e->e.getProperty().contentEquals("price"))){
            boolean isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
            boolean isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
            checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);
        };

        return ShopFavoriteListDto.builder()
                .productsList(productReadListDtos)
                .page_size(productReadListDtos.size())
                .total_size(totalSize).build(); // 최종 사이즈
    }



    //***************************************************************************************************************************

    // 카테고리별 물품 조회하기
    // 회원 탈퇴 적용(21.02.26)
    @Override
    public ShopProductListDto getProductsByCategory(UserDetailsImpl userDetails, int categoryId, Pageable pageable) {
        ModelMapper modelMapper = new ModelMapper();
        Comparator<ProductReadListDto> readListDtosComparator = Comparator.comparing((ProductReadListDto o) -> parseStringToInt(o.getPrice()));
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

        List<ProductReadListDto> productReadListDtos = null;
        if( userDetails != null ){
            Member member = memberRepository.findById(userDetails.getId());
            productReadListDtos = productRepository.getProductByCategoryId(categoryId, member.getBlockUsers(), member.getUserBlockedMe(), pageable).stream()
                    .map(product -> modelMapper.map(product, ProductReadListDto.class))
                    .collect(Collectors.toList());
        }else{
            productReadListDtos = productRepository.getProductsByCategoryId(categoryId, pageable).stream()
                    .map(product -> modelMapper.map(product, ProductReadListDto.class))
                    .collect(Collectors.toList());
        }



        // 정렬 중 price 가 있을 경우
        if (pageable.getSort().stream().anyMatch(e->e.getProperty().contentEquals("price"))){
            boolean isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
            boolean isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
            checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);
        };

        if(pageable.getPageSize() == 4){ // 해당 카테고리의 물건 조회시 ( 개별 물건조회 하단 색션 )

            if(userDetails != null){
                Member member = memberRepository.findById(userDetails.getId());
                List<Product> productList = productRepository.getProductByCategoryId(categoryId, member.getBlockUsers(), member.getUserBlockedMe(), null);
                return ShopProductListDto.builder()
                        .total_size(productList.size())
                        .page_size(productReadListDtos.size())
                        .productsList(productReadListDtos).build();
            }else {
                List<Product> productList = productRepository.getProductsByCategoryId(categoryId, null);
                return ShopProductListDto.builder()
                        .total_size(productList.size())
                        .page_size(productReadListDtos.size())
                        .productsList(productReadListDtos).build();
            }

        }

        return ShopProductListDto.builder()
                .page_size(productReadListDtos.size())
                .productsList(productReadListDtos).build();
    }

    // 전체 물건 보기
    // 회원 탈퇴 적용(21.02.26)
    @Override
    public ShopProductListDto getAllProducts(UserDetailsImpl userDetails, Pageable pageable) {
        ModelMapper modelMapper = new ModelMapper();
        Comparator<ProductReadListDto> readListDtosComparator = Comparator.comparing((ProductReadListDto o) -> parseStringToInt(o.getPrice()));

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

        List<Product> products = productRepository.getAllByTransactionStatusIdEquals(0, pageable);
        if(userDetails != null) { // 로그인
            Member member = memberRepository.findById(userDetails.getId());
            products = products
                    .stream()
                    .filter(product -> !(member.getBlockUsers().contains(product.getMember())))
                    .filter(product -> !(member.getUserBlockedMe().contains(product.getMember()))).collect(Collectors.toList());
        }
        List<ProductReadListDto> productReadListDtos = products
                    .stream()
                    .map(product -> modelMapper.map(product, ProductReadListDto.class))
                    .collect(Collectors.toList());

        // 정렬 중 price 가 있을 경우
        if (pageable.getSort().stream().anyMatch(e->e.getProperty().contentEquals("price"))){
            boolean isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
            boolean isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
            checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);
        };

        return ShopProductListDto.builder()
                .page_size(productReadListDtos.size())
                .productsList(productReadListDtos)
                .build();
    }

    // 물건 정보 보기
    @Override
    public ProductReadOneDto getProductInfo(UserDetailsImpl userDetails, int productId) throws CustomControllerExecption {
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomControllerExecption("존재하지 유저입니다.", HttpStatus.NOT_FOUND));
        if(product.getProductStatus() == 1){
            throw new CustomControllerExecption("삭제된 물건입니다.",HttpStatus.NOT_FOUND);
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
            }
        };
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(propertyMap);

        if(userDetails != null){
            // 차단된 사용자의 물건에 접속시 예외처리
            Member member = memberRepository.findById(userDetails.getId());
            if(member.getBlockUsers().contains(product.getMember()) || member.getUserBlockedMe().contains(product.getMember())){
                throw new CustomControllerExecption("차단한 유저 혹은 차단된 유저의 물건에 접근할 수 없습니다.", HttpStatus.GONE);
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
        Product product = productRepository.findById(likeRequest.getProduct_id()).orElseThrow(() -> new CustomControllerExecption("not found result", HttpStatus.NOT_FOUND)); // 좋음! noSuch 디테일 구분 ?

        if(product.getProductStatus() == 1){ // 삭제 예외
            throw new CustomControllerExecption("해당 게시물은 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }

        // 차단된 사용자의 물건에 좋아요 할경우 예외처리
        if(member.getBlockUsers().contains(product.getMember()) || member.getUserBlockedMe().contains(product.getMember())){
            throw new CustomControllerExecption("차단한 유저 혹은 차단된 유저의 물건에 접근할 수 없습니다.", HttpStatus.GONE);
        }

        if(likeRequest.getCurrent_like_status().equals("nolike")){
            member.like(product); // 좋아요
            return "like";
        }else {
            member.unlike(product); // 좋아요 취소
            return "nolike";
        }
    }

    // sort 중 price 가 있을 경우 정렬함 (price string + 원화 가 포함되어있어 어쩔 수 없다)
    private void checkPriceDescAsc(List<ProductReadListDto> productReadListDtos, Comparator<ProductReadListDto> readListDtosComparator, boolean isPriceDesc, boolean isPriceAsc) {
        if(isPriceAsc){ // 저가순
            productReadListDtos.sort(readListDtosComparator);
            productReadListDtos.forEach(e -> log.info("after : {}", e.getPrice()));
        }else if(isPriceDesc){ // 고가순
            productReadListDtos.sort(readListDtosComparator.reversed());
            productReadListDtos.forEach(e -> log.info("after : {}", e.getPrice()));
        }
    }


    public static int parseStringToInt(String priceString){
        priceString = priceString.replaceAll("￦|,", ""); //remove commas
        return (int)Math.round(Double.parseDouble(priceString)); //return rounded double cast to int
    }
    // 유저 탈퇴 / 물건 삭제 체크
    public static void globalCheck(Product product) throws CustomControllerExecption {

        // 탈퇴체크
        if(product.getMember().getIsWithdrawn() == 1){
            throw new CustomControllerExecption("탈퇴한 유저의 물건은 조회할 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 삭제된 물건
        if(product.getProductStatus() == 1){
            throw new CustomControllerExecption("해당 물건은 삭제되었습니다.", HttpStatus.NOT_FOUND);
        }

    }
}
