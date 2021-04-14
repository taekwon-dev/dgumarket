## 수정사항이력


### 3.17 꼭 읽어볼 것! 경고유무알려주는 필드값 추가, 유저프로필 없을 경우 또는 물건이미지 없을 경우 설명 추가

`warn` 은 해당 물건을 올린 사용자가 관리자로 부터 경고 받고 있는 지에 대한 유무를 알려준다. false 의 경우 관리자로 부터 경고를 받고 있지 않고 있고, 이때 클라이언트가 해야할 일은 없다. 다만 true 가 왔을 때는 관리자로 부터 경고를 받고 있다는 뜻이므로 **물건상세정보 페이지 어느 곳에 경고아이콘의 형태로나 다른 UI형태로 해당 물건을 소유하고 있는 유저가 경고받고 있다는 사실을 알려줘야한다.**

실제로 경고에대해서 자세히 설명하면,
관리자로부터 유저가 경고를 받아도 실제로 바로 경고 유무가 true가 되지는 않는다. 경고가 3 또는 4개가 되었을 때 경고값이 true 가 되며 최대 경고 표시가 노출되는 기간은 최대 일주일이다. 가령 경고를 2번까지 받은 유저는 실제로 경고는 받았지만, 유저들에게 보여지는 부분에 있어서 경고유무값이 false 이기 때문에 어떤 패널티도 없다. 그런데 만약 관리자로 부터 경고를 3회 받았을 경우에는 3회째 경고를 받은 시점으로 부터 일주일이 지나지 않았다면 warn 필드의 값은 true 로 내려온다.


프로필이미지가 존재하지 않거나, 물건이미지가 없는 경우에는 모두 필드는 존재하나, 값이 null 인 형태로 온다.
ex)
`profileImgDirectory` : null, (프로필이미지)
...
`imgDirectories` : null (물건이미지)





# 개별물건정보조회

* 인증여부 : 불필요(선택)

-> 당연히 만약 로그인 했을 경우라면 해당
물건에 대한 좋아요유무를 `isLiked` 을 통해 알 수 있다.

---
### resonse body

`message`: 응답메시지(user_product | my_product )

"my_product" : 로그인한 유저가 자신의 물건을 조회할 경우

"user_product" : 로그인한 유저가 자기가 올린 물건이 아닌 다른 사람의 물건에 들어가거나, 로그인하지 않고 개별물건정보를 조회할 때

`status`: 응답코드,

`data`: 응답데이터,

---
`id`: 물건 고유아이디

`title`: 물건제목

`information`: 물건상세설명

`price`: 물건가격

`categoryId`: 물건카테고리 고유 아이디

`productCategory`: 물건카테고리 이름

`transactionStatusId`: 물건거래상태(0: 판매중, 2: 거래완료)

`transactionModeId`: 거래방식(0 : 직거래
1 : 비대면 거래(택배)
2 : 구매자와 조율 가능)

`isNego`: 네고여부( 0 : 가능, 1: 불가능 )

`likeNums`: 좋아요 수

`viewNums`: 조회 수

`chatroomNums`: 채팅방 수

`selfProductStatus`: [판매자 기준] 상품 상태 (0 :S급, 1:A급, 2:B급, 3:C급)

`userId`: 판매자 유저 고유 아이디

`profileImgDirectory`: 프로필 이미지 경로

`userNickName`: 판매자 유저 닉네임

`lastUpdatedDatetime`: 마지막 물건 수정 시간( 수정하지 않았을 경우는 null 로 옴)

`uploadDatetime`: 물건 업로드 시간

`isLiked`: 좋아요 유무("like"=내가 좋아요를 하고 있다는 뜻 | "nolike" = 내가 좋아요를 하고 있지 않다는 뜻 )

`imgDirectories`: 물건 이미지 경로 (이 부분은 나중에 해야함, 슬라이드로 불러올 떄는 어떻게 해야하는 지)

`warn`(boolean) : 경고 유무(해당 물건을 올린 유저가 현재 경고를 받고 있는 유저인지 아닌지를 알려줍니다.)



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

// 물건이 삭제되거나 존재하지 않는 물건을 조회할 경우
```json
{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:33:54.937+00:00",
    "message": "삭제되거나 존재하지 않은 물건입니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/exceptions"
}
```

// 관리자에 의해 비공개 처리된 물건일 경우
```json
{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:33:54.937+00:00",
    "message": "관리자에 의해 비공개 처리된 물건입니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/exceptions"
}
```

// 물건판매자가 탈퇴했을 경우
```json

{
  "statusCode": 404,
  "timestamp": "2021-04-14T02:30:07.400+00:00",
  "message": "물건의 판매자가 탈퇴하여 물건을 조회할 수 없습니다.",
  "requestPath": "uri=/api/product/119/info",
  "pathToMove": "/exceptions"
}


```


// 물건판매자가 관리자로 부터 이용제재 조치 받고 있을 경우
```json

{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:32:40.582+00:00",
    "message": "물건의 판매자가 관리자로 부터 이용제재조치를 받고 있어 물건을 조회할 수 없습니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/exceptions"
}

```

// 차단한 유저의 물건을 조회할 경우

```json

{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:32:40.582+00:00",
    "message": "차단한 유저의 물건을 조회할 수 없습니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/exceptions"
}

```

// 차단당한 유저의 물건을 조회할 경우

```json

{
    "statusCode": 404,
    "timestamp": "2021-04-14T02:32:40.582+00:00",
    "message": "차단당한 유저의 물건을 조회할 수 없습니다.",
    "requestPath": "uri=/api/product/119/info",
    "pathToMove": "/exceptions"
}

```