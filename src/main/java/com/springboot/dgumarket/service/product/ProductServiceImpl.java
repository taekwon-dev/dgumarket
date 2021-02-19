package com.springboot.dgumarket.service.product;

import com.springboot.dgumarket.controller.product.LogExecutionTime;
import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.product.ProductReadListDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductCategoryRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ModelMapper modelMapper, MemberRepository memberRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.modelMapper = modelMapper;
        this.memberRepository = memberRepository;
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
                map().setTransaction_status_id(source.getTransactionStatusId());
                when(Conditions.isNull()).skip().setLastUpdatedDatetime(source.getUpdateDatetime());
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

    //***************************************************************************************************************************


    // 카테고리별 물품 조회하기 (비로그인)
    @Override
    public ShopProductListDto getCategoryProductsNotLoggedIn(int categoryId, Pageable pageable) {
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


        List<ProductReadListDto> productReadListDtos = productRepository.getProductsByCategoryId(categoryId, pageable)
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
                .productsList(productReadListDtos).build();
    }

    // 카테고리별 물품 조회하기 (로그인)
    @Override
    public ShopProductListDto getCategoryProductsLoggedIn(UserDetailsImpl userDetails, int categoryId, Pageable pageable) {
        Member member = memberRepository.findById(userDetails.getId());
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


        // 차단한 유저(or 나를 차단한 유저)의 물건을 보여주지 않는다.
        List<ProductReadListDto> productReadListDtos = productRepository.getProductsByCategoryId(categoryId, pageable)
                .stream()
                .filter(product -> !(member.getBlockUsers().contains(product.getMember())))
                .filter(product -> !(member.getUserBlockedMe().contains(product.getMember())))
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

    // 물건 전체보기 (비로그인)
    @Override
    public ShopProductListDto getAllProducts(Pageable pageable) {
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


        List<ProductReadListDto> productReadListDtos = productRepository.getAllByTransactionStatusIdEquals(0, pageable)
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

    // 물건 전체보기 (로그인)
    @Override
    public ShopProductListDto getAllProducts(UserDetailsImpl userDetails, Pageable pageable) {
        Member member = memberRepository.findById(userDetails.getId());
        log.info("유저아이디 : {}", member.getId());
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

        // 차단한 유저(or 나를 차단한 유저)의 물건을 보여주지 않는다.
        List<ProductReadListDto> productReadListDtos = productRepository.getAllByTransactionStatusIdEquals(0, pageable)
                .stream()
                .filter(product -> !(member.getBlockUsers().contains(product.getMember())))
                .filter(product -> !(member.getUserBlockedMe().contains(product.getMember())))
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
