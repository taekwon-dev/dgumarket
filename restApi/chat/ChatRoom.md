---
REST API - Chat created by MS (21-01-08)

수정내역
21-01-25

채팅방 목록을 불러올 때 `block` 필드가 하나 더 추가되었다.
[block](#blockboolean--------true---false---)
설명 : 해당 필드는 채팅방의 상대방을 내가 차단했는 지 차단하지 안했는 지에 대한 차단유무를 알려준다. 


---

# 채팅방 목록들 조회하기 

- `채팅방 목록 조회`

`채팅방 목록 조회`는 채팅방 목록화면에 들어갔을 떄 클라이언트(유저)와 관련된 모든 채팅방들을 조회한다. 


**결론 : 채팅방 목록화면이 보여지는 모든 순간 에 아래의 엔드포인트로 서버에 요청하여 채팅방 목록 데이터를 가져온다.**  

1. 플로팅 버튼이 있는 화면(꼭 인덱스가 아니더라도 플로팅버튼이 나와있는 화면은 모두 해당된다.)에서 플로팅 버튼을 클릭하여 채팅방 목록화면이 나올 떄 
2. 채팅방 화면에서 뒤로가기(< 버튼)를 눌러 채팅방 목록화면으로 다시 돌아갈 떄

나와 관련되어 있는(내가 누군가에게 채팅을 해서 만들어진 채팅방, 다른 누군가가 나의 물건이 맘에 들어서 채팅을 했을 떄 생긴 채팅방) 모든 채팅방들을 가져옵니다.
 
 여기서 응답되는 값들의 필드 중 눈여겨 봐야 할 부분은 다음과 같다.
 1. `roomId` (채팅방 고유 ID)  
 2. `chatRoomRecentMessageDto` 의 `message_type` (최근 메시지의 메시지 타입)
 3. `chatRoomRecentMessageDto` 의 `message_date`(최근 메시지의 메시지 보낸 시간) 
 4. `chatRoomProductDto` 의 `product_deleted` (채팅방 물건의 삭제여부)
 5. `block` 차단 유무(내가 상대방을 차단했는지 에대한 유무)
 
 
 #### `roomId` (채팅방고유ID) 
 채팅방 고유 ID는 채팅방을 구별할 수 있는 요소이다. (실시간으로 메시지가 오게될 떄 메시지를 적절한 채팅방에 보여주기 위해서 존재) 
 예를들어 유저가 채팅방 목록화면에 있다고 생각해보자. 이떄 웹소켓 프로토콜을 통해 메시지가 오게되면 메시지안에 있는 채팅방 번호를 가져와 어느 채팅방목록에 채팅메시지를 
 넣을 지 판단해야 한다. 아래의 엔드포인트로 요청하여 개별 채팅방의 `roomId` 정보를 받게 되면 `roomId` 값은 개별 채팅방 DOM 에 위치시켜 나중에 오는 메시지를 
 적절하게 처리하여 보여줄 수 있어야 한다.
 
 
 #### `chatRoomRecentMessageDto` 의 `message_type` (최근 메시지의 메시지 타입)
 메시지 타입은 최근 보낸 메시지가 텍스트였냐 아니면 이미지를 보냈냐 하는 유무를 나타낸다. 텍스트 : 0, 이미지 : 1
 만약 테스트일 경우에는(message_type : 0) 큰 문제가 되지 않지만
 만약 이미지일 경우에는(message_type : 1) 일 경우에는 최근 메시지 내용 부분에 이미지를 보여줄 수 없기 떄문에 이럴 경우 클라이언트는 "사진을 보냈습니다" 라는 문구를 보여준다. 
 
예시, 개별 채팅방들 중 하나가 이렇게 되어있을 것이다.)
 ```json
  {
        "roomId": 62,
        "chatMessageUserDto": {
            "userId": 1,
            "nickName": "asd0296"
        },
        "chatRoomRecentMessageDto": {
            "message_type": 1,
            "message_date": "2021-01-09T00:28:54",
            "message": "/imgs/slideshow_sample.jpg"
        },
        "unreadMessageCount": 3,
        "block": true,
        "chatRoomProductDto": {
            "product_id" : 1,
            "product_deleted": 0,
            "productImgPath": "/imgs/slideshow_sample.jpg"
        }
    }

```
 

 
 #### `chatRoomRecentMessageDto` 의 `message_date`(최근 메시지의 메시지 보낸 시간)
 메시지 날짜는 2021-01-08T13:20:19 와 같은 형태이다. 년,월,일 뿐만 아니라 초단위 까지 필요하기 떄문에 다음과 같은 형태로 보낸다. )
 만약 해당 날짜를 받았다면 클라이언트는 적절하게 날짜를 고친 후 메시지를 보여줘야 할 것이다. 그리고 서버로 부터 받은 2021-01-08T13:20:19 의 날짜형태는 개별 채팅방의 고유 ID를 개별 채팅방 DOM 에 넣은 것처럼 
 어딘가에 보관을 해야한다. 
 


#### `chatRoomProductDto` 의 `product_deleted` (채팅방 물건의 삭제여부)

채팅방의 물건이 삭제되었을 경우에는 `product_deleted` 값이 1이된다. 그리고 찾으려고 하는 productImgPath 는 존재하지 않는다.(null)
그렇기 떄문에 채팅목록 왼쪽의 물건이미지를 대체할 수 있는 이미지를 새롭게 화면에 넣어야 한다.

예시, 개별 채팅방들 중 하나가 이렇게 되어있을 것이다.)
 ```json
  {
        "roomId": 62,
        "chatMessageUserDto": {
            "userId": 1,
            "nickName": "asd0296"
        },
        "chatRoomRecentMessageDto": {
            "message_type": 1,
            "message_date": "2021-01-09T00:28:54",
            "message": "/imgs/slideshow_sample.jpg"
        },
        "block": false,
        "unreadMessageCount": 3,
        "chatRoomProductDto": {
            "product_id": 1, 
            "product_deleted": 1,
            "productImgPath": null
        }
    }

```

#### `block` (채팅방고유ID) 

`block` 이란 내가 특정유저를 차단했을 경우 해당 유저와 관련된 모든 채팅방들은 이미 차단된 상태를 유지해야한다. 즉, 채팅방 목록의 햄버거바를 
클릭해서 나오는 `차단하기` 는 해당 채팅방 상대방(유저) 에 대한 차단이다. 그렇기 때문에 만약 내가 채팅방목록을 조회할 때 차단한 유저와
관련되어 있는 채팅방들은 모두 `차단` 된 상태(유저를 차단한 상태) 를 유지해야 한다.

만약 A유저가 올린 10개의 물건(1, 2, ... 10)에 대해서 B유저가 모두 채팅을 통해 거래를 시도한 상태라고 가정하자. 이때 A에게는 B유저와 대화한 10개의 채팅방이 
존재한다. 그리고 개별 10개의 채팅방에 있는 `차단하기` 기능은 B를 차단하는 것과 동일하다. 만약 1번 물건이 있는 채팅방의 햄버거 버튼을 눌러
차단하기를 눌렀을 경우에는 (2, 3, 4 ... 10) 나머지 B와 채팅한 채팅방들 은 `차단하기` 가 이미 일어난 것이기 때문에
클라이언트는 채팅방 목록에서 특정 채팅방의 햄버거바를 눌러 `차단하기` 눌러 차단을 요청한 후 `차단해제`로 바꿔야 할텐데, 이때 B와 관련되어 있는 
모든 채팅방의 햄버거 버튼을 눌렀을 때 `차단해제` 문구가 나오도록 해야한다.


**URL** : `/chat/rooms` 

**Method** : `GET`

**Authentication required** : `yes`

**Data** : `None`

## Success Responses

___

**Code** : `200 OK`

**Content**

`roomId`: 채팅방의 고유 ID (채팅방 고유아이디는 생성되는 채팅목록 DOM 들의 고유한 이름으로 사용한다. )

`chatMessageUserDto`: 채팅하는 상대방의 정보

​		`userId` : 상대방의 고유 ID (채팅방 들어가서 상대방에게 메시지를 보낼 때(`SEND, /message`) 사용된다.) 

​		`nickName` : 상대방의 닉네임 (내가 어떤 사용자와 채팅하고 있는 지에 사용자의 닉네임정보 -> 채팅방 목록 중 개별 채팅방의 Jypman 닉네임부분에 해당)

`chatRoomRecentMessageDto`: 채팅방의 가장 최근 메시지 

​		`message_type` : 최근 메시지의 메시지 타입(0: 텍스트, 1: 이미지) 

​	    `message_date` :  최근 메시지의 메시지 보낸 날짜

​		`message` : 최근 메시지의 내용  

`unreadMessageCount` : 채팅방의 읽지 않은 메시지의 개수 (해당 채팅방의 읽지 않은 메시지 개수를 보여준다. -> 채팅방 목록 중 개별 채팅방의 오른쪽 읽지 않은 메시지 개수 표현)

#`block(boolean)` : 해당 채팅방의 상대방을 차단했는지에 대한 유무( true : 차단함, false : 차단하지않음 )

`chatRoomProductDto` : 채팅방의 물건 정보

​	    `product_id` : 물건 고유 아이디(이 또한 채팅목록의 classId 에 넣을 필요가 있다. 채팅방클릭시 채팅방 화면의 상단 섹션부분에 물건에 대한 정보를 조회하는 API 이용 시 활용)

​	    `product_deleted` : 물건 삭제여부(0 : 삭제안함, 1 : 삭제함) 

​	    `productImgPath` : 물건 이미지 사진 경로 (개별 채팅방 왼쪽에 물건이미지가 저장된 이미지 경로, 가져와서 해당부분에 렌더링
삭제되었을 경우 null 로 들어옴
)

**example**

```json
[
    {
        "roomId": 105,
        "chatMessageUserDto": {
            "userId": 1,
            "nickName": "asd0296"
        },
        "chatRoomRecentMessageDto": {
            "message_type": 0,
            "message_date": "2021-01-15T18:07:56",
            "message": "갖고싶어여"
        },
        "block": false,
        "unreadMessageCount": 0,
        "chatRoomProductDto": {
            "product_id": 4,
            "product_deleted": 0,
            "productImgPath": "/imgs/slideshow_sample.jpg"
        }
    },
    {
        "roomId": 106,
        "chatMessageUserDto": {
            "userId": 1,
            "nickName": "asd0296"
        },
        "chatRoomRecentMessageDto": {
            "message_type": 0,
            "message_date": "2021-01-15T18:07:56",
            "message": "안뇽"
        },
        "unreadMessageCount": 0,
        "block": false,
        "chatRoomProductDto": {
            "product_id": 5,
            "product_deleted": 0,
            "productImgPath": "/imgs/slideshow_sample.jpg"
        }
    }
]
```

---

# 채팅방의 물건 조회하기

채팅방에 들어갔을 떄(채팅방화면) 상단의 어떤 제품인지에 대한 정보를 조회할 수 있는 API 이다.
 
**결론 : 채팅방 화면이 보여지는 모든 순간 에 아래의 엔드포인트로 서버에 요청하여 물건에 대한 정보를 가져온다.**  

두가지 경우가 있음
1. 물건이 삭제되지 않았을 경우 
2. 물건이 삭제되었을경우


1. 물건이 삭제되지 않았을 경우

**URL** : `/chat/room/section/product/{product-id}` 

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

​		`product_img_path`: 물건 이미지 경로

​		`transaction_status_id`: 물건 거래 상태(0 : 판매중 (Default)
                                          1 : 예약중 (판매자가 특정 구매 의사를 보인 사람과 거래 약속을 잡은 경우, 판매자가 직접 설정)
                                          2 : 판매완료 (거래 완료 후, 판매자가 직접 설정) 
                                          3 : 신고처리중 (관리자가 직접 설정) )

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
        "transaction_status_id": 3
    }
}
```

2. 물건이 삭제되었을경우

## Success Responses

___
 
**Code** : `200 OK`

**Content**

`message`: 응답 메시지 

`status`: 응답 상태 

`data`: 물건 정보

​		`transaction_status_id`: 물건 거래 상태(4 : 삭제 (판매자가 업로드한 게시물을 삭제한 경우))

**example**

```json
{
    "message": "채팅방 물건 정보",
    "status": 200,
    "data": {
        "transaction_status_id": 4
    }
}
```

___

# 채팅방(판매자) 거래완료 요청하기

채팅방에서 거래완료를 하기위해선 아래와 같은 엔드포인트로 서버로 요청한다.
**결론 : 판매자가 채팅방에서 거래완료 확인 버튼을 누르는 순간 아래와 같은 엔드포인트로 서버에 요청한다.** 


 **URL** : `/chat/room/{room-id}/product` 
 
 **Method** : `PATCH`
 
 **Authentication required** : `yes`
 
 **Body** : 
 
 `transaction_status_id` : 물건거래상태( 2 : 거래완료)
 
 ```json

{
    "transaction_status_id" : 2 
}

```


## Success Responses

**Status** : 200

`product status updated` : 정상적으로 물건거래상태가 거래완료로 바뀌었을 경우 다음과 같은 텍스트를 반환한다. 


# 채팅방 상태 조회하기

여기서 말하는 상태란, 유저가 채팅방에 들어갔을 떄 구매자입장에서 들어갔는지, 판매자 입장에서 들어갔는지
구매자 입장에서 들어갔다면 판매자가 거래완료를 눌렀는 지 누르지 않았는지
판매자가 구매완료 버튼을 눌렀다면 구매자 채팅방은 어떻게 확인할 수 있는 지를 조회하는 API 이다.

**결론 : 유저가 채팅방 목록에서 채팅방으로 들어갈 떄 아래의 엔드포인트로 요청한다**


 **URL** : `/chat/room/{room-id}/status` 

 **Method** : `GET`
 
 **Authentication required** : `yes`
 
 ## Success Responses

**Content**

`message`: 응답 메시지 

`status`: 응답 상태 

`data`: 채팅방 상태 정보

​		`productStatus`: 채팅방 상태
0: 판매자가 구매완료 누르지 않은 상태에서 구매자가 채팅방에 들어갈 경우
1: 판매자가 구매완료 눌렀을 상태에서 구매자가 채팅방에 들어갈 경우
2: 판매자가 채팅방에 들어갈 경우
3: 아예 채팅방의 물건자체가 삭제된 경우

​		`transactionStatus`(optional) : 해당 물건에 대해서 거래완료 를 했는지에 대한 유무( 1: 거래완료 됨)

​		`isReviewUpload`(optional) : 실제 구매자가 리뷰를 올렸는 지에 대한 유무 ( 1: 리뷰작성함 )

​		`reviewer_nickname`(optional) : [판매자에게만 보임]리뷰를 남긴 사람의 닉네임(리뷰어 리뷰를 남기지 않아도 누구와 거래했는 지에 대한 닉네임 정보는 알 수 있음)



1.
**example** 

채팅방에 판매자본인이 들어갔을 경우 + 아직 거래완료 누르지 않은 상태
클라이언트는 거래완료 버튼을 활성화 하도록 한다.

```json

{
    "productStatus": 2
}

```


2.
**example**

(판매자가 구매완료 버튼 누른)판매자본인이 들어갔을 경우 그런데 아직 구매자가 사용후기를 남기지 않았을 경우

클라이언트는 거래완료 버튼을 비활성화 상태로 만들 되, 아직 구매자가 사용후기를 남기지 않았기 때문에 
"아직 구매자가 사용후기를 작성하지 않았습니다" 등의 문구정도는 채팅방 상단 섹션에 띄어준다. 

 ```json
{
    "productStatus": 2,
    "transactionStatus": 1, 
    "reviewer_nickname": "fgh0296"
}

```

(판매자가 구매완료 버튼 누른)채팅방에 판매자본인이 들어갔는데, 구매자가 후기를 남겨준 경우
클라이언트는 이 경우, 거래완료도 했고, 구매자가 거래후기를 남겼기 떄문에 
" 구매자가 거래후기를 남겼습니다 :) 거래후기 보기 " 등과 같은 버튼이나 문구를 채팅방 상단 섹션 DOM
에 그려준다. 
그리고 거래완료 버튼은 비활성화 또는 거래하기 버튼 모양이 거래완료라는 것을 뜻하는 버튼모양으로 바뀐다.

3.
**example**
```json

{
    "productStatus": 2,
    "transactionStatus": 1,
    "isReviewUpload": 1,
    "reviewer_nickname": "fgh0296"
}

```

4.

(판매자가 거래완료 버튼 누른) 채팅방에 `구매자`가 들어가는 경우 + 구매자가 리뷰를 남기지 않은 경우
클라이언트는 이 경우 [후기 남기기] 라는 버튼을 생성하여 후기를 남길 수 있도록 한다.

**example**
```json

{
    "productStatus": 1,
    "transactionStatus": 1
}

```

5.

(판매자가 거래완료 버튼 누른) 채팅방에 `구매자` 가 들어가는 경우 + 구매자가 리뷰를 남긴 경우
클라이언트는 [후기 남기기] 버튼이 아닌 이미 내용을 입력한 상태이기 떄문에, [내가 남긴 후기 보기] 등으로 
문구를 바꾸면 될 것이다.

**example**
```json

{
    "productStatus": 1,
    "transactionStatus": 1,
    "isReviewUpload": 1
}

```


6.

구매자가 아직 거래완료 되지 않은 채팅방에 들어가는 경우  
만약 이 경우 클라이언트는 아무런 조치를 하지 않아도 된다.

```json

{
    "productStatus": 0 
}

```

7.
**example** 

<span style="color: red;">채팅방의 물건이 삭제된 경우</span>.

```json

{
    "productStatus": 3
}

```


# 채팅방나가기

여기서 말하는 채팅방 나가기란 
카카오톡의 채팅방 나가기 처럼 실제로 해당 채팅방을 지우는 행위를 뜻한다.( 즉, 나에게선 채팅방 목록이 보이진 않지만,  상대방은 여전히 채팅방 목록에 해당 채팅방이 존재한다.)
실제로 삭제하지 않고 테이블의 채팅방 나감 유무 값을 1로 바꾸고 나간 날짜를 기록해놓을 뿐이다.


**결론 : 유저가 채팅방 목록 혹은 채팅방 안에서 채팅방 나가기를 할 떄 아래와 같은 엔드포인트로 서버로 요청한다. **


 **URL** : `/chat/room/{room-id}` 

 **Method** : `PATCH`
 
 **Authentication required** : `yes`
 
 **Body** :  
 ```json

{
    "room_leave": true
}

```
 
 ## Success Responses

**Content**

room leave success 


해당 메시지를 받으면 성공적으로 채팅방으로 나갔다는 뜻이므로 클라이언트는 
DOM에서 해당 채팅방목록을 삭제한다.
1.채팅방목록에서 채팅방나가기한다면 해당 채팅방 목록 DOM 을 지운다.
2.채팅방에서 채팅방나가기를 클릭해 위의 응답메시지를 받으면
    -> 채팅방에서 채팅방 목록으로 뒤로가기 한다. (웹소켓에 대한 구독요청을 모두 취소한다.)
    -> 그리고 새롭게 채팅방 목록에 대한 요청(http)을 한다. 