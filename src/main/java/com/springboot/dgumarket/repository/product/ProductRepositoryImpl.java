package com.springboot.dgumarket.repository.product;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPQLQuery;

import com.springboot.dgumarket.exception.ErrorMessage;

import com.springboot.dgumarket.exception.notFoundException.ResultNotFoundException;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;

import com.springboot.dgumarket.model.member.QMember;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.model.product.QProductReview;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;


import java.util.*;

import static com.springboot.dgumarket.model.product.QProduct.product;
import static com.springboot.dgumarket.model.product.QProductLike.productLike;
import static com.springboot.dgumarket.model.product.QProductReview.productReview;

@Slf4j
@Repository
public class ProductRepositoryImpl extends QuerydslRepositorySupport implements CustomProductRepository{

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    public ProductRepositoryImpl() {
        super(Product.class); // domain class
    }
    // 전체 물건리스트조회
    @Override
    public PageImpl<Product> findAllPaging(Member loginMember, Pageable pageable) {
        // 테스트
        // select *, regexp_replace(price, '^￦|,', '') as "price" from product order by CAST(regexp_replace(price, '^￦|,', '') as DECIMAL) DESC

        JPQLQuery query = from(product);
        query.where(product.member.isWithdrawn.eq(0) // 물건올린유저가 탈퇴가 아닌 유저만
                .and(product.member.isEnabled.eq(0) //물건올린유저가 삭제,제재상태 아닌 유저만
                .and(product.transactionStatusId.eq(0) // 물건거래상태 판매중만
                .and(product.productStatus.eq(0))))); // 올라가 있는 물건만
        if(loginMember != null) { // 로그인할경우(조건추가)
            query.where(notContainBlocks(loginMember, product.member)); // 물건들 중 내가 차단하거나/차단당한유저가 올린 물건제외
        }

        for (Sort.Order order : pageable.getSort()) { // 물건의 경우 cast 과정을 거친다.
            if(order.getProperty().equals("price")){ // price 의 경우에는 order by 과정에서 cast 과정을 거친다.
                PathBuilder orderByExpression = new PathBuilder(Product.class, "product");
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, orderByExpression.getString(order.getProperty()).castToNum(Integer.class)));
            }
        }

        long totalCount = query.fetchCount();
        List products = Objects.requireNonNull(getQuerydsl()).applyPagination(pageable, query).fetch();
        return new PageImpl<>(products, pageable, totalCount);
    }

    // 전체 카테고리별 물건조회
    @Override
    public PageImpl<Product> findAllPagingByCategory(Member loginMember, int categoryId, Pageable pageable, Product exceptProduct) {
        JPQLQuery query = from(product);
        // products?size=4&product_set=sale&except_pid={{productId}}
        query.where(product.member.isWithdrawn.eq(0) // 물건올린유저가 탈퇴가 아닌 유저만
                .and(product.member.isEnabled.eq(0) //물건올린유저가 삭제,제재상태 아닌 유저만
                .and(product.transactionStatusId.eq(0) // 물건거래상태 판매중만
                .and(product.productStatus.eq(0)) // 올라가 있는 물건만
                .and(product.productCategory.id.eq(categoryId))))); // 지정된 카테고리만
        if(loginMember != null) { // 로그인할경우(조건추가)
            query.where(notContainBlocks(loginMember, product.member)); // 물건들 중 내가 차단하거나/차단당한유저가 올린 물건제외
        }

        for (Sort.Order order : pageable.getSort()) {
            if(order.getProperty().equals("price")){ // price(저가/고가 정렬) 의 경우에는 order by 과정에서 cast 과정을 거친다.
                PathBuilder orderByExpression = new PathBuilder(Product.class, "product");
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, orderByExpression.getString(order.getProperty()).castToNum(Integer.class)));
            }
        }

        if(exceptProduct != null && pageable.getPageSize()==4){ // 상세페이지의 해당 유저의 다른 물건보기
            query.where(product.ne(exceptProduct)); // 상세물건페이지에서 보고 있는 물건 제외
        }
        long totalCount = query.fetchCount();
        List products = Objects.requireNonNull(getQuerydsl()).applyPagination(pageable, query).fetch();
        return new PageImpl<>(products, pageable, totalCount);
    }

    //------------------------------------------------------ 유저 판매물건 정보 --------------------------------------------------------------

    // 유저의 판매물품조회
    @Override
    public PageImpl<Product> findUserProducts(Member loginMember, Member user, String productSet, Pageable pageable, Product exceptProduct) {
        // 다양한 조건이 있음.
       JPQLQuery<Product> query =  from(product); // 물건 등 중에서
       query.where(product.productStatus.eq(0) // 판매중인 것들만
                    .and(product.member.eq(user)) // 특정 유저의 물건들만
                    .and(eqProductSet(productSet))) // 전체, 판매중, 판매완료
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());
       if(loginMember != null){ // 로그인 한 유저의 경우, 차단고려(나오면 안됨)
           query.where(notContainBlocks(loginMember, product.member));
       }
        for (Sort.Order order : pageable.getSort()) { // 물건의 경우 cast 과정을 거친다.
            if(order.getProperty().equals("price")){ // price 의 경우에는 order by 과정에서 cast 과정을 거친다.
                PathBuilder orderByExpression = new PathBuilder(Product.class, "product");
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, orderByExpression.getString(order.getProperty()).castToNum(Integer.class)));
            }
        }


       if(exceptProduct != null && pageable.getPageSize()==4){ // 상세페이지의 해당 유저의 다른 물건보기
           log.info("상세페이지의 해당유저의 다른 물건보기 물건 : {} ", exceptProduct.getId());
           query.where(product.ne(exceptProduct));
       }
       long totalCount = query.fetchCount();
       List products = getQuerydsl().applyPagination(pageable, query).fetch();
       return new PageImpl<>(products, pageable, totalCount);
    }

    // 유저의 구매리뷰들 조회
    @Override
    public PageImpl<ProductReview> findAllReviews(Member loginMember, Member user, Pageable pageable) {
        QProductReview productReview = QProductReview.productReview;
        JPQLQuery<ProductReview> query = from(productReview);
        query.where(productReview.seller.eq(user) // 조회대상의 사람
                    .and(productReview.reviewMessage.isNotNull()) // 메시지 있는 것들만
                    .and(productReview.consumer.isEnabled.eq(0) // 이용제재 X
                    .and(productReview.consumer.isWithdrawn.eq(0)) // 탈퇴 X
                    ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        if(loginMember != null){
            query.where(notContainBlocks(loginMember, productReview.consumer)); // 리뷰 남긴 사람들 중 내(로그인한 유저)가 차단하거나/차단당한 유저의 리뷰글이 있을 경우 제외한다.
        }
        long totalCount = query.fetchCount();
        // 리뷰 최신순 / 오래된 순
        for (Sort.Order order : pageable.getSort()) {
            if(order.getProperty().equals("ReviewRegistrationDate") && order.getDirection().isDescending()){
                query.orderBy(productReview.ReviewRegistrationDate.desc());
            }else if(order.getProperty().equals("ReviewRegistrationDate") && order.getDirection().isAscending()){
                query.orderBy(productReview.ReviewRegistrationDate.asc());
            }
        }
        QueryResults<ProductReview> productReviews = query.fetchResults();
        return new PageImpl<>(productReviews.getResults(), pageable, totalCount);
    }

    // 유저관심물건들 가져오기
    @Override
    public PageImpl<Product> findAllFavoriteProducts(Member loginMember, Pageable pageable) {
//        select product.* from product inner join product_like pl on product.id = pl.product_id and pl.user_id=1;
        JPQLQuery<Product> query = from(product);
        query.join(productLike).on(product.id.eq(productLike.product.id))
                .where(productLike.member.eq(loginMember)
                    .and(product.productStatus.eq(0) // 삭제, 비공개 처리 안되고
                    .and(product.member.isEnabled.eq(0) // 해당 물건의 소유자가 유저제재처리도 안되었고
                    .and(product.member.isWithdrawn.eq(0)) // 해당 물건의 소유자가 탈퇴도 안되였고
                    .and(notContainBlocks(loginMember, product.member))))) // 해당 물건의 소유자가 나와 차단관계도 아닌경우
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 관심물건중 물건 업로드 최신순 / 오래된 순 / 가격순
        for (Sort.Order order : pageable.getSort()) {
            if(order.getProperty().equals("createDatetime") && order.getDirection().isDescending()){
                query.orderBy(product.createDatetime.desc());
            }else if(order.getProperty().equals("createDatetime") && order.getDirection().isAscending()){
                query.orderBy(product.createDatetime.asc());
            }else if(order.getProperty().equals("price")){ // price 의 경우에는 order by 과정에서 cast 과정을 거친다.
                PathBuilder orderByExpression = new PathBuilder(Product.class, "product");
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, orderByExpression.getString(order.getProperty()).castToNum(Integer.class)));
            }else if(order.getProperty().equals("likeNums")){
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, product.likeNums));
            }else if(order.getProperty().equals("chatroomNums")){
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, product.chatroomNums));
            }
        }

        long totalCount = query.fetchCount();
//        List products = getQuerydsl().applyPagination(pageable, query).fetch();
        QueryResults<Product> productLikes = query.fetchResults();
        return new PageImpl<>(productLikes.getResults(), pageable, totalCount);
    }

    // 유저구매물건들 조회(물건삭제/블라인드처리/상대방차단/상대방제재조치/상대방탈퇴)
    @Override
    public PageImpl<ProductReview> findUserPurchases(Member loginMember, String purchase_set, Pageable pageable) {
//        select * from product as p inner join product_review pr on p.id = pr.product_id where pr.seller_id = 1 and pr.review_message is not null;
        pageable.getSort().stream().forEach(e -> log.info("유저 구매물건 : {}", e.getProperty()));
        JPQLQuery<ProductReview> query = from(productReview)
                                        .where(productReview.consumer.eq(loginMember)
                                            .and(eqPurchaseSet(purchase_set)) // 전제,후기쓴것,후기안쓴것
                                            .and(productReview.product.productStatus.eq(0)) // 물건삭제,블라인드처리 제외
                                            .and(productReview.seller.isEnabled.eq(0)) // 상대방제재조치 제외
                                            .and(productReview.seller.isWithdrawn.eq(0)) // 상대방탈퇴 제외
                                            .and(notContainBlocks(loginMember, productReview.product.member))) // 차단된것 보여주지 않기
                                            .offset(pageable.getOffset())
                                            .limit(pageable.getPageSize());
        // 정렬
        for (Sort.Order order : pageable.getSort()) {
            if(order.getProperty().equals("createDatetime") && order.getDirection().isAscending()){ // 거래완료 최신순
                query.orderBy(productReview.createdDate.asc());
            }else if(order.getProperty().equals("createDatetime") && order.getDirection().isDescending()){ // 거래완료 오래된 순
                query.orderBy(productReview.createdDate.desc());
            }
        }
        QueryResults<ProductReview> queryResults = query.fetchResults();
        long totalCount = query.fetchCount(); // 전체개수
        return new PageImpl<>(queryResults.getResults(), pageable, totalCount);
    }

    // (로그인)유저의 관심 카테고리 별 물건 조회
    @Override
    public List<Product> findIndexProductsByCategoryLogin(Member loginMember, ProductCategory productCategory) {
        JPQLQuery<Product> query = from(product)
                            .where(
                                    product.productCategory.eq(productCategory),
                                    product.transactionStatusId.eq(0), // 현재 판매중인 상품만
                                    product.productStatus.eq(0), // 상품 삭제, 블라인드 x
                                    product.member.isEnabled.eq(0), // 유저제재 x
                                    product.member.isWithdrawn.eq(0) // 탈퇴
                            ).limit(4).orderBy(product.createDatetime.desc()); // 업로드 최신순
        if(loginMember != null){ // 로그인한 경우
            log.info("login : {}",loginMember.getId());
            query.where(notContainBlocks(loginMember, product.member)); // 개별 물건들중 내가 차단하거나/차단당한 유저의 물건은 제외
        }
        QueryResults<Product> productQueryResults = query.fetchResults();
        return productQueryResults.getResults();
    }

    // (비로그인) 인기카테고리 물건조회하기
    @Override
    public List<Product> findIndexProductsByCategory(ProductCategory productCategory) {
        JPQLQuery<Product> query = from(product)
                .where(
                        product.productCategory.eq(productCategory),
                        product.transactionStatusId.eq(0), // 현재 판매중인 상품만
                        product.productStatus.eq(0), // 상품 삭제, 블라인드 x
                        product.member.isEnabled.eq(0), // 유저제재 x
                        product.member.isWithdrawn.eq(0) // 탈퇴
                ).limit(4).orderBy(product.createDatetime.desc()); // 업로드 최신순
        return query.fetch();
    }

    // 검색 결과 조회


    @Override
    public PageImpl<Product> findAllPagingBySearch(Member loginMember, Pageable pageable, String categoryId, String keyword) {

        if (categoryId == null || keyword == null || keyword.trim().length() == 0) {
            // [예외처리] 카테고리, 키워드 @RequestParam을 임의로 수정한 경우 예외처리
            // [예외처리] 빈 값을 검색하는 경우 예외처리 (클라 측에서도 공란 상태로 검색할 수 없도록 처리)
            throw new ResultNotFoundException(errorResponse("[0]요청에 대한 결과를 조회할 수 없는 경우", 307, "/api/product/search"));
        }


        JPQLQuery query = from(product).where(
                product.member.isWithdrawn.eq(0)                // 상품을 업로드한 유저가 탈퇴 상태가 아닌 경우
                .and(product.member.isEnabled.eq(0)             // 상품을 업로드한 유저가 이용제재 대상이 아닌 경우
                .and(product.transactionStatusId.eq(0)          // 업로드 된 상품이 거래 중인 경우
                .and(product.productStatus.eq(0)                // 업로드 된 상품이 삭제되지 않은 경우
                .and((product.title.contains(keyword).or(product.information.contains(keyword)))))))) // 업로드 된 상품의 타이틀 또는 설명 내용 중 유저가 검색한 키워드를 포함하는 경우
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());


        // 검색 시 카테고리 값을 따로 지정하지 않은 경우 -> '전체' 카테고리에서 검색하는 상황이고, 쿼리에 추가적인 조건이 붙지 않는다.
        try {
            // categoryId = "" 인 경우 -> 전체 카테고리
            if (categoryId != "") {
                // category params에 값이 있는 경우
                if ((Integer.parseInt(categoryId) < 16 && Integer.parseInt(categoryId) > 0)) {
                    // categoryId 1~15인 경우,
                    query.where(product.productCategory.id.eq(Integer.parseInt(categoryId)));
                } else {
                    log.error("주어진 카테고리 범위 밖에서 요청하는 경우");
                    // [예외처리] 카테고리가 지정된 범위 밖으로 임의로 수정한 경우
                    throw new ResultNotFoundException(errorResponse("[1]요청에 대한 결과를 조회할 수 없는 경우", 307, "/api/product/search"));

                }
            }
        } catch (NumberFormatException e) {
            // from parseInt() method
            throw new ResultNotFoundException(errorResponse("[2]요청에 대한 결과를 조회할 수 없는 경우", 307, "/api/product/search"));


        }


        // 로그인 상태로 검색하는 경우 -> 추가 조건 (via notContainBlocks) :
        // 1. 로그인 한 유저가(검색 주체) 차단한 유저의 상품을 제외
        // 2. 로그인 한 유저(검색 주체)를 차단한 유저의 상품을 제외
        if (loginMember != null) {
            query.where(notContainBlocks(loginMember, product.member));
        }

        for (Sort.Order order : pageable.getSort()) {

            // 가격 순 ( 고가순, 저가순 ) 정렬이 varchar 타입으로 지정된 상황이므로
            // order by 조건을 적용하기 위해 integer로 casting 작업 추가
            if(order.getProperty().equals("price")){
                PathBuilder orderByExpression = new PathBuilder(Product.class, "product");
                query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, orderByExpression.getString(order.getProperty()).castToNum(Integer.class)));
            }
        }
        long totalCount = query.fetchCount();
        List products = Objects.requireNonNull(getQuerydsl()).applyPagination(pageable, query).fetch();
        return new PageImpl<>(products, pageable, totalCount);
    }

    // 차단하거나 차단당한 것들은 제외(로그인, 공통)
    private BooleanExpression notContainBlocks(Member member, QMember qMember){

        // 내가 차단한 + 나를 차단한 리스트를 저장할 HashSet 초기화
        Set<Member> members = new HashSet<>();

        // 내가 차단한 경우 + 나를 차단한 전체 경우의 수를 담고 있는 리스트
        // [user_block] 테이블에서
        // user_id = 로그인 유저 고유 ID
        // OR
        // blocked_user_id = 로그인 유저 고유 ID
        List<BlockUser> blockUserList = blockUserRepository.findByUserOrBlockedUser(member, member);

        // for Loop : 유저가 차단한 리스트, 유저를 차단한 리스트 대상으로
        // 상품 정보에서 제외될 유저 리스트 생성
        for (int i = 0; i < blockUserList.size(); i++) {

            // 리스트 내부에서 로그인 유저가 차단한 유저를 HashSet<Member>에 추가한다.
            if (blockUserList.get(i).getUser().equals(member)) {

                Member blockedUser = memberRepository.findById(blockUserList.get(i).getBlockedUser().getId());
                // NPE
                if (blockedUser != null) members.add(blockedUser);
            } else {
                // 리스트 내부에 로그인 유저를 차단한 유저를 HashSet<Member>에 추가한다.
                Member blockingMeUser = memberRepository.findById(blockUserList.get(i).getUser().getId());
                // NPE
                if (blockingMeUser != null) members.add(blockingMeUser);
            }
        } // end loop

        // HashSet<Member> -> 상품 조회 시 제외돼야 할 리스트를 선정하는 역할

        if (member.getBlockUsers().size() == 0 && member.getUserBlockedMe().size() == 0){
            return null;

        } else if (member.getBlockUsers().size() != 0 && member.getUserBlockedMe().size() == 0) {
            return qMember.notIn(members);

        } else if (member.getBlockUsers().size() == 0 && member.getUserBlockedMe().size() != 0) {
            return qMember.notIn(members);

        } else if (member.getBlockUsers().size()!= 0 && member.getUserBlockedMe().size() != 0) {
            return qMember.notIn(members);

        } else {
            return null;
        }
    }

    // 구매물건 동적 조건쿼리( 전체, 후기쓴것, 후기안쓴것 )
    private BooleanExpression eqPurchaseSet(String purchaseSet){
        if(purchaseSet == null || purchaseSet.equals("total")){
            return null;
        }else if(purchaseSet.equals("write")){
            return productReview.reviewMessage.isNotNull();
        }else if(purchaseSet.equals("nowrite")){
            return productReview.reviewMessage.isNull();
        }else{
            return null;
        }
    }

    // 판매물건 동적 조건쿼리( 전체, 판매중, 판매완료 )
    private BooleanExpression eqProductSet(String productSet){
        if(productSet == null ||  productSet.equals("total")){ // 아무것도 없을 경우 통과
            log.info("아무것도 없음");
            return null;
        }else if (productSet.equals("sale")){ // 판매중인 물건
            log.info("sale");
            return product.transactionStatusId.eq(0);
        }else if(productSet.equals("sold")){ // 판매완료된 물건만
            log.info("sold");
            return product.transactionStatusId.eq(2);
        }else {
            return null;
        }
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
                    .pathToMove("/exceptions")
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
