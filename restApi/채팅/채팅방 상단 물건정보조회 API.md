# 채팅방의 물건 조회하기

채팅방에 들어갔을 떄(채팅방화면) 상단의 어떤 제품인지에 대한 정보를 조회할 수 있는 API 이다.

**결론 : 채팅방 화면이 보여지는 모든 순간 에 아래의 엔드포인트로 서버에 요청하여 물건에 대한 정보를 가져온다.**

예전에는 물건이 삭제되었을 경우 그렇지 않았을 경우 두가지의 상태값이 존재했다면 수정된 내용에서는


1. 물건 블라인드조치된 물건일 경우
2. 탈퇴한유저의 물건일 경우
3. 이용제재조치 당한 유저의 물건일 경우
4. 물건이 삭제되었을 경우

이렇게 총 4가지가 있으며 이 4가지에 해당한다면
각각의 응답값 메시지 형태는 해당 포스트맨의 예시 응답과 같다.

**1.2.3.4 가 아닌 경우라면, 즉 정상적인 경우에는
아래와 같은 응답값을 가지게 된다.**

**URL** : `/api/chatroom/product/{productId}`

**request param(path)** : `{productId}` : 물건번호

**Method** : `GET`

**Authentication required** : `yes`

**Data** : `None`

## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지

`status`: 응답 상태

`data`: 물건 정보

​		`product_id`: 물건 고유 아이디 (채팅방의 상단 섹션 클릭시 물건의 상세페이지로 이동하기 위함)

​		`product_title`: 물건 제목

​		`product_price`: 물건 가격

​		`product_img_path`: 물건 이미지 경로( 만약 물건의 이미지가 없을 경우에는 아예 필드가 존재하지 않는다 )

​		`transaction_status_id`: 물건 거래 상태(0 : 판매중 (Default)
2 : 판매완료 (거래 완료 후, 판매자가 직접 설정) )

**example**

```json
{
    "message": "채팅방 물건 정보",
    "status": 200,
    "data": {
        "product_id" : 1, 
        "product_title": "도서_1",
        "product_price": "￦15000",
        "product_img_path": "/imgs/slideshow_sample.jpg",
        "transaction_status_id": 0
    }
}
```

**만약 삭제된 경우라면 ? ** 예전에는 `transaction_status_id` : 4 라는 값으로 응답을 줬다면 이제는 그게 아니라


```json


{
    "statusCode": 404,
    "timestamp": "2021-03-11T03:56:40.100+00:00",
    "message": "존재하지 않은 상품입니다.",
    "description": "uri=/api/chatroom/product/4"
}


```


와 같은 응답메시지가 내려온다.



** 클라이언트 **

이와 같은 예외응답이 온다면 해당 물건부분을 선택하지 못하도록 비활성하게 하면 된다. 그리고 에러메시지를 저렇게 세부적으로 나눈 이유는 물론 실제 서비스를 이용하는 사람들에게는 보이겠지만 클라이언트가 개발하기 편하게 만들었음



** 꼭!! 읽어볼 것 3/17 추가 설명, 채팅방상단의 물건 정보 조회시, 물건의 이미지가 없을경우

채팅방 상단의 물건 조회시 물건의 이미지가 없을 경우에는 아예 필드값이 없다. 가령
어떤 채팅방의 상단 물건번호가 2번이고 물건의 이미지가 존재할 경우
ex)
```json
{
    "message": "채팅방 물건 정보",
    "status": 200,
    "data": {
        "product_id": 2,
        "product_title": "도서_2",
        "product_price": "￦2,000",
        "product_img_path": "https://dgu-springboot-build.s3.ap-northeast-2.amazonaws.com/sample/145185076_1_1612252036_w292.jpg",
        "transaction_status_id": 0
    }
}
```

하지만 만약 이미지가 없을 경우에는 아예 `product_img_path` 이라는 필드값이 존재하지 않게된다.

````json

{
  "message": "채팅방 물건 정보",
  "status": 200,
  "data": {
    "product_id": 2,
    "product_title": "도서_2",
    "product_price": "￦2,000",
    "transaction_status_id": 0
  }
}

````


## 예외 응답

1. 물건 비공개 처리조치 된 물건일 경우
2. 탈퇴한유저의 물건일 경우
3. 이용제재조치 당한 유저의 물건일 경우
4. 물건이 삭제되었을 경우


### 1.비공개 처리 조치 된 경우

```json

{
    "statusCode": 404,
    "timestamp": "2021-03-11T03:57:31.079+00:00",
    "message": "해당 중고물품은 관리자에 의해 비공개 처리되었습니다.",
    "description": "uri=/api/chatroom/product/4"
}


```


### 2. 탈퇴한 유저의 물건일 경우


```json

{
    "statusCode": 400,
    "timestamp": "2021-03-11T03:58:31.134+00:00",
    "message": "탈퇴한 유저의 물건은 조회할 수 없습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```



### 3. 이용제재조치 당한 유저의 물건일 경우


```json

{
    "statusCode": 400,
    "timestamp": "2021-03-11T03:58:46.957+00:00",
    "message": "이용제재를 받고 있는 유저의 물건은 조회할 수 없습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```


### 4. 물건이 삭제되었을 경우


````json

{
    "statusCode": 404,
    "timestamp": "2021-03-11T03:56:40.100+00:00",
    "message": "해당 중고물품은 삭제처리 되었습니다.",
    "description": "uri=/api/chatroom/product/4"
}


````


4가지 응답 메시지 중 어느 하나를 응답받았다면 클라이언트는 해당 채팅방 상단의 물건 이미지를 
기본이미지로 바꾸고 어떠한 물건에 대한 설명도 있어서는 안된다.