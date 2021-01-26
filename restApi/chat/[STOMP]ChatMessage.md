---
STOMP - Chat created by MS


수정사항
21-01-25(ms)

기존 3개구독 -> 5개구독으로 변경
app2.js 에 변경된 샘플 클라이언트 코드 참조
상세내용

추가된 구독형태
1.`[SUBSCRIBE] /user/queue/error` 
2.`[SUBSCRIBE] /user/queue/room/event`

### 1. `[SUBSCRIBE] /user/queue/error`
에러메시지를 담고 있는 MESSAGE 프레임을 받기위함 (가령 차단된상대에게 메시지를 보내면, sub 콜벡부분에 서버가 에러코드와 메시지를 던달한다.)

 
### 2. `[SUBSCRIBE] /user/queue/room/event`
1.채팅방입장시(4번 구독하는 경우, 채팅방 화면에 들어가는 경우) 모든 메시지를 받기위함(실제로 이제 이곳으로 메시지들을 보내준다) + 2.채팅거래하기 누를 때 새로운 채팅방에 대한 정보를 받기위함


---

## STOMP
 
- [1.`[CONNECT] /ws`](#1-connect-ws)
- [추가1 `[SUBSCRIBE] /user/queue/error`](#-1-subscribe-userqueueerror) <- 에러메시지를 담고 있는 MESSAGE 프레임을 받기위함 (가령 차단된상대에게 메시지를 보내면, sub 콜벡부분에 서버가 에러코드와 메시지를 던달한다.)
- [추가2 `[SUBSCRIBE] /user/queue/room/event`](#-2-subscribe-userqueueroomevent) <- 1.채팅방입장시(4번) 모든 메시지를 받기위함 + 2.채팅거래하기 누를 때 새로운 채팅방에 대한 정보를 받기위함
- [2.`[SUBSCRIBE] /topic/chat/{user-id}`](#2-subscribe-topicchatuser-id) <- 채팅방에 들어가 있지 않아도 메시지를 받기 위함 
- [3.`[SUBSCRIBE] /topic/room/{room-id}, header={ id: "room-" + {room-id}}`](#3-subscribe-topicroomroom-id--header-id-room---room-id) ex) id:room-10 <- 실시간으로 오는 메시지를 받기 위함
- [4.`[SUBSCRIBE] /topic/room/{room-id}/{user-id}, header={id: "room-user-" + room_id + "-" + userid}`](#4-subscribe-topicroomroom-iduser-id-headerid-room-user---room_id-----userid-ex-id-room-user-10-1) ex) id: room-user-10-1 <- 다른유저의 join 이벤트를 받기 위함  
- [5.`[SEND] /message, body={productId: {pid}, senderId: {userId}, receiverId: {receiverId}, messageType: {messageType}, message: {message}}`](#5-send-message-bodyproductid-pid-senderid-userid-receiverid-receiverid-messagetype-messagetype-message-message)
- [6. `[UNSUBSCRIBE] 2번과 3번에 대해서 UNSUB`](#6-unsubscribe-3-4--unsub)


사전 설명
클라이언트는 어디에 있던(플로팅버튼이 있는 화면, 채팅방목록화면, 채팅방화면) 누군가로 부터 채팅 메시지가 왔을 경우, 메시지를 수신하여 적절한 처리를 통해 DOM에 그
려줘야 한다. 
### 1. [CONNECT] /ws
#### 설명 :
웹소켓과의 연결을 시도한다( 정확히 이야기 하면 서버로 `CONNECT` Frame 을 보내는 과정 )
#### 웹소켓 연결하기 ( 이 부분은 인증과정이 들어가서 수정될 수 있다. 수정은 헤더에 어떤 정보를 추가하는 방면으로 )

### 추가-1 [SUBSCRIBE] /user/queue/error
#### 설명 
클라이언트가 잘못된 메시지를 보내었을 경우 서버로부터오는 여러 에러메시지내용이 담긴 MESSAGE 프레임을 받을 수 있는 구독이다.
가령, 유저가 차단된 유저에게 메시지를 보내는 경우( `SEND` /message ) 서버는 에러메시지코드와 에러메시지 내용을 담아 메시지를 보냈던 유저에게
MESSAGE Frame 을 전달한다.

가령 아래는 자신이 차단한 유저에게 메시지를 보내었을 경우 서버로 부터 오는 메시지의 형태이다.

어떤 에러코드가 있고 어떻게 클라이언트가 에러메시지를 받고 처리해야하는 지는 [error_code.md](error_code.md) 를 참고하면 된다. 
 
**body**

`error_code`: 에러코드 ( 에러코드는 순전히 서버가 정한 것, 에러코드에 대한 내용 역시 정리해서 따로 올릴예정이다. )
`error_description` : 에러 상세 설명 ( 어떤 에러인지 설명되어 있다. 아래의 예시의 경우에는 "내가 차단한 유저에게 메시지를 보낼 수 없다" 라는 에러메시지를 담고있다.)

**example** 

```
<<< MESSAGE
destination:/user/queue/error
content-type:application/json
subscription:sub-1
message-id:4a5uk4km-13
content-length:83

{"error_code":1,"error_description":"You cannot send messages to user you blocked"}
```


### 추가-2 [SUBSCRIBE] /user/queue/room/event 
#### 설명 
채팅방에 대해 구독(4번)을 하게 되면 REDIS 채팅방에 가입하게 되는 데 이때 서버는 누군가가 같은 채팅방에 존재할 경우 상대방에게 채팅방에 내가 들어왔다는 메시지를
전송하게 되는 데 해당 메시지를 받아서 처리하는 곳이다.
그리고 새롭게 채팅방을 만들었을 때 채팅방에 대한 정보를 받는 것도 해당 구독에서 처리한다.

기존에는 /topic/chat/{user-id} 에서 받았으나, 여러 문제가 생겨 수정하게 되었다. 
가령 1번유저가 크롬브라우저에서 로그인 구독요청 : /topic/chat/1
1번유저가 파이어폭스에서 로그인 구독요청 : /topic/chat/1 
이때 크롬브라우저로 로그인한 유저가 채팅으로 거래하기를 통해 새롭게 채팅방을 만들고 서버가 채팅방정보를 전달하게 되면
/topic/chat/1 로 전달하게 된다. 문제는 여기서 발생했다. 특정 유저라고 생각했던 구독 대상 형태가 실제로는 파이어폭스에서 로그인한 유저에게도
채팅방에 대한 정보가 가게 된다. 

**결론 
**1. 채팅방 구독 시(4번, 즉 채팅방으로 들어갈 때) 해당 채팅방에 대한 모든 메시지들을 받는다.**
**2. 새롭게 채팅방을 만들었을 때 채팅방에 대한 정보를 받는다.**

**destination url** : `/user/queue/room/event` 

**Frame** : `SUBSCRIPTION`

#### 요청 시점 : 
1. 웹소켓 성공적으로 연결 후 실행되는 콜백부분에서 요청

#### 수신되는 메시지 형태(2가지 이다) :

* 구독하는 순간 서버로부터 채팅방에 대한 모든 메시지들을 받게 된다. 

`roomId`: 채팅메시지가 속한 채팅방 고유 ID (채팅방 고유아이디는 생성되는 채팅목록 DOM 들의 고유한 이름으로 사용한다. )

`messageType` : 메시지의 타입(0: 텍스트, 1: 이미지)

`messageStatus` : 메시지 읽음여부(0: 읽지않음, 1:읽음)

`message` : 메시지 내용 

`messageDate` :  메시지 보낸 날짜

`chatMessageUserDto`: 

​		`userId` : 메시지 보낸 이 고유 유저 아이디

​		`nickName` : 메시지 보낸 유저의 닉네임

​		`profileImgPath` : 메시지 보낸 유저의 닉네임의 프로필 경로

```json
[
  {
    "messageType": 0,
    "messageStatus": 1,
    "message": "minshik2233",
    "messageDate": "2021-01-12T17:22:23",
    "chatMessageUserDto": {
      "userId": 24,
      "nickName": "dangdang",
      "profileImgPath": "/imgs/slideshow_sample.jpg"
    }
  },
  {
    "messageType": 0,
    "messageStatus": 1,
    "message": "minshik2233",
    "messageDate": "2021-01-12T17:22:23",
    "chatMessageUserDto": {
      "userId": 24,
      "nickName": "dangdang",
      "profileImgPath": "/imgs/slideshow_sample.jpg"
    }
  },
  {
    "messageType": 0,
    "messageStatus": 1,
    "message": "minshik2233",
    "messageDate": "2021-01-12T17:22:23",
    "chatMessageUserDto": {
      "userId": 24,
      "nickName": "dangdang",
      "profileImgPath": "/imgs/slideshow_sample.jpg"
    }
  },
  {
    "messageType": 0,
    "messageStatus": 1,
    "message": "minshik2233",
    "messageDate": "2021-01-12T17:22:22",
    "chatMessageUserDto": {
      "userId": 24,
      "nickName": "dangdang",
      "profileImgPath": "/imgs/slideshow_sample.jpg"
    }
  }
]   
```


2. 물건 페이지에서 [채팅으로 거래하기] 누른 후 채팅방에서 메시지를 보내고(`/message`) 받게 되는 메시지 형태
```json
    { "roomId" : 101 }
```
기존에는 채팅방에 메시지를 보내기 전에 먼저 채팅방에 들어가 채팅방에 대한 메시지를 받기 위해 `/topic/room/{room-id}`, `/topic/room/{room-id}/{user-id}`에 대해 구독하는 
형태였다.     
그렇기 떄문에 이 경우에는 사용자(나)가 메시지를 보내게 되면 서버에서 메시지를 저장하기 전에 먼저 채팅방이 존재하는 지 확인한 후 채팅방이 없다면 채팅방을 새롭게 만든다. 그리고 나서
방금 보낸 메시지를 방금만든 채팅방 번호를 포함해서 메시지를 저장하게 된다. 그리고 다시 사용자(나)에게 어떤 채팅방이 만들어졌는 지에 대한 정보를 보내게 된다. 이 경우 서버는
초반에 유저가 구독한 `/user/queue/room/event` (특정 유저)로 방금 만들어진 만들어진 채팅방번호 정보`{ "roomId" : 101 }`를 전달한다.  
이와 같이 방번호 : 방고유아이디 형태의 메세지를 받게 되면(물론 `SUB /user/queue/room/event`의 콜백부분에서) 해당 정보를 이용해 다시 채팅방에
대해서 `/topic/room/101` , `/topic/room/101/{user-id}` 로 구독요청을 하게된다.
 



### 2. [SUBSCRIBE] /topic/chat/{user-id}
#### 설명 : 
해당 엔드포인트로 구독을 맺게되면 `topic/chat/{user-id}` 로 오는 메시지는 실시간으로 받을 수 있다. 즉, 나에게 오는 메시지를 받기 위해 존재한다.

#### 전체 메시지 구독 요청하기
**destination url** : `/topic/chat/{user-id}` ex) 사용자 고유 번호가 1번인 경우, /topic/chat/1

**Frame** : `SUBSCRIPTION`

#### 요청 시점 : 
로그인 이후 웹소켓 연결을 맺는 과정(1번)이 성공적으로 이루어지면 서버로 해당 엔드포인트로 구독을 요청한다.

#### 수신되는 메시지 형태(1가지 이다 )
- 기존에는 2가지 였으나 새로운방정보를 받아 처리하는 곳은 `SUB /user/queue/room/event` 로 바뀌었다. (2021.1.25) 
 
 
1. 누군가 나에게 메시지를 보냈을 경우(상대방은 채팅방에 들어가 메시지를 보냈을 것이다.)

`roomId`: 채팅메시지가 속한 채팅방 고유 ID (채팅방 고유아이디는 생성되는 채팅목록 DOM 들의 고유한 이름으로 사용한다. )

`messageType` : 메시지의 타입(0: 텍스트, 1: 이미지)

`message` : 메시지 내용 

`messageDate` :  메시지 보낸 날짜

`chatMessageUserDto`: 
​		`userId` : 메시지 보낸 이 고유 유저 아이디

​		`nickName` : 메시지 보낸 유저의 닉네임

​		`profileImgPath` : 메시지 보낸 유저의 닉네임의 프로필 경로

`chatRoomProductDto` : 채팅방의 물건 정보

​	    `product_id` : 물건 고유번호

​	    `product_deleted` : 물건 삭제여부(0 : 삭제안함, 1 : 삭제함) 

​	    `productImgPath` : 물건 이미지 사진 경로 (개별 채팅방 왼쪽에 물건이미지가 저장된 이미지 경로, 가져와서 해당부분에 렌더링)

```json
   {
     "roomId": 101,
     "messageType": 0,
     "messageStatus": 0,
     "message": "minshik",
     "messageDate": "2021-01-12T21:22:52.3008765",
     "chatMessageUserDto": {
       "userId": 24,
       "nickName": "dangdang",
       "profileImgPath": "/imgs/slideshow_sample.jpg"
     },
     "chatRoomProductDto": {
       "product_id": 1,
       "product_deleted": 0,
       "productImgPath": "/imgs/slideshow_sample.jpg"
     }
   }
```






### 3. #[SUBSCRIBE] /topic/room/{room-id} , header={ id: "room-" + {room-id}}
#### 설명 : 
해당 엔드포인트로 구독을 맺게되면 `topic/room/{user-id}` 로 오는 메시지는 실시간으로 받을 수 있다. 채팅방으로 오는 메시지를 받을 수 있다.

### 채팅방에 대해 구독하기
**destination url** : `/topic/room/{room-id}` ex) 채팅방 번호가 3번인 경우, /topic/room/3

**Frame** : `SUBSCRIPTION`

**Header** : `{id: "room-{room-id}}` ex) 채팅방 번호가 3번인 경우, {id: room-3}

#### 요청 시점 : 
1.채팅방 목록에서 특정 채팅방에 들어가는 순간
2.` [SUBSCRIBE] /user/queue/room/event` 의 메시지를 받는 콜백메서드 부분에서 `{ "roomId" : 101 }` 와 같은 형태의 메시지를 받았을 떄, 101 번 채팅방 번호를 
받아서 채팅방에 대해 구독요청을 하려고 할 떄

#### 수신되는 메시지 형태(1가지 이다) :

`messageType` : 메시지의 타입(0: 텍스트, 1: 이미지)

`messageStatus` : 메시지 읽음여부(0: 읽지않음, 1:읽음)

`message` : 메시지 내용 

`messageDate` :  메시지 보낸 날짜

`chatMessageUserDto`: 
​		`userId` : 메시지 보낸 이 고유 유저 아이디

​		`nickName` : 메시지 보낸 유저의 닉네임

​		`profileImgPath` : 메시지 보낸 유저의 닉네임의 프로필 경로

`chatRoomProductDto` : 채팅방의 물건 정보

​	    `product_id` : 물건 고유 ID (내가 채팅방 목록에 있고 새로운 메시지(new 채팅방)가 올 경우 DOM에 그리면서 class or id 이름에 넣어야하기 떄문!, 이후 채팅방 들어가서 메시지보낼 떄 필요)

​	    `product_deleted` : 물건 삭제여부(0 : 삭제안함, 1 : 삭제함) 

​	    `productImgPath` : 물건 이미지 사진 경로 (개별 채팅방 왼쪽에 물건이미지가 저장된 이미지 경로, 가져와서 해당부분에 렌더링)


```json
   {
     "roomId": 101,
     "messageType": 0,
     "messageStatus": 0,
     "message": "minshik",
     "messageDate": "2021-01-12T21:22:52.3008765",
     "chatMessageUserDto": {
       "userId": 24,
       "nickName": "dangdang",
       "profileImgPath": "/imgs/slideshow_sample.jpg"
     },
     "chatRoomProductDto": {
       "product_id": 1,
       "product_deleted": 0,
       "productImgPath": "/imgs/slideshow_sample.jpg"
     }
   }
```

유의할 점은 내가 `SEND /message`로 보낸 메시지는 `SUB /topic/room/{room-id}` 로 다시 받는다. 
그렇기 때문에 `SUB /topic/room/{room-id}` 로 받는 메시지는 
1.내가 `SEND /message` 로 메시지를 보낸 나의 메시지
2.상대방이 `SEND /message` 로 나에게 보낸 메시지 
이렇게 두 가지가 있다.  


### 4. [SUBSCRIBE] /topic/room/{room-id}/{user-id}, header={id: "room-user-" + room_id + "-" + userid}` ex) id: room-user-10-1

#### 설명 : 
해당 엔드포인트는 서버로 채팅방에 가입 및 메시지 읽지않음 상태를 읽음상태로 바꾸며, 상대방(채팅방에 있을 경우) 혹은 반대로 내가 상대방에게 메시지를 보낸 후에
실시간으로 채팅방에 있을 경우 읽지않음 메시지가 읽음형태로 바뀌게 하기 위해서 존재한다. 
헤더의 경우는 어떤 유저가 어떤 방에 대해서 구독취소를 하는지에 대한 정보를 서버가 알 수있도록 하기 위해서다(구독취소할 떄 구독했던 정보를 활용하게 됨)

즉, 
해당 구독요청을 보내게 되면 서버는 구독 요청메시지를 받아 어떤유저가 어떤방에 들어왔는 지 파악하여 상대방이 만약 10개의 메시지를 보내고 내가 그것을 읽지 않은 상태였다면
읽음 상태로(0 -> 1) 바꾸게 되는 과정을 거치고, 그 이후 상대방이 채팅방에 존재(채팅방 화면에 있다면) 실시간으로 상대방에게 읽지않음메시지가 읽음 형태로 바뀌게끔 
해야한다. 마찬가지로 유저(나)가 채팅방에 들어가 상대방에게 메시지를 10개 보낸 상태이다. 이떄 상대방은 채팅방에 있지 않은 상태라고 가정하다. 그렇다면 메시지는 읽지 않음 상태로 
남아있을 것이다. 이떄 만약 상대방이 채팅방에 입장한다면 서버는 상대방이 채팅방에 입장(상대방 또한 채팅방 화면에 있음)했다는 사실은 위의 엔드포인트로 메시지를 전달다.

### 채팅방 + 유저아이디 에 대해 구독하기
**destination url** : `/topic/room/{room-id}/{user-id}` ex) 유저아이디가 1이고 채팅방 번호가 3번인 경우, /topic/room/3/1

**Frame** : `SUBSCRIPTION`

**Header** : `{id: "room-user-{room-id}-{user-id}` ex) 유저아이디가 1이고 채팅방 번호가 3번인 경우, {id: room-user-3-1}

#### 요청 시점 : 
1.채팅방 목록에서 특정 채팅방에 들어가는 순간
2.` [SUBSCRIBE] /topic/chat/{user-id}` 의 메시지를 받는 콜백메서드 부분에서 `{ "roomId" : 101 }` 와 같은 형태의 메시지를 받았을 떄

#### 수신되는 메시지 형태(1가지 이다) :


* 누군가 나의 채팅방에 들어왔다는 정보를 사용자에게 알려준다.(즉, 내가 보낸 메시지들을 읽었고 채팅방에 들어왔다는 뜻)

해당메시지를 받는 다면 채팅방에 존재하고 있는 읽지않음 메시지들은 읽음으로 바꿔야한다.(개별 채팅메시지 DOM 의 읽지않음 상태 -> 읽음 상태로)

`who` : 채팅방에 들어온 상대방 고유 ID
 
`event` :  들어온 이벤트(join 밖에 없음)

```json

{"who": "24", "event": "join"}

```

### 5. `[SEND] /message, body={productId: {pid}, senderId: {userId}, receiverId: {receiverId}, messageType: {messageType}, message: {message}}`

#### 설명 : 
채팅방에 들어가 상대방에게 메시지를 보낼 떄 하는 요청이다.

#### 메시지 보내기: 
 
 **destination url** : `/message` 
 
 **Frame** : `SEND`
 
 **Body** : 
 ```json 

{
    "productId" : {product-id}, // 채팅방에서의 물건 고유 ID
    "receiverId" :  {receiver-id}, // 메시지 받을 상대방 고유 ID
    "messageType" : {message-type}. // 메시지 타입
    "message" : {message} // 메시지 내용
} 

```

#### 요청 시점 : 
채팅방에 들어가 메시지를 입력하여 보내기 버튼을 누를 떄

위의 엔드포인트로 `SEND` 프레임을 보내게 되면 해당 메시지는 다시 나에게로 전달되는 구조이다.
전달되는 대상은 채팅방에 들어오면서 구독을 맺었던 `SUBSCRIBE topic/room/{room-id}` 이고, 해당 부분의 콜백부분에서
`SEND /message` 로 보낸 메시지를 받게 된다. `SUBSCRIBE topic/room/{room-id}` 에서 메시지를 받아 채팅메시지 형태로 DOM에 그려줘야할 것이다. 



### 6. `[UNSUBSCRIBE] 3번과 4번에 대해서 UNSUB`

#### 설명 : 
채팅방에 들어갈 떄 맺었던 채팅방에 대한 구독요청을 취소하는 과정이다. 


#### 요청 시점 : 
채팅방에 있다가 채팅목록 화면으로 나갈 떄 실행한다.

해당 채팅방(`/topic/room/{room-id}`, `/topic/room/{room-id}/{user-id}`)으로 부터 오는 메시지를 더 이상 받지 않겠다는 의미(조심해야할 것은, 이떄 더 이상 메시지를 받지 않는 다는 것은 해당 채팅방에 대해 영원히 메시지를 받지 않겠다는 의미가 아니다. 
정확히 말하면 서버내부에 실시간으로 관리되는 채팅방에서 나가겠다는 의미이다.) UNSUB 과정은 SUBSCRIBE 맺었던 STOMP CLIENT 객체에 unsubscribe 메소드를 호출만 하면 된다.
즉 두개의 StompClient Sub 객체(`topic/room/{room-id}` 그리고 `/topic/room/{room-id}/{user-id}`에 대해 구독맺었던) 에 unsubscribe 메소드를 호출하면 된다.


**갑작스럽게 종료할 경우**

`SUSCRIBE /topic/room/{room-id}/{user-id}` 에 대해서 unsubscribe 했을 경우가 중요하다.
왜냐하면 클라이언트 해당 SUBSCRIBE 에 대해서 UNSUBSCRIBE 하여 채팅방 (redis) 에서 정상적으로 나가게 할 수 있기 떄문이다. 
그렇기 때문에 
내가 채팅방에 들어온 상태(`SUBSRIBE /topic/room/{room-id}/{user-id}` 구독을 맺고 있는 상태임)에서 갑작스럽게 종료할 경우에는
바로 DISCONNECT frame 을 보내기 떄문에 서버는 UNSUB 메시지를 받지 못하게 된다. 그렇기 떄문에 클라이언트는
브라우저 종료시에 UNSUB를 할 수 있도록 조치를 해야한다. 해당 코드는 app2.js 에 있다. 참고할 것 


