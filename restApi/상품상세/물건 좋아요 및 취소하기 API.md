# 물건 좋아요 ( 좋아요 취소하기 )


* 인증여부 : 필요


로그인한 유저가 관심물건리스트 혹은 개별 물건페이지에서 좋아요를 하거나 했던 좋아요를 취소하는 기능이다.
개별 물건페이지에서 좋아요를 했다면 관심목록리스트 좋아요한 물건이 추가된다.

## 수정내용 5.4
좋아요/좋아요 취소 시 예외사항발생하게 되면 index 으로 이동시킵니다.
그렇기 때문에 이동해야하는 url 필드에 /shop/main/index 값이 추가 되었습니다.
예외응답부분은 99번째라인 밑에 있는 예외응답들은 모두 메시지내용이 바뀌었고, 모두 404에러를 반환합니다. 반.드.시 확인바랍니다.

## 수정내용 5.6
1. 로그인하지 않고 요청했을 경우의 예외응답 제거(어차피 게이트웨이 앞단에서 예외처리하기 떄문)
2. "좋아요/좋아요취소 하려고 하는 물건의 판매자가 관리자로 부터 이용제재조치를 받고 있는 경우" 의 응답 메시지 변경 => "물건의 판매자가 관리자로부터 이용제재조치를 받고 있어 좋아요/좋아요취소 요청을 할 수 없습니다."
3. "탈퇴한 유저의 물건에 대해서 좋아요(좋아요취소)를 요청했을 경우" 의 응답메시지 문구 변경
4. "차단한 관계에 있는 유저의 물건에 좋아요 및 좋아요취소하기 요청시" 의 응답 메시지 문구 변경

## 수정내용 6.18
예외발생시 커스텀에러응답코드를 반환하도록 함
- 관련한 에러 응답 메시지 수정


### request body(json)

`product_id` : 좋아요(또는 좋아요 취소) 하려는 물건의 고유 번호

`current_like_status` : 현재 물건의 좋아요 유무 상태( "like" = 내가 현재 해당 물건에 대해서 좋아요 중, "nolike" = 내가 현재 해당 물건에 대해서 좋아요 하고 있지 않음)


### response body

`message`: 응답메시지,
`status`: 응답코드,
`data`: 현재 좋아요|좋아요 취소한 물건의 현재 좋아요 상태

--- 
example response body


좋아요 신청 및 성공시
```
{
    "message": "좋아요 요청",
    "status": 200,
    "data": "current_status_like"
}
```

---

좋아요 취소 신청 및 성공시

```
{
    "message": "좋아요 취소 요청",
    "status": 200,
    "data": "current_status_nolike"
}
```

### except response (예외응답)

**Code** : `400 Bad Request`

**Content**

`statusCode`: custom 에러 응답 코드
`timestamp` : 요청시간
`message` : 요청에러이유
`description` : 요청한 URL
`pathToMove` : 리다이렉트 해야하는 페이지 URL


### 존재하지 물건에 대해서 요청을 할 때(삭제되거나 존재하지 않은 경우)

* 이 경우 물건이 존재하지 않는 데 해당페이지에 들어가 있는 상태이다. 즉 나는 해당 물건페이지에 있는 데 그 순간
  그 물건을 올린유저가 물건을 삭제한 경우이다. 이렇게 되었을 경우는 해당 물건페이지에 대한 정보를 더 이상 보여주면
  안되기 때문에 클라이언트는 해당 경고문구가 왔을 경우는 바로 인덱스페이지로 이동시켜야 한다.

```json

{
  "statusCode": 100,
  "timestamp": "2021-06-17T16:38:45.314+00:00",
  "message": "삭제되거나 존재하지 않은 물건입니다.",
  "requestPath": "uri=/api/product/like",
  "pathToMove": "/shop/main/index"
}

```

### 관리자에 의해 비공개 처리된 물건에 대해서 좋아요/좋아요취소 를 요청하였을 경우

* 위의 사례와 같다


```json

{
  "statusCode": 101,
  "timestamp": "2021-06-17T16:41:36.406+00:00",
  "message": "관리자에 의해 비공개 처리된 물건입니다.",
  "requestPath": "uri=/api/product/like",
  "pathToMove": "/shop/main/index"
}

``` 


### 탈퇴한 유저의 물건에 대해서 좋아요(좋아요취소)를 요청했을 경우

* 이 경우 탈퇴한 유저의 물건에 들어갈 수 없음에도 불구하고 해당페이지(혹은 내거래정보에서 관심물건이 있다면, 그곳에서 좋아요 취소/좋아요 요청)에 들어가 있는 상태이다. 
  즉 나는 해당 물건페이지(내거래정보의 관심물건에서 좋아요또는 좋아요취소 누름)에 있는 데 그 순간
  해당 물건을 올린 유저가 탈퇴한 경우, 이렇게 되었을 경우는 해당 물건페이지(내거래정보)에 대한 정보를 더 이상 보여주면
  안되기 때문에 클라이언트는 해당 경고문구가 왔을 경우는 바로 인덱스페이지로 이동시켜야 한다.

```json

{
  "statusCode": 102,
  "timestamp": "2021-06-17T16:42:31.099+00:00",
  "message": "물건의 판매자가 탈퇴하여 좋아요/좋아요취소 요청을 할 수 없습니다.",
  "requestPath": "uri=/api/product/like",
  "pathToMove": "/shop/main/index"
}
```


### 좋아요/좋아요취소 하려고 하는 물건의 판매자가 관리자로 부터 이용제재조치를 받고 있는 경우

* 위의 사례와 같다

```json

{
  "statusCode": 103,
  "timestamp": "2021-06-17T16:42:57.018+00:00",
  "message": "물건의 판매자가 관리자로부터 이용제재조치를 받고 있어 좋아요/좋아요취소 요청을 할 수 없습니다.",
  "requestPath": "uri=/api/product/like",
  "pathToMove": "/shop/main/index"
}

``` 

### 차단한 관계에 있는 유저의 물건에 좋아요 및 좋아요취소하기 요청시

* 에초에 차단관계에 있는 물건은 조회조차 불가능하다. 이 경우는 먼저 물건페이지에 들어간 상태에서 상대방이 나를
  또는 내가(다른 브라우저에서)상대방을 차단 한 경우를 뜻한다. 이때 만약 좋아요를 누르게 되면 서버에서는 차단관계에 있는 관계
  를
  
- 내가 차단한 물건에 대해서 좋아요/좋아요 취소 요청 하는 경우

```json

{
  "statusCode": 104,
  "timestamp": "2021-06-17T16:44:39.914+00:00",
  "message": "내가 차단한 유저의 물건에 대해 좋아요(취소) 요청을 할 수 없습니다.",
  "requestPath": "uri=/api/product/like",
  "pathToMove": "/shop/main/index"
}

```




- 나를 차단한 유저의 물건에 대해서 좋아요/좋아요 취소 요청을 하는 경우

```json

{
    "statusCode": 105,
    "timestamp": "2021-06-17T16:46:06.660+00:00",
    "message": "나를 차단한 유저의 물건에 대해 좋아요(취소) 요청을 할 수 없습니다.",
    "requestPath": "uri=/api/product/like",
    "pathToMove": "/shop/main/index"
}

```