---
REST API - Chat created by MS (21-02-04)
---


### 수정사항(3.25)

이미지가 없을 경우 null로 옵니다.

### 꼭 읽을 것! (5/2 수정사항 추가)
기존 접근불가능 했던 api에 대해서 예외처리 화면으로 이동한 것을 이제는 /shop/main/index (인덱스페이지)로 이동하기위해 예외 응답 pathToMove 필드 값은 /shop/shop/main/index 로 내려옴


# 유저에게 남긴 리뷰 조회하기

유저가 중고로 팔고있는 물품들을 조회한다.


**URL** : `/api/shop/{user-id}/reviews` 

**Method** : `GET`

**Authentication required** : `no`

**Request Param** : 

`page` : 보여줄 페이지, 무한스크롤시 스크롤 최하단에 닿을때마다 page 1씩 늘어남-> 추가적인 정보를 가져옴(number) ( 0 부터 시작 )

`size` : 한 페이지에 보여줄 리뷰개수(number) (임의로 정할 수 있음)

`sort` : 세부정렬(리뷰작성 오래된순(ReviewRegistrationDate,asc), 리뷰작성 최신순(아무것도 없어도 됨 | ReviewRegistrationDate,desc))

기본적으로 리뷰작성 sort param 이 없을 경우, 최신순으로 정렬한다.

ex)
2번유저의 리뷰 조회
http://localhost:8080/api/shop/2/reviews?page=0&size=10&sort=ReviewRegistrationDate,desc (최신순)
http://localhost:8080/api/shop/2/reviews?page=0&size=10 (최신순)

## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지 

ex1) 비로그인 유저(or 로그인 유저가 자신이 아닌 다른 유저의) review 조회요청했을 경우 , review_messages
ex2) 로그인 유저(나)가 내 자신의 review 조회를 요청했을 경우, my_review_messages

`status`: 응답 상태 

`data`: 유저에게남긴 리뷰들 정보

​		`total_size`: 해당 영역별 해당되는 총 리뷰 개수 

​		`page_size`: 페이지 별 가져오는 크기(한 번에 가져오는 리뷰 개수)

​		`review_list`: 물건 리스트[JSON Array]

​		​		 `review_user_id`: 리뷰남긴유저 고유 ID,
​		​		 `review_user_icon`: 유저 프로필 이미지 경로,(이미지 없을경우 null로 옵니다.)
​		​		 `review_nickname`: 리뷰 유저 닉네임,
​		​		 `review_comment`: 리뷰 내용,
​		​		 `review_date`: 리뷰 남긴 날짜



**example**

```json

{
    "message": "review_messages",
    "status": 200,
    "data": {
        "total_size": 3,
        "page_size": 3,
        "review_list": [
            {
                "review_user_id": 2,
                "review_user_icon": "/imgs/dog.jpeg",
                "review_nickname": "fgh0296",
                "review_comment": "테스트16",
                "review_date": "2021-01-18T06:55:43"
            },
            {
                "review_user_id": 2,
                "review_user_icon": "/imgs/dog.jpeg",
                "review_nickname": "fgh0296",
                "review_comment": "2번이 1번의1번물건을 샀다!",
                "review_date": "2021-02-02T10:52:09"
            },
            {
                "review_user_id": 2,
                "review_user_icon": "/imgs/dog.jpeg",
                "review_nickname": "fgh0296",
                "review_comment": "테스트17",
                "review_date": "2021-02-02T10:52:09"
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

// 존재하지 않거나 탈퇴한 유저의 리뷰들을 조회하려고 하는 경우
```json
{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:33:54.937+00:00",
    "message": "존재하지 않거나 탈퇴한 유저 입니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}
```

// 관리자에 의해 이용제재 받고 있는 유저의 리뷰들을 조회하려고 하는 경우
```json
{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:33:54.937+00:00",
    "message": "관리자로부터 이용제재 받고 있는 유저입니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}
```

// 차단한 유저의 리뷰들을 조회할 경우(로그인)

```json

{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:32:40.582+00:00",
    "message": "차단한 유저에 대한 정보를 조회할 수 없습니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/shop/main/index"
}

```

// 차단당한 유저의 리뷰들을 조회할 경우(로그인)

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

[comment]: <> (차단한 유저 혹은 차단된 유저의 리뷰를 보기위해서 api요청을 보냈을 경우 다음과 같은 에러응답값을 반환한다.)

[comment]: <> (**Code** : `403 Forbidden`)

[comment]: <> (**Content**)

[comment]: <> (`statusCode`: HTTP 상태코드)

[comment]: <> (`timestamp` : 요청시간)

[comment]: <> (`message` : 요청에러이유)

[comment]: <> (`description` : 요청한 URL)

[comment]: <> (**example**)

[comment]: <> (1번유저가 차단한 유저의 10번의 리뷰들을 보려고 리뷰정보를 요청할 때 반환되는 값이다)

[comment]: <> (```json)

[comment]: <> ({)

[comment]: <> (    "statusCode": 403,)

[comment]: <> (    "timestamp": "2021-02-04T13:27:24.455+00:00",)

[comment]: <> (    "message": "Unable to access blocked user.",)

[comment]: <> (    "description": "uri=/api/shop/10/reviews")

[comment]: <> (})

[comment]: <> (```)


