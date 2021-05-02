---
REST API - Chat created by MS (21-02-04)

---


### 수정사항(3.25)

이미지가 없을 경우 null로 옵니다.

### 꼭 읽을 것! (5/2 수정사항 추가)
기존 접근불가능 했던 api에 대해서 예외처리 화면으로 이동한 것을 이제는 /shop/main/index (인덱스페이지)로 이동하기위해 예외 응답 pathToMove 필드 값은 /shop/main/index 로 내려옴


# 유저의 판매물품조회하기

유저가 중고로 팔고있는 물품들을 조회한다.


**URL** : `/api/shop/{user-id}/products` 

**Method** : `GET`

**Authentication required** : `no`

**Request Param** : 

`product_set`: 판매물품정렬(전체(=total), 판매중(=sale), 거래완료(=sold)) (string)

`page` : 보여줄 페이지, 무한스크롤시 스크롤 최하단에 닿을때마다 page 1씩 늘어남-> 추가적인 정보를 가져옴(number) ( 0 부터 시작 )

`size` : 한 페이지에 보여줄 물건개수(number) (임의로 정할 수 있음)

`sort` : 세부정렬(좋아요수(=likeNums), 채팅수(=chatroomNums, 저가순(=price,asc), 고가순(=price,desc), 오래된 순(=createDatetime,asc), 최신 순(=createDateTime,desc)


예를들어: 전체물건 중 고가 순 정렬
http://localhost:8080/api/shop/3/products?size=10&sort=price,desc&product_set=total&page=0

## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지 ( ex) "_products_sort_" , 내가 무엇을 요청했는 지를 알수있다.)
"_products_sort_" 여기에 누가 어떤 요청을 하는 것에 따라 prefix 와 suffix 가 붙는다. 
 
prefix = user(비로그인유저 요청 | 로그인유저가 다른 상대방의 판매물건조회시) | my ( 로그인 후 내가 나의 판매물건을 조회했을 경우)
suffix = total(전체), sale(판매중), sold(거래완료)

ex1) 비로그인 유저가 판매물건조회 중 '전체'를 요청했을 경우 , user_products_sort_total
ex2) 로그인 유저가 자신의 판매물건조회 중 '판매중' 을 요청했을 경우, my_products_sort_sale

`status`: 응답 상태 

`data`: 유저 물건 정보

​		`total_size`: 해당 영역별 해당되는 총 물건 개수( 판매중(product_set=sale)이라면 판매중이라고 되어있는 모든 유저의 물건개수, 판매완료(product_set=sold)라면 판매완료라고 되어 있는 모든 유저 개수) 

​		`page_size`: 페이지 별 가져오는 크기(한 번에 가져오는 물건 개수)

​		`productsList`: 물건 리스트[JSON Array]

​		​		 `id`: 물건 고유ID
​		​		 `thumbnailImg`: 물건이미지경로(없을 경우 null 로 옵니다.)
​		​		 `title`: 물건제목
​		​		 `price`: 물건가격
​		​		 `likeNums`: 물건좋아요개수
​		​		 `chatroomNums`: 물건과 관련된 채팅방 개수
​		​		 `lastUpdatedDatetime`: 마지막 물건 수정날짜( 없을경우 : null )
​		​		 `uploadDatetime`: 마지막 물건 업로드날짜
​		​		 `transaction_status_id`: 물건거래상태(0: 판매중, 2:거래완료)



**example**

```json

{
    "message": "user_products_sort_sold",
    "status": 200,
    "data": {
        "total_size": 10,
        "page_size": 3,
        "productsList": [
            {
                "id": 10,
                "thumbnailImg": "/imgs/dog.jpeg",
                "title": "기프티콘_2",
                "price": "￦15,000",
                "likeNums": 10,
                "chatroomNums": 1,
                "lastUpdatedDatetime": "2021-01-29T21:16:23",
                "uploadDatetime": "2021-01-02T15:52:39",
                "transaction_status_id": 2
            },
            {
                "id": 9,
                "thumbnailImg": "/imgs/slideshow_sample.jpg",
                "title": "기프티콘_1",
                "price": "￦15,000",
                "likeNums": 9,
                "chatroomNums": 3,
                "lastUpdatedDatetime": "2021-01-16T13:04:50",
                "uploadDatetime": "2021-01-02T15:52:35",
                "transaction_status_id": 2
            },
            {
                "id": 1,
                "thumbnailImg": "/imgs/home_goods1.jpeg",
                "title": "도서_1",
                "price": "￦15,000",
                "likeNums": 1,
                "chatroomNums": 2,
                "lastUpdatedDatetime": "2021-02-02T01:52:09",
                "uploadDatetime": "2021-01-02T15:42:18",
                "transaction_status_id": 2
            }
        ]
    }
}

```


### 예외응답
아래와 같은 예외 응답을 받을 경우에는 바로 예외페이지으로 이동시킨다.
굳이 경고문구를 안띄워줘도 된다고 생각함(띄워줘도 상관없음)

**Code** : `404 Not found`

**Content**

`statusCode`: HTTP 상태코드
`timestamp` : 요청시간
`message` : 요청에러이유
`description` : 요청한 URL
`pathToMove` : 리다이렉트 해야하는 페이지 URL

// 존재하지 않거나 탈퇴한 유저의 판매물건들을 조회하려고 하는 경우
```json
{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:33:54.937+00:00",
    "message": "존재하지 않거나 탈퇴한 유저 입니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}
```

// 관리자에 의해 이용제재 받고 있는 유저의 판매물건들을 조회하려고 하는 경우
```json
{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:33:54.937+00:00",
    "message": "관리자로부터 이용제재 받고 있는 유저입니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}
```

// 차단한 유저의 판매물건들을 조회할 경우(로그인)

```json

{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:32:40.582+00:00",
    "message": "차단한 유저에 대한 정보를 조회할 수 없습니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}

```

// 차단당한 유저의 판매물건들을 조회할 경우(로그인)

```json

{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:32:40.582+00:00",
    "message": "차단당한 유저의 정보를 조회할 수 없습니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}

```


// 아래의 내용은 무시하셔도 됩니다.
[comment]: <> (## Fail Responses)

[comment]: <> (차단한 유저 혹은 차단된 유저의 프로필을 보기위해서 api요청을 보냈을 경우 다음과 같은 에러응답값을 반환한다.)

[comment]: <> (**Code** : `403 Forbidden`)

[comment]: <> (**Content**)

[comment]: <> (`statusCode`: HTTP 상태코드)

[comment]: <> (`timestamp` : 요청시간)

[comment]: <> (`message` : 요청에러이유)

[comment]: <> (`description` : 요청한 URL)

[comment]: <> (**example**)

[comment]: <> (1번유저가 차단한 유저의 10번이 판매하고 있는 물건들을 보려고 물건정보를 요청할 때 반환되는 값이다)

[comment]: <> (```json)

[comment]: <> ({)

[comment]: <> (    "statusCode": 403,)

[comment]: <> (    "timestamp": "2021-02-03T07:25:34.324+00:00",)

[comment]: <> (    "message": "Unable to access blocked user.",)

[comment]: <> (    "description": "uri=/api/shop/10/products")

[comment]: <> (})

[comment]: <> (```)


