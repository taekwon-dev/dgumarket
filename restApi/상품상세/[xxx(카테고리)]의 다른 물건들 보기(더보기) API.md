# 해당 물건 카테고리의 다른 물건들 조회하기 (개별물건정보 밑에 있는 섹션 2)

* 인증여부 : 불필요

해당 물건 카테고리의 다른 물건들을 조회하기 위해서는 '해당 물건의 고유 카테고리 아이디' 가 필요하다. '해당 물건의 고유 카테고리 아이디' 는 개별물건정보조회API 요청시 응답 response body 안에 `categoryId(해당 물건의 카테고리 아이디)`가 있다. 이 값을 이용해 해당물건카테고리의 다른 물건들을 조회하는 API 경로를 만들고 서버로 요청하게 된다.

---
### 결론
최초로 1. 개별물건정보조회( /api/product/{product}/info ) 요청을 통해 데이터를 가져오게 되면 여러 값들 중 `categoryId` 값을 받게 된다. 그리고 받은 `categoryId` 를 이용해 2. 해당 물건 카테고리의 다른 최신물건들을 가져오게 된다.

현재 productId 개별 물건정보 페이지에서 보고있는 현재 물건 고유 번호를 except_pid의 값으로 활용하여 현재보고있는 물건을 제외하고 해당카테고리의 다른물건들을 보여줄 수 있도록 한다.

`/api/category/{categoryId}/products?size=4&except_pid={productId}`



### request param(path, url)

`/api/category/{{categoryId}}/products?size=4&except_pid={productId}`

* `{categoryId}` :카테고리 아이디 ( 이 카테고리아이디는 개별물건정보 조회 요청을 통해 받은 결과값 중 `categoryId` 를 가져와 활용한다.)

* `except_pid` : 현재의 물건번호({productId}), 즉 물건상세페이지에서 참조하고 있는 고유의 물건번호. 이렇게 또 파라미터를 주는 이유는 똑같은 물건을 보여주지 않기 위해서 이다

* `size` : 4 (최대 가져오는 물건개수, 이 값은 무조건 4이어야함)

--- 

### response body

`message` : 응답메시지

`status` : 응답코드

`data` : 물건리스트 정보

---
`total_size` : 해당 카테고리의 전체 물건 개수

`page_size` : 가져오는 데이터 크기

`productsList` (json oject array): 물건리스트

---
`productsList`

개별 물건들(json obejct) 에 대한 json 필드 설명

`id` : 물건아이디( 이용 해당 물건페이지로 이동할 수 있도록 한다. )

`thumbnailImg` : 물건 썸네일 이미지 경로

`title` : 물건제목

`price` : 물건가격

`likeNums` : 좋아요 수

`chatroomNums` : 채팅방 수

`lastUpdateDatetime` : 물건 수정 시간

`uploadDatetime` : 물건 업로드 시간

`transaction_status_id` : 물건 거래상태(0: 판매중, 2:거래완료)



### example response

```json

{
    "message": "뷰티 조회",
    "status": 200,
    "data": {
        "total_size": 10,
        "page_size": 4,
        "productsList": [
            {
                "id": 21,
                "thumbnailImg": "https://dgu-springboot-build.s3.ap-northeast-2.amazonaws.com/sample/124095805_1_1604844130_w292.jpg",
                "title": "뷰티_10",
                "price": "￦21,000",
                "likeNums": 1,
                "chatroomNums": 0,
                "lastUpdatedDatetime": "2021-01-02T13:38:50",
                "uploadDatetime": "2021-01-02T13:38:02",
                "transaction_status_id": 0
            },
            {
                "id": 20,
                "thumbnailImg": "https://dgu-springboot-build.s3.ap-northeast-2.amazonaws.com/sample/138720282_1_1611149403_w292.jpg",
                "title": "뷰티_9",
                "price": "￦20,000",
                "likeNums": 1,
                "chatroomNums": 0,
                "lastUpdatedDatetime": "2021-02-19T17:31:20",
                "uploadDatetime": "2021-01-01T13:37:57",
                "transaction_status_id": 0
            },
            {
                "id": 19,
                "thumbnailImg": "https://dgu-springboot-build.s3.ap-northeast-2.amazonaws.com/sample/143023655_1_1610617308_w292.jpg",
                "title": "뷰티_8",
                "price": "￦19,000",
                "likeNums": 1,
                "chatroomNums": 0,
                "lastUpdatedDatetime": "2021-01-02T13:38:02",
                "uploadDatetime": "2020-12-31T13:38:02",
                "transaction_status_id": 0
            },
            {
                "id": 18,
                "thumbnailImg": "https://dgu-springboot-build.s3.ap-northeast-2.amazonaws.com/sample/143156085_1_1611383813_w292.jpg",
                "title": "뷰티_7",
                "price": "￦18,000",
                "likeNums": 1,
                "chatroomNums": 0,
                "lastUpdatedDatetime": "2021-01-02T13:37:57",
                "uploadDatetime": "2020-12-30T13:38:02",
                "transaction_status_id": 0
            }
        ]
    }
}


```