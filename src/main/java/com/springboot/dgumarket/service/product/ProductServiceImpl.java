package com.springboot.dgumarket.service.product;


import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.product.ProductReadListDto;
import com.springboot.dgumarket.dto.product.ProductReadOneDto;
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

    @Override
    public String enrollProduct(ProductCreateDto productCreateDto) {



        /*
        클라이언트가 요청한 카테고리 코드 값과 매핑되는 카테고리가 없는 경우 발생하는 에러.
        {
        "statusCode": 500,
        "timestamp": "2020-12-16T09:56:46.436+00:00",
        "message": "could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement",
        "description": "uri=/api/product/create"
        }
        */
        ProductCategory productCategory = productCategoryRepository.findById(productCreateDto.getProductCategory());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
                .transactionStatusId(productCreateDto.getTransactionStatusId()) // 거래 상태 코드
                .transactionModeId(productCreateDto.getTransactionModeId())     // 거래 방식 코드
                .selfProductStatus(productCreateDto.getSelfProductStatus())     // 판매자 자체 상품 상태 평가
                .build();

        productRepository.save(product);
        return product.getTitle();
    }

    // 메인 페이지 (index) : 비로그인 상태로 접속한 경우 -> 인기 카테고리, 카테고리 별 상품 리스트 리턴.
    @Override
    public List<ProductListIndex> indexNotLoggedIn(int lastCategoryId) {

        org.modelmapper.PropertyMap<ProductCategory, ProductCategoryDto> map_category = new PropertyMap<ProductCategory, ProductCategoryDto>() {
            @Override
            protected void configure() {
                map().setCategory_id(source.getId());
                map().setCategory_name(source.getCategoryName());
            }
        };
        modelMapper = new ModelMapper();
        modelMapper.addMappings(map_category);

        Pageable pageable = PageRequest.of(0, 3);
        // Page 타입 -> List, findAllByCategoryTypeOrderByIdAsc(인기카테고리 Flag(= 1), PageRequest)
        List<ProductCategoryDto> popularCategories = productCategoryRepository.findAllByCategoryTypeAndIdGreaterThanOrderByIdAsc(1, lastCategoryId, pageable)
                .stream()
                .map(productCategory -> modelMapper.map(productCategory, ProductCategoryDto.class))
                .collect(Collectors.toList());

        // add Mappings
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

        List<ProductCategory> productCategories = productCategoryRepository.findAllByCategoryTypeAndIdGreaterThanOrderByIdAsc(1, lastCategoryId, pageable).toList();
        List<ProductReadListDto> dtos = productRepository.apiProductIndex(productCategories)
                .stream()
                .map(productList -> modelMapper.map(productList, ProductReadListDto.class))
                .collect(Collectors.toList());

        List<ProductListIndex> productListIndexList = new ArrayList<>();

        popularCategories.stream().forEach(
            category -> {
                List<ProductReadListDto> productReadListDtos = new ArrayList<>();
                // 물건 리스트 스트림
                dtos.stream().forEach(product -> {
                    // 현재 카테고리 id와 동일한 상품만 묶어서 매핑
                    if (category.getCategory_id() == product.getCategory_id()) {
                        productReadListDtos.add(product);
                    }
                });

                productListIndex = ProductListIndex.builder()
                        .category_id(category.getCategory_id())
                        .category_name(category.getCategory_name())
                        .productsList(productReadListDtos)
                        .build();
                productListIndexList.add(productListIndex);
            });
        return productListIndexList;
    }


    // 관심 카테고리 (유저 별)
    @Override
    public List<ProductListIndex> indexLoggedIn(UserDetailsImpl userDetails, int lastCategoryId) {

        // '유저' 별 관심 카테고리를 추출하기 위해 로그인한 유저의 고유 아이디를 가진 Member 객체 생성
        Member member = Member.builder()
                .id(userDetails.getId())
                .build();

        org.modelmapper.PropertyMap<ProductCategory, ProductCategoryDto> map_category = new PropertyMap<ProductCategory, ProductCategoryDto>() {
            @Override
            protected void configure() {
                map().setCategory_id(source.getId());
                map().setCategory_name(source.getCategoryName());
            }
        };
        modelMapper = new ModelMapper();
        modelMapper.addMappings(map_category);

        Pageable pageable = PageRequest.of(0, 3);
        List<ProductCategoryDto> popularCategories = productCategoryRepository.findByMembersAndIdGreaterThanOrderByIdAsc(member,lastCategoryId, pageable)
                .stream()
                .map(productCategory -> modelMapper.map(productCategory, ProductCategoryDto.class))
                .collect(Collectors.toList());
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
        List<ProductCategory> productCategories = productCategoryRepository.findByMembersAndIdGreaterThanOrderByIdAsc(member,lastCategoryId, pageable).toList();
        List<ProductReadListDto> dtos = productRepository.apiProductIndex(productCategories)
                .stream()
                .map(productList -> modelMapper.map(productList, ProductReadListDto.class))
                .collect(Collectors.toList());

        List<ProductListIndex> productListIndexList = new ArrayList<>();

        popularCategories.stream().forEach(
                category -> {
                    List<ProductReadListDto> productReadListDtos = new ArrayList<>();
                    // 물건 리스트 스트림
                    dtos.stream().forEach(product -> {
                        // 현재 카테고리 id와 동일한 상품만 묶어서 매핑
                        if (category.getCategory_id() == product.getCategory_id()) {
                            productReadListDtos.add(product);
                        }
                    });

                    productListIndex = ProductListIndex.builder()
                            .category_id(category.getCategory_id())
                            .category_name(category.getCategory_name())
                            .productsList(productReadListDtos)
                            .build();
                    productListIndexList.add(productListIndex);
                });

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
                .filter(product -> product.getProductStatus() != 1) // 삭제 제외
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
                map().setUploadDatetime(source.getUpdateDatetime());
                map().setLastUpdatedDatetime(source.getUpdateDatetime());
                map().setTransaction_status_id(source.getTransactionStatusId());
                map().setLikeStatus("nolike");
            }
        };
        modelMapper = new ModelMapper();
        modelMapper.addMappings(listDtoPropertyMap);

        // 차단 유저물건 조회 X
        // 삭제 X
        List<ProductReadListDto> productReadListDtos = productLikeRepository.findAllByMember(member, pageable).stream()
                .filter(productLike -> productLike.getProduct().getProductStatus() == 0) // 삭제 제외
                .filter(productLike -> !(member.getBlockUsers().contains(productLike.getProduct().getMember()))) // 좋아요 물건에 차단한 유저의 물품이 있다면 제외
                .filter(productLike -> !(member.getUserBlockedMe().contains(productLike.getProduct().getMember()))) // 좋아요 물건의 유저가 나를 차단했다면 제외
                .map(productLike -> modelMapper.map(productLike.getProduct(), ProductReadListDto.class))
                .collect(Collectors.toList());

        productReadListDtos.forEach(
                e -> {
                    for (Product product : member.getLikeProducts()){
                        if(e.getId() == product.getId()){
                            e.setLikeStatus("like");
                        }
                    }
                });

        // 정렬 중 price 가 있을 경우
        if (pageable.getSort().stream().anyMatch(e->e.getProperty().contentEquals("price"))){
            boolean isPriceDesc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isDescending());
            boolean isPriceAsc = pageable.getSort().stream().anyMatch(e -> e.getProperty().contentEquals("price") && e.getDirection().isAscending());
            checkPriceDescAsc(productReadListDtos, readListDtosComparator, isPriceDesc, isPriceAsc);
        };

        return ShopFavoriteListDto.builder()
                .productsList(productReadListDtos)
                .page_size(productReadListDtos.size())
                .total_size(totalSize).build();
    }
    //***************************************************************************************************************************

    // 카테고리별 물품 조회하기
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

        List<Product> products = productRepository.getProductsByCategoryId(categoryId, pageable);
        if( userDetails != null ){
            Member member = memberRepository.findById(userDetails.getId());
            products = products.stream()
                    .filter(product -> !(member.getBlockUsers().contains(product.getMember())))
                    .filter(product -> !(member.getUserBlockedMe().contains(product.getMember()))).collect(Collectors.toList());
        }
        List<ProductReadListDto> productReadListDtos = products.stream()
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
                .productsList(productReadListDtos).build();
    }

    // 전체 물건 보기
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
        List<ProductReadListDto> productReadListDtos;
        if(userDetails != null) { // 로그인
            Member member = memberRepository.findById(userDetails.getId());
            productReadListDtos = productRepository.getAllByTransactionStatusIdEquals(0, pageable)
                    .stream()
                    .filter(product -> !(member.getBlockUsers().contains(product.getMember())))
                    .filter(product -> !(member.getUserBlockedMe().contains(product.getMember())))
                    .map(product -> modelMapper.map(product, ProductReadListDto.class))
                    .collect(Collectors.toList());
        }else{ // 비로그인
            productReadListDtos = productRepository.getAllByTransactionStatusIdEquals(0, pageable)
                    .stream()
                    .map(product -> modelMapper.map(product, ProductReadListDto.class))
                    .collect(Collectors.toList());
        }

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
        Product product = productRepository.findById(productId).orElse(null);
        assert product != null;

        if(product.getProductStatus() == 1){ // 삭제
            throw new CustomControllerExecption("요청하신 물건은 삭제되었습니다.", HttpStatus.NOT_FOUND);
        }
        PropertyMap<Product, ProductReadOneDto> propertyMap = new PropertyMap<Product, ProductReadOneDto>() {
            @Override
            protected void configure() {
                map().setUserId(source.getMember().getId());
                map().setUserNickName(source.getMember().getNickName());
                map().setProfileImgDirectory(source.getMember().getProfileImageDir());
                map().setIsNego(source.getIsNego());
                map().setTitle(source.getTitle());
                map().setImgDirectories(source.getImgDirectory()); // 추후 보강 필요
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
        Product product = productRepository.findById(likeRequest.getProduct_id()).orElseThrow(() -> new CustomControllerExecption("not found result", HttpStatus.NOT_FOUND)); // 좋음! noSuch

        // 삭제된 물건 예외
        if(product.getProductStatus() == 1){
            throw new CustomControllerExecption("요청하신 물건은 삭제되었습니다.", HttpStatus.NOT_FOUND);
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
}
