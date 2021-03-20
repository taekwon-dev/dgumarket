package com.springboot.dgumarket.repository.product;


import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.member.QMember;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.model.product.QProductReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

import static com.springboot.dgumarket.model.product.QProduct.product;
import static com.springboot.dgumarket.model.product.QProductLike.productLike;
import static com.springboot.dgumarket.model.product.QProductReview.productReview;

@Slf4j
@Repository
public class ProductRepositoryImpl extends QuerydslRepositorySupport implements CustomProductRepository{

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
                    .and(eqProductSet(productSet))); // 크게, 전체, 판매중, 판매완료
       if(loginMember != null){ // 로그인 한 유저의 경우, 차단고려(나오면 안됨)
           query.where(notContainBlocks(loginMember, product.member));
       }

       if(exceptProduct != null && pageable.getPageSize()==4){ // 상세페이지의 해당 유저의 다른 물건보기
           log.info("상세페이지의 해당유저의 다른 물건보기 물건 : {} ", exceptProduct.getId());
           query.where(product.ne(exceptProduct));
//           query.where(product.ne(exceptProduct)); // 상세물건페이지에서 보고 있는 물건 제외
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
            if(order.getProperty().equals("createDatetime") && order.getDirection().isDescending()){
                query.orderBy(productReview.ReviewRegistrationDate.desc());
            }else if(order.getProperty().equals("createDatetime") && order.getDirection().isAscending()){
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
                    .and(product.productStatus.eq(0)
                    .and(product.member.isEnabled.eq(0)
                    .and(product.member.isWithdrawn.eq(0))
                    .and(notContainBlocks(loginMember, product.member)))));
        long totalCount = query.fetchCount();
        List<Product> products = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<>(products, pageable, totalCount);
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
            if(order.getProperty().equals("createDatetime") && order.getDirection().isAscending()){
                query.orderBy(productReview.createdDate.asc());
            }else if(order.getProperty().equals("createDatetime") && order.getDirection().isDescending()){
                query.orderBy(productReview.createdDate.desc());
            }
        }
        QueryResults<ProductReview> queryResults = query.fetchResults();
        long totalCount = query.fetchCount(); // 전체개수
        return new PageImpl<>(queryResults.getResults(), pageable, totalCount);
    }

    @Override
    public List<Product> findIndexProductsByCategory(Member loginMember, ProductCategory productCategory) {
        log.info("loginMember : {}", loginMember.getId());
        log.info("productCategory : {}", productCategory.getCategoryName());
        JPQLQuery<Product> query = from(product)
                            .where(
                                    product.productCategory.eq(productCategory),
                                    product.transactionStatusId.eq(0), // 현재 판매중인 상품만
                                    product.productStatus.eq(0), // 상품 삭제, 블라인드 x
                                    product.member.isEnabled.eq(0), // 유저제재 x
                                    product.member.isWithdrawn.eq(0) // 탈퇴
                            ).limit(4).orderBy(product.createDatetime.desc()); // 업로드 최신순
        if(loginMember != null){ // 로그인한 경우
            query.where(notContainBlocks(loginMember, product.member)); // 개별 물건들중 내가 차단하거나/차단당한 유저의 물건은 제외
        }

        List<Product> products = query.fetch();
        products.forEach(e->log.info("productid : {}, productcate : {}", e.getId(), e.getProductCategory().getCategoryName()));
        return query.fetch();
    }

    // 차단하거나 차단당한 것들은 제외(로그인, 공통)
    private BooleanExpression notContainBlocks(Member member, QMember qMember){
        if(member.getBlockUsers().size()==0 && member.getUserBlockedMe().size()==0){
            return null;
        }else if(member.getBlockUsers().size() != 0 && member.getUserBlockedMe().size() ==0){
            return qMember.notIn(member.getBlockUsers());
        }else if(member.getBlockUsers().size() == 0 && member.getUserBlockedMe().size() != 0){
            return qMember.notIn(member.getUserBlockedMe());
        }else if(member.getBlockUsers().size()!=0 && member.getUserBlockedMe().size()!=0){
            return qMember.notIn(member.getBlockUsers())
                    .and(qMember.notIn(member.getUserBlockedMe()));
        }else {
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

}
