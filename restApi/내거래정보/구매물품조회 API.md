---
REST API - Chat created by MS (21-02-02)
---


### 수정사항(3.25)

이미지가 없을 경우 null로 옵니다.


### 수정사항(4.13)

시간 정렬 키값 변경 createdDate -> createDatetime


# 유저의 구매물품 조회하기

유저가 중고로 팔고있는 물품들을 조회한다.


**URL** : `/api/shop/{user-id}/purchase` 

**Method** : `GET`

**Authentication required** : `yes`

**Request Param** : 

`purchase_set`: 구매물품정렬( 전체(=total), 리뷰작성(=write), 리뷰미작성(=nowrite)) (string)

`page` : 보여줄 페이지, 무한스크롤시 스크롤 최하단에 닿을때마다 page 1씩 늘어남-> 추가적인 정보를 가져옴(number) ( 0 부터 시작 )

`size` : 한 페이지에 보여줄 물건개수(number) (임의로 정할 수 있음)

`sort` : 정렬( 거래완료 최신순(=createDatetime,desc), 거래완료 오래된순(=createDatetime,asc) )  

## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지
purchase products 로 고정

`status`: 응답 상태 

`data`: 유저 물건 정보

​		`total_size`: 해당 영역별 해당되는 총 구매물건 개수( 판매중(purchase_set=write)이라면 전체구매물품 중 리뷰를 작성한 것들의 구매물건 개수)) 

​		`page_size`: 페이지 별 가져오는 크기(한 번에 가져오는 구매물건 개수)

​		`productsList`: 구매물건 리스트[JSON Array]

​		​		 `purchase_product_id`: 구매물건 고유ID
​		​		 `purchase_title`: 구매물건제목
​		​		 `purchase_price`: 구매물건가격
​		​		 `purchase_product_img`: 구매물건 이미지경로 (이미지 없을 경우 null)
​		​		 `purchase_like_num`: 구매물건 좋아요 개수
​		​		 `purchase_chat_num`: 구매물건 채팅 수
​		​		 `purchase_date`: 구매물건 구매날짜
​		​		 `purchase_review_date`: 구매물건 리뷰등록일( 없을경우 : null )
​		​		 `_review`: 구매물건 리뷰 등록 여부(true : 작성, false: 미작성)



**example**

```json

{
    "message": "purchase products",
    "status": 200,
    "data": {
        "total_size": 7,
        "page_size": 7,
        "purchase_product_list": [
            {
                "purchase_product_id": 93,
                "purchase_title": "뷰티_10",
                "purchase_price": "￦21,000",
                "purchase_product_img": "/imgs/slideshow_sample.jpg",
                "purchase_like_num": 15,
                "purchase_chat_num": 22,
                "purchase_date": "2021-01-08T14:29:11",
                "purchase_review_date": null,
                "_review": false
            },
            {
                "purchase_product_id": 91,
                "purchase_title": "뷰티_8",
                "purchase_price": "￦19,000",
                "purchase_product_img": "/imgs/slideshow_sample.jpg",
                "purchase_like_num": 15,
                "purchase_chat_num": 22,
                "purchase_date": "2021-01-11T12:18:23",
                "purchase_review_date": null,
                "_review": false
            },
            {
                "purchase_product_id": 88,
                "purchase_title": "뷰티_5",
                "purchase_price": "￦16,000",
                "purchase_product_img": "/imgs/food1.jpg",
                "purchase_like_num": 12,
                "purchase_chat_num": 2,
                "purchase_date": "2021-01-18T06:55:43",
                "purchase_review_date": "2021-01-18T06:55:43",
                "_review": true
            },
            {
                "purchase_product_id": 90,
                "purchase_title": "뷰티_7",
                "purchase_price": "￦18,000",
                "purchase_product_img": "/imgs/slideshow_sample.jpg",
                "purchase_like_num": 14,
                "purchase_chat_num": 1,
                "purchase_date": "2021-01-24T05:48:15",
                "purchase_review_date": null,
                "_review": false
            },
            {
                "purchase_product_id": 92,
                "purchase_title": "뷰티_9",
                "purchase_price": "￦20,000",
                "purchase_product_img": "/imgs/slideshow_sample.jpg",
                "purchase_like_num": 14,
                "purchase_chat_num": 1,
                "purchase_date": "2021-01-24T05:48:15",
                "purchase_review_date": null,
                "_review": false
            },
            {
                "purchase_product_id": 71,
                "purchase_title": "도서_1",
                "purchase_price": "￦1,000",
                "purchase_product_img": "/imgs/home_goods1.jpeg",
                "purchase_like_num": 1,
                "purchase_chat_num": 2,
                "purchase_date": "2021-02-02T10:52:09",
                "purchase_review_date": "2021-02-02T10:52:09",
                "_review": true
            },
            {
                "purchase_product_id": 89,
                "purchase_title": "뷰티_6",
                "purchase_price": "￦17,000",
                "purchase_product_img": "/imgs/slideshow_sample.jpg",
                "purchase_like_num": 13,
                "purchase_chat_num": 1,
                "purchase_date": "2021-02-02T10:52:09",
                "purchase_review_date": "2021-02-02T10:52:09",
                "_review": true
            }
        ]
    }
}

```

## Fail Responses

구매물품 목록을 보기위해서 본인이 아닌 다른 유저가 api요청을 보냈을 경우 다음과 같은 에러응답값을 반환한다.

**Code** : `403 Forbidden`

**Content**

`statusCode`: HTTP 상태코드
`timestamp` : 요청시간
`message` : 요청에러이유
`description` : 요청한 URL

**example**

```json

{
    "statusCode": 403,
    "timestamp": "2021-02-04T13:38:34.968+00:00",
    "message": "403 Forbidden, Wrong access",
    "description": "uri=/api/shop/1/purchase"
}

```


