package com.springboot.dgumarket.dto.product;

import lombok.Getter;

/**
 * Created by TK YOUN (2020-12-09 오후 5:43)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description : 상품 등록
 *
 * - title : 상품 타이틀
 * - information : 상품 정보
 * - price : 상품 가격
 * - imgDirectory : 이미지 파일명 리스트 (최대 5장), 0번 인덱스에 있는 이미지를 메인 이미지(상품 리스트에 보여지는)로 지정
 * - productCategory : 상품 카테고리 상세 내역입니다. {"id" : "category"}
 * 0 : 도서/음반/DVD
 * 1 : 패션의류/잡화
 * 2 : 뷰티
 * 3 : 가전디지털
 * 4 : 스포츠/레저
 * 5 : 완구/취미
 * 6 : 문구/오피스
 * 7 : 기프티콘
 * 8 : 주방용품
 * 9 : 생활용품
 * 10 : 홈인테리어
 * 11 : 반려동물용품
 * 12 : 헬스/건강식품
 * 13 : 식품
 * 14 : 이용권(헬스장 등)

 * - transactionStatusId : 상품 상태 상세 내역입니다. {"id" : "status"}
 * 0 : 판매중 (Default)
 * 1 : 예약중 (판매자가 특정 구매 의사를 보인 사람과 거래 약속을 잡은 경우, 판매자가 직접 설정)
 * 2 : 판매완료 (거래 완료 후, 판매자가 직접 설정)
 * 3 : 신고처리중 (관리자가 직접 설정)
 * 4 : 삭제 (판매자가 업로드한 게시물을 삭제한 경우)

 * - transactionModeId : 선호 거래방식 상세 내역입니다.{"id" : "mode of transaction"}
 * 0 : 캠퍼스 내 직거래
 * 1 : 거주지역 주변 직거래
 * 2 : 비대면 거래(택배)
 * 3 : 구매자와 조율 가능

 * - selfProductStatus : 판매자 자체 상품 상태 (2021-03 삭제 처리 결정 / 추후 전체 로직에서 이 부분 삭제 예정)
 * 0 : S 등급
 * 1 : A 등급
 * 2 : B 등급
 * 3 : C 등급
 */



@Getter
public class ProductCreateDto {
    private String title;                                                  // 상품 타이틀 (글 제목)
    private String information;                                            // 상품 정보
    private String price;                                                  // 상품 가격 (0원 입력 시 - 무료나눔)
    private String imgDirectory;                                           // 저장된 상품 이미지 디렉토리 (최대 5장)
    private int productCategory;                                           // 상품 카테고리 코드
    private int isNego;                                                    // 가격 조율 가능 여부 (0 : 가능, 1 : 불가능)
    private int transactionStatusId;                                       // 거래 상태 코드
    private int transactionModeId;                                         // 거래 방식 코드
    private int selfProductStatus;                                         // [판매자 자체] 상품 상태 평가
}
