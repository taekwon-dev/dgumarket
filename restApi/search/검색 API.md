## 검색 API

### 

상품 타이틀, 정보 중 **카테고리 + 검색 키워드** 를 포함하는 경우 상품 리스트를 반환합니다. 



**URL** : `/api/product/search`

**Method** : `GET`

**Authentication** : 인증 선택 (로그인 여부에 따라 검색 결과에 차이가 있을 수 있다.)

**Request Params** : 

`category` : 카테고리 ID

전체 "" 

***(전체 카테고리 + "태권" 검색한 경우 : /api/product/search?category=&q=태권)***

도서 1
음반/DVD 2
뷰티 3
기프티콘 4
가전/디지털 5
식품 6
완구/취미 7
주방용품 8
생활용품 9
홈 인테리어 10
스포츠/레저 11
반려동물용품 12
문구/오피스 13
의류/잡화 14
기타 15

`q` : 검색 키워드 

`sort` 정렬 옵션 

___

**Response**

```json
[HTTP/1.1 200 OK]
// 검색 결과 조회 성공 
{
    "statusCode": 200,
    "message": "카테고리 고유 ID : '' 검색 키워드 : '태' 에 대한 검색 결과",
    "responseData": {
        "total_size": 0, // 해당 카테고리, 키워드에 포함된 전체 상품 수 
        "page_size": 0,  // 페이징 처리 (최초 검색화면에서 출력될 상품 수)
        "productsList": [ // 검색 결과 조회된 상품 리스트 
            {
                "id": 0,
                "thumbnailImg": "이미지 저장 경로",
                "title": "상품 타이틀",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "lastUpdatedDatetime": "2021-04-03T13:42:09",
                "uploadDatetime": "2021-04-03T13:39:31",
                "transaction_status_id": 0
            },
            {
                "id": 0,
                "thumbnailImg": "이미지 저장 경로",
                "title": "상품 타이틀",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "lastUpdatedDatetime": "2021-04-01T09:16:02",
                "uploadDatetime": "2021-04-01T09:16:02",
                "transaction_status_id": 0
            }
        ]
    }
}

// 예외처리 
// 임의로 URL를 수정 후 검색하는 경우 

1. Params를 임의로 수정하는 경우 : 에러 페이지 반환 

* 에러 페이지 (HTML) 파일은 `dgumarket` 서버에서 반환한다. 아래 그림 1 참조 

ex) /api/product/search?categoryyy=&q=검색 : category -> categoryyy 로 임의로 수정하는 경우 
ex) /api/product/search?category=&qqqq=검색 : q -> qqqq 로 임의로 수정하는 경우 
ex) /api/product/search?category=&q=검색&sort=price,desc : '민식이 해결하면 추가할 부분'

2. 지정된 카테고리 범위 밖의 수를 입력하거나, 문자(열)를 입력한 경우 : 에러 페이지 반환 

ex) /api/prouct/search?category=0&q=검색 : category 1~15 범위에 포함되지 않은 경우 
ex) /api/product/search?category=문자열&q=검색 : category에 문자(열)을 입력한 경우 



```

![image-20210406183245607](/Users/youn/Library/Application Support/typora-user-images/image-20210406183245607.png)

​						**<그림 1 : 에러 페이지 디렉토리 on dgumarket 서버>** 





![image-20210406185402797](/Users/youn/Library/Application Support/typora-user-images/image-20210406185402797.png)