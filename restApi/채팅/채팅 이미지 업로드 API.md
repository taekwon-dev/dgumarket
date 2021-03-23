# 채팅 이미지 업로드 ( 3. 22 ) 


### 1. 채팅 이미지 렌더링 관련 설명
채팅의 이미지의 경우 url + 저장된파일명을 활용하여 저장된 이미지 파일을 렌더링 하는 방식을 사용합니다(물건에 있는 이미지와 같음)
최초에 채팅방 들어가서 메시지들을 불러올 때 메시지타입이 1인경우, 또는 채팅방에 있을 때 상대방이 보낸 메시지의 타입1 인 경우에는
message 내용에 2e33bbdf91bb4ed88551afade799e7e9.jpeg 와 같이 전체이미지 경로가 아닌 파일명+확장자만 있습니다.
그렇기 때문에 요청할 때는 url + 2e33bbdf91bb4ed88551afade799e7e9.jpeg 로 최종 s3의 이미지가 저장되어 있는 경로를
만들어준 후 요청합니다. (여기서 요청이라는 표현은 image 태그의 src 안에 들어갔을 때를 의미합니다.)
   
처음에는 리사이즈된 채팅이미지를 요청합니다.
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/400x400px/031ff210394943d7a92dd3767da0b09f.jpeg

만약 해당 url로 요청했는 데 이미지가 없다면 아래로 이미지를 요청합니다.
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/031ff210394943d7a92dd3767da0b09f.jpeg

즉
리사이즈경로 + 이미지타입의 채팅메시지의 이미지파일명
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/400x400px/ + 031ff210394943d7a92dd3767da0b09f.jpeg

만약 없다면 원본이미지 요청
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/ + 031ff210394943d7a92dd3767da0b09f.jpeg




### 2. 채팅이미지 클릭
채팅방에 있는 메시지들중 채팅이미지를 클릭하여 해당 이미지를 불러올 때도 위와 같습니다.
이 경우는 최초에 클릭 시 1000x1000px 를 요청하고 없을 경우 원본사이즈의 채팅이미지를 요청합니다.
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/1000x1000px/031ff210394943d7a92dd3767da0b09f.jpeg

만약 위의 url로 요청했는 데 이미지가 없을 경우에는
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/031ff210394943d7a92dd3767da0b09f.jpeg
와 같은 경로로 이미지를 요청합니다. 

**유의 : 이미지를 클릭해서 보는 경우에는 반드시 다른 탭을 추가하여 그곳에서 이미지를 볼 수 있도록 합니다**




### 3. 채팅이미지 업로드 설명 :
채팅방에서 이미지를 업로드 합니다. 
이미지(사진)은 최대 10장을 올릴 수 있으며, 허용되는 이미지의 크기는 장당 5mb 로서 최대 50mb까지 한꺼번에 업로드 가능합니다.
클라이언트를 허용되는 이미지 jpg,png,jpeg 확장자의 이미지파일만 가능하도록 예외처리를 해야합니다.





**URL** : `/api/multi-img/upload`

**Method** : `POST`

**Authentication required** : `yes`

**Request body(form-data)** : 

`files` : 선택한 파일객체들,

`uploadDirPrefix` : origin/chat(채팅이미지가 업로드될 prefix 경로입니다. 고정)

`senderId` : 보내는 이 고유 아이디(이 경우에는 로그인한 본인의 고유 아이디가 되겠죠)

`receiverId` : 메시지 받는 대상 고유 아이디

`productId` : 물건 고유 아이디

이전에 stomp 프로토콜 이용하여 `/message` 경로 메시지를 전송(SEND)할 때 물건아이디, 보내는아이디, 받는 이 고유아이디를  
넣었던 것과 같습니다! 다만 여기에 `sessionId` 가 추가됩니다. 

`sessionId` : 웹소켓 고유 세션 아이디

`sessionId` 는 웹소켓 연결 시 활용되는 고유의 웹소켓 세션 아이디 입니다. 실제로 클라이언트가 직접적으로 활용하지는 않았지만
서버에서는 이미 이 값을 사용해서 메시지를 주고받고 했었습니다.그때는 STOMP프로토콜 내에서 메시지를 주고 받았기 때문에
이미 메시지를 주고 받는 헤더에 고유의 웹소켓 세션아이디가 포함되어있어서 활용가능했습니다. 그러나 지금의 상황은
웹소켓 프로토콜이 아닌 HTTP POST방식 으로 전달하다 보니 유저의 고유 웹소켓 세션아이디를 알 수 없게됩니다.
각설하고 결론은 우리는 이미지를 전송할 때 고유의 웹소켓 세션 아이디를 같이 전달해야합니다. 

웹소켓 세션 아이디를 STOMP 최초로 커넥션 했을 때 얻을 수 있습니다.(엄밀히 말하면 커넥션 성공과 상관없이 얻을 수 있으나, 안정하게 웹소켓
연결성공했을 때의 웹소켓 세션아이디를 저장하도록 합니다.)
아래의 코드를 보면(app2.js 에 해당 코드 부분 있음), stompClient 객체로 부터 고유의 sessionId 를 추출하는 과정입니다.
이 과정을 통해 해당 유저의 고유 소켓 sessionId 를 얻을 수입니다.

sessionId는 고정으로 지정해야하나요? 그렇지 않습니다. 해당유저를 식별하는 소켓세션아이디에 불과합니다. 그리고 해당 정보를 통해
어떤 유저에게 메시지를 전달해야할 지를 결정합니다.
만약 새로고침을 하게되면 새롭게 연결될테고 그럴 때마다 당연히 저장되는 sessionId는 바뀌게 될것입니다.

해당 세션아이디를 가지고 있다가 채팅이미지를 보낼 때 활용하시면 됩니다. 


```javascript
    
    var sessionId = null;
    
    (중략)

    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        var url = stompClient.ws._transport.url;
        url = url.replace("ws://localhost:8080/ws", "");
        url = url.replace(/[0-9]+\//, "");
        url = url.replace("/websocket", "");
        url = url.replace("/", "");
        sessionId = url; // 얻어낸 웹소켓 고유 세션 아이디
        
        
    }

```




## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지

`status`: 응답 상태

`data`: 업로드된 이미지들 파일 이름(string)


**example**

```json


{
    "message": "AWS 복수 이미지 업로드 성공",
    "status": 200,
    "data": "[2e33bbdf91bb4ed88551afade799e7e9.jpeg, fd24969ea1624b0fae9e4d42a4023cbf.jpeg, 4e5cff224b5e4689abba5f2201df7707.jpeg, 2b7a775ce97a4c13addff97ff4a702d3.jpeg, 7f665ff0652f4a85bac19b35f303e85b.heic, 660a5084a97b4d6e96962cf380b330f5.jpeg]"
}



```


Q1. POST 방식으로 이미지를 전송하였을 때 위와 같은 응답이 나오게 되는 데 이건 어디에 활용하나요?

A1: 활용하지 않습니다. 




1) 채팅으로 거래하기(최초로 거래하는 데 바로 이미지를 첨부하여 거래 시도)

텍스트 메시지를 통해 채팅으로 거래하기를 했을 때와 상황은 비슷합니다.
만약 처음부터 이미지업로드를 하게 된다면 이미지를 업로드 -> (서버) 채팅방유무 검사 -> 없을 경우 채팅방 만들기 -> 클라이언트로 채팅방 정보 제공
-> 해당 방에대해서 구독하기
만약 5개의 이미지를 처음부터 올렸다면 5개의 이미지를 받개 될 뿐입니다. 



2) 이미 존재하고 있는 채팅방에서 이미지를 전송하는 경우

/topic/room/{채팅방번호} 로 메시지가 올것입니다. 
즉, 10개의 이미지를 올렸다면 /topic/room/{채팅방번호} (이미 채팅방에 들어간 상태이기 때문에 해당 채팅방에 대해서는 구독을 하고 있을 겁니다. 그렇기 때문에 메시지를 
받을 수 있습니다.) 로 메시지가 올것입니다.
기존에 채팅방에서 텍스트로 채팅메시지를 보냈을 때도 /topic/room/{채팅방번호}로 메시지를 받은 것처럼 똑같이 해당 경로로 내가 보낸 채팅메시지를 받게 됩니다.
다만 이전에는 "stomp 프로토콜 내에서 메시지 전송 -> stomp 프로토콜 로 받기" 였다면 지금은
"Http 프로토콜, post 방식의 이미지 전송 -> stomp 프로토콜로 받기" 형태로 볼 수 있습니다.





### 예외 response


1) 이미지 전송 시 내가 상대방을 차단했을 경우


```json

{
    "statusCode": 400,
    "timestamp": "2021-03-11T03:57:31.079+00:00",
    "message": "차단한 유저와는 메시지를 주고 받을 수 없습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```



2) 이미지 전송 시 상대방이 나를 차단했을 경우


```json

{
    "statusCode": 400,
    "timestamp": "2021-03-11T03:57:31.079+00:00",
    "message": "해당유저로 부터 차단당했을 경우 메시지를 주고 받을 수 없습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```


3) 이미지 전송 시 서로 차단한 관계일 경우


```json

{
    "statusCode": 400,
    "timestamp": "2021-03-11T03:57:31.079+00:00",
    "message": "차단한 유저와는 메시지를 주고 받을 수 없습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```


4) 이미지 전송 시 상대방이 탈퇴한 유저였을 경우


```json

{
    "statusCode": 404,
    "timestamp": "2021-03-11T03:57:31.079+00:00",
    "message": "해당유저는 존재하지 않습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```



5) 이미지 전송 시 상대방이 관리자로 부터 유저제재를 당했을 경우


```json

{
    "statusCode": 400,
    "timestamp": "2021-03-11T03:57:31.079+00:00",
    "message": "해당 유저는 관리자로 부터 이용제재 처분을 받고 있습니다. 메시지를 전송할 수 없습니다.",
    "description": "uri=/api/chatroom/product/4"
}

```