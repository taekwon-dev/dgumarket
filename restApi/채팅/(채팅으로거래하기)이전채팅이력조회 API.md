# 물건에 대해서 채팅으로 거래한 이력이 있는 지 조회하기 기능

*설명

로그인 후 물건 개별 정보가 있는 페이지에서(onepick) 채팅으로 거래하기 클릭 시 내가 현재 해당 물건에 대해서 채팅으로 거래한 이력이 있는 지 확인한다.

만약 해당 물건에 대해서 채팅으로 거래한 이력이 있다면 "해당 물건에 대해서 채팅으로 거래한 이력이 있습니다. 채팅방에서 확인하세요!" 라고 클라이언트는 UI로 보여줘야 할 것이고 사용자를 채팅방으로 가게끔 유도한다.

혹은 거래한 이력이 없다면 이전처럼 채팅방 UI가 나오고 채팅을 할 수 있는 상황이 된다.

## 수정 사항 6.18 
- 예외발생시 커스텀에러코드 반환하도록 바꿈


**URL** : `/api/product/{productId}/chat-history`

**Method** : `GET`

**Authentication required** : `yes`

**Request param(path)**

`productId` : 고유 물건아이디 ( 현재 물건 상세페이지의 물건 고유 번호 )


### success response body(json)

1) 해당 물건에 대해서 채팅이력이 있는 경우

`status` : 응답코드,

`message` : 응답메시지

`data` : 이력유무 데이터

---

`history_room_id` : 채팅방 아이디(optional, 1,2번의 사례의 경우 포함됨)

`history_product_id` : 물건번호(optional, 1,2번의 사례의 경우 포함됨)

`existed` : 채팅이력 존재유무,

`leave` : 채팅방 나감유무



### 1. 채팅으로거래하기 클릭시, 해당물건에 대해서 채팅방이 있고 나가지 않았을 경우

```json

{
    "message": "이전에 채팅거래했는지 조회",
    "status": 200,
    "data": {
        "history_room_id": 422,
        "history_product_id": 4,
        "existed": true,
        "leave": false
    }
}

```



### 2. 채팅으로거래하기 클릭시, 해당물건에 대해서 채팅방은 있지만(채팅 이력은 있지만) 나갔을 경우

```json

{
    "message": "이전에 채팅거래했는지 조회",
    "status": 200,
    "data": {
        "history_room_id": 422,
        "history_product_id": 4,
        "existed": true,
        "leave": true
    }
}


```


### 3. 채팅으로 거래하기 클릭시, 해당물건에 대해서 채팅한 이력이 없을 경우


````json

{
    "message": "이전에 채팅거래했는지 조회",
    "status": 200,
    "data": {
        "existed": false,
        "leave": false
    }
}


````



## 예외의 경우(클라이언트가 해야할 일 )

## 결론
**해당예외의 경우가 나오면 채팅방 UI를 띄워주는 부분까지 안가면 된다!!
그리고 1, 2, 3, 4, 5, 6의 경우에는 애초에 이 공간에 들어오면 안되는 상황이므로 인덱스 페이지로 이용자를
강제 이동시킨다.**
즉, 아래의 4가지의 경우에 해당되면 애초에 이 화면에 들어올 수 없는 구조이다! 
이 경우는 이미 유저가 물건 상세페이지에 들어온 상태에서 아래의 4가지 케이스가 적용되고 
채팅으로 거래하기 를 클릭하려고 하는 상태이다!


1. 새롭게 채팅하려고 하는 유저가 **삭제** 된 물건에 대해서 요청하려고 할 때
2. 새롭게 채팅하려고 하는 유저가 **비공개 처리** 된 물건에 대해서 요청하려고 할 때
3. 새롭게 채팅하려고 하는 유저가  **탈퇴한 유저** 의 물건에 대해서 요청하려고 할 때
4. 새롭게 채팅하려고 하는 유저가 **이용제재** 당한 유저의 물건에 대해서 요청하려고 할 때
5. 새롭게 채팅하려고 하는 유저가 자신이 **차단한 유저**의 물건에 대해서 요청하려고 할 때(서로 차단했을 때도 5번에 해당)
6. 새롭게 채팅하려고 하는 유저가 자신이 **차단당한 유저**의 물건에 대해서 요청하려고 할 때

## except response

**Code** : `400 Bad Request`

**Content**

`statusCode`: custom 에러 응답 코드
`timestamp` : 요청시간
`message` : 요청에러이유
`description` : 요청한 URL
`pathToMove` : 리다이렉트 해야하는 페이지 URL

### 1. 새롭게 채팅하려고 하는 유저가 **삭제, 존재하지 않은** 물건에 대해서 요청하려고 할 때

```json

{
  "statusCode": 100,
  "timestamp": "2021-06-17T17:27:43.484+00:00",
  "message": "삭제된 중고물품의 경우 채팅거래를 하실 수 없습니다.",
  "requestPath": "uri=/api/product/119/chat-history",
  "pathToMove": null
}

```





### 2. 새롭게 채팅하려고 하는 유저가 **관리자에 의해 비공개 처리** 된 물건에 대해서 요청하려고 할 때

```json

{
  "statusCode": 101,
  "timestamp": "2021-06-17T17:28:15.003+00:00",
  "message": "관리자에 의해 비공개 처리된 물건입니다. 채팅거래를 하실 수 없습니다.",
  "requestPath": "uri=/api/product/119/chat-history",
  "pathToMove": null
}

```



### 3. 새롭게 채팅하려고 하는 유저 **탈퇴한 유저** 의 물건에 대해서 요청하려고 할 때


```json

{
  "statusCode": 102,
  "timestamp": "2021-06-17T17:30:10.043+00:00",
  "message": "탈퇴한 유저입니다. 채팅거래를 하실 수 없습니다.",
  "requestPath": "uri=/api/product/119/chat-history",
  "pathToMove": null
}

```


### 4. 새롭게 채팅하려고 하는 유저가 **이용제재** 당한 유저의 물건에 대해서 요청하려고 할 때



```json

{
  "statusCode": 103,
  "timestamp": "2021-06-17T17:30:24.870+00:00",
  "message": "관리자로부터 이용제재당한 유저와 채팅거래를 하실 수 없습니다.",
  "requestPath": "uri=/api/product/119/chat-history",
  "pathToMove": null
}

```


### 5. 새롭게 채팅하려고 하는 유저를 내가 차단했을 경우


```json

{
  "statusCode": 104,
  "timestamp": "2021-06-17T17:30:49.419+00:00",
  "message": "차단한 유저와는 채팅 거래 할 수 없습니다.",
  "requestPath": "uri=/api/product/119/chat-history",
  "pathToMove": null
}

```


### 6. 새롭게 채팅하려고 하는 유저가 나를 차단했을 경우


```json

{
  "statusCode": 105,
  "timestamp": "2021-06-17T17:31:37.341+00:00",
  "message": "나를 차단한 유저와는 채팅 거래 할 수 없습니다.",
  "requestPath": "uri=/api/product/119/chat-history",
  "pathToMove": null
}

```



