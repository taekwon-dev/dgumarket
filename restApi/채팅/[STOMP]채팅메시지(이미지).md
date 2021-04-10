# [STOMP] 채팅메시지(이미지)


### 결론 
**기존에 사용하는 복수 이미지 업로드 API를 사용합니다. 다만, 채팅이미지 업로드를 올리는 경우에 한해서 요청필드가 하나 더 추가됩니다.**


크게 
1. 채팅 메시지 중 이미지와 관련된 메시지일 경우 렌더링 설명
2. 채팅방에 들어가 채팅 이미지를 전송하는 부분
3. 채팅이미지를 클릭하는 부분


## 1. 채팅 이미지 렌더링 관련 설명

**하나씩 메시지를 받을 때나, 채팅방에 들어가서 메시지를 받을 때도 역시 똑같은 로직을 사용합니다**

채팅의 이미지의 경우 url + 파일명을 활용하여 저장된 채팅 이미지 파일을 렌더링한다.
최초에 채팅방 들어가서 메시지들을 불러올 때 메시지타입(`message_type`)이 1인경우, 또는 채팅방에 있을 때 받은 메시지중 메시지의 타입(`message_type`)이 1인 경우에는
url + 2e33bbdf91bb4ed88551afade799e7e9.jpeg 로 최종 s3의 이미지가 저장되어 있는 경로를
만들어준 후 요청합니다. (여기서 요청이라는 표현은 image 태그의 src 안에 들어갔을 때를 의미합니다.)

**url** : 
1. https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/400x400px/
2. https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/

실제로, message 내용에 2e33bbdf91bb4ed88551afade799e7e9.jpeg 와 같이 전체이미지 경로가 아닌 파일명+확장자만 있습니다.


처음에는 리사이즈된 채팅이미지를 요청합니다.
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/400x400px/031ff210394943d7a92dd3767da0b09f.jpeg

만약 해당 url로 요청했는 데 이미지가 없다면 아래로 이미지를 요청합니다.
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/031ff210394943d7a92dd3767da0b09f.jpeg

즉
리사이즈경로 + 이미지타입의 채팅메시지의 이미지파일명
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/400x400px/ + 031ff210394943d7a92dd3767da0b09f.jpeg

만약 없다면 원본이미지 요청
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/ + 031ff210394943d7a92dd3767da0b09f.jpeg

`{
   "roomId": 1,
   "message_type": 1,
   "messageStatus": 1,
   "message": "495d06d7c2b44db0b7feb63c3355e326.jpeg",
   "messageDate": "2021-01-12T17:22:23",
   "chatMessageUserDto": {
   "userId": 24,
   "nickName": "dangdang",
   "profileImgPath": "/imgs/slideshow_sample.jpg"
   }
}`

물론 그럴 가능성은 없겠지만 **원본파일마저 없을 경우도 고려해야한다**. 그럴경우에는 [X] 와 같이 기본이미지를 활용하여 렌더링을 하면 될 것이다. 



## 2. 채팅방에 들어가 채팅 이미지를 전송하는 부분

이 문서는 채팅방에서 이미지를 주고 받기 위해서 필요한 것들이 무엇이고 어떻게 해야하는 지에 대한 설명을 담고있습니다.
지금까지는 텍스트메시지만 주고 받았고 텍스트 메시지의 경우에는 직접 STOMP 프로토콜로 주고 받는 것이 가능했습니다.
하지만 이미지의 경우에는 다른방식으로 접근합니다.(이미지를 먼저 HTTP POST 로 올리고 난 후 그것에 대한 성공값+파일명리스트 를 활용하여 다시 STOMP 메시지를 보냅니다)

**허용 이미지 용량과 최대업로드 허용 장수 **
이미지의 경우 장당 최대 5mb 한번에 최대 5장까지 업로드 가능하도록 해야하며, 만약 6장을 선택하거나 5장을 선택했는 데 용량이 25mb 이상이라면 경고창을 띄우며 업로드를 할 수 없도록 한다.
(장당 5mb, 최대 업로드 수를 5장으로 제한한 이유는 현실적인 이유입니다. 실제로 장당 10mb에 최대 10까지 허용하였을 때 15~20초사이의 업로드 시간이 걸리며, 과연 10장까지 허용할 필요가 있을 까? 라는 생각에서 우선 최대 5장, 장당 최대 허용용량 5mb로 하였습니다.)

예를 들어 설명하겠습니다.
A유저가 1번방에서 B(기존에 1번방에서 대화를 나누었던 상대방)에게 이미지를 전송하려고 하는 과정 설명드리겠습니다.

1. A유저가 첨부파일 버튼을 눌러 전송하고 싶은 이미지를 클릭 후 열기 버튼을 누릅니다.( 이 시점에 http 프로토콜을 이용 상대방에게 이미지를 전송합니다(정확히는 파일을 그냥 올리는 과정입니다.) )
2. POST 방식으로 바디에 선택한 이미지파일들 그리고 **targetId** 라는 필드를 요청바디에 추가하고 채팅 상대방 유저 고유 아이디를 값으로 넣습니다.
   기존에 사용하던 복수이미지 업로드 API 를 사용합니다. 다만 달라지는 것은 요청바디 필드 'uploadDirPrefix' => 'origin/chat/'
   그리고 새로운 필드값 'targetId(상대방고유id)' 값이 추가된다는 것입니다. 이 부분은 밑에서 다시 상세히 설명하겠습니다.
3. 이미지 전송(post)하고 응답을 받을 때 까지 오래 걸릴 수 있기 때문에 클라이언트에서는 현재 이미지가 올라가고 있는 중이라는 로딩이미지를 보여줍니다.
4. 업로드가 성공하여 파일명 리스트를 받으면 받은 파일명 리스트(str)를 그.대.로 채팅메시지 만들 때 message 필드의 값으로 넣고, 메시지타입은 1로 만든 후(나머지는 
   값들은 기존 텍스트 메시지를 보낼 때와 같습니다.) STOMP 프로토콜로 전송할 채팅메시지를 만듭니다.
5. `/message`로 만든 메시지를 전송합니다. 
6. 그 이후의 과정은 기존에 텍스트로 메시지를 전송하여 받는 과정과 같습니다. 다만 이 경우는 메시지들의 메시지타입이 1 이라는 것과, 메시지내용에는 파일명이 있는 채로
받게 되기 때문에, 메시지를 받아 채팅방에 렌더링시에 이 부분만 유의하면 됩니다. 

### request param(body)

// 이미지 파일명 아닙니다. 이미지 파일입니다.
"files" : 이미지 파일(최소 한 장 이상)

// 이미지 저장 경로 값은 아래 기준을 참고해주세요.
"uploadDirPrefix" : origin/chat/

"targetId" : 상대방 유저아이디 <------------이 부분이 추가됩니다.




*최초로 채팅으로거래하기 할때 바로 이미지를 보내는 경우*
이 경우도 사실 똑같다. 다만 채팅방정보를 받는 과정이 있을 뿐이다.
즉, 5번과정을 하고 난 후에 서버에서는 새로운 채팅방을 만들게되고 채팅방정보를 클라이언트로 보내준다. 그러면 클라이언트는 채팅방정보에 있는 채팅방번호를 
이용하여 채팅방에 대해 구독하는 절차만 추가하면 된다(예전에 하던 과정과 똑같다고 보면 된다.)


[4번 추가 설명]
**<응답 예시>**

```json
[HTTP/1.1 200] ( 성공적인 응답 ) 
{
   "statusCode": 200,
   "message": "AWS 복수 이미지 업로드 성공",
   "responseData": "[d1e6042fd4634a6ea3060cef6606523b.heic, dde5b6701a524386b432d8d134f2238c.jpeg, 40794ca3d33c453d944708deab241ec2.jpeg]"
}
```   

이렇게 응답이 온다고 했을 때 
`responseData` 필드에 있는 값들을 그.대.로(파싱 ㄴ, 그냥 string 그대로) 사용하여 STOMP 채팅메시지 만들 때 message 필드안에 넣으면 됩니다. 


**<응답 예시>**

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "AWS 복수 이미지 업로드 성공",
    "responseData": [
        "example.jpg",
        "example2.jpg"
    ]
    // 복수 이미지 업로드 API 결과 반환되는 값은 List 입니다.
    // 이 값을 활용해서 상품 업로드 또는 상품 수정 시 이미지 경로 값으로 활용하면 됩니다.
 
}


### 그 외 이미지를 업로드 하지 못하고 에러응답을 받환했을 경우입니다.
크게 두가지 경우가 있습니다.
1. 서버의 문제로 인해 이미지를 업로드 하지 못하는 경우
2. 서버의 문제가 아닌 상대방이 탈퇴/차단/유저제재 의 관계에 있을 경우
        
## 1.서버의 문제로 인해 이미지를 업로드 하지 못하는 경우
아래와 같은 응답을 받았을 경우에는 "이미지 업로드 과정에서 문제가 발생하였습니다. 재업로드 하시길 바랍니다." 라는 팝업 경고창을 띄워줍니다.

[HTTP/1.1 200]
{
    "statusCode": 355,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "IOException, 복수 채팅 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 355,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 채팅 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 355
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "InterruptedException, 복수 채팅 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}


```


## 2. 서버의 문제가 아닌 상대방이 탈퇴/차단/유저제재 의 관계에 있을 경우
이 경우 적절하게 채팅방 UI를 바꾸어 주시면 됩니다. 

1. 채팅이미지 업로드시 상대방이 탈퇴한 유저일 경우

```json

{
   "statusCode":404,
   "timestamp":"2021-04-08T14:16:34.054+00:00",
   "message":"탈퇴한 유저에게 채팅 이미지를 전송할 수 없습니다.",
   "requestPath":"uri=/api/multi-img/upload",
   "pathToMove":null
}

```

2. 채팅이미지 업로드시 상대방이 관리자로부터 이용제재 받고 있는 경우

```json

{
   "statusCode":404,
   "timestamp":"2021-04-08T14:16:34.054+00:00",
   "message":"관리자로 부터 이용제재 받고 있는 유저에게 채팅 이미지를 전송할 수 없습니다.",
   "requestPath":"uri=/api/multi-img/upload",
   "pathToMove":null
}

```



3. 채팅이미지 업로드시 차단한 유저일 경우

```json

{
   "statusCode":400,
   "timestamp":"2021-04-08T14:16:34.054+00:00",
   "message":"차단한 유저와 채팅을 할 수 없습니다.",
   "requestPath":"uri=/api/multi-img/upload",
   "pathToMove":null
}

```


4. 채팅이미지 업로드시 상대방으로 부터 차단되었을 경우

```json

{
   "statusCode":400,
   "timestamp":"2021-04-08T14:16:34.054+00:00",
   "message":"차단당한 유저와 채팅을 할 수 없습니다.",
   "requestPath":"uri=/api/multi-img/upload",
   "pathToMove":null
}

```


## 3. 채팅이미지를 클릭하는 부분

https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/chat/1000x1000px/031ff210394943d7a92dd3767da0b09f.jpeg
으로 요청한다. 해당 경로에 파일이 없을 수도 있기 때문에 만약 해당 경로에 파일이 없다면 아래의 원본이미지를 요청한다.

https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/chat/031ff210394943d7a92dd3767da0b09f.jpeg

만약 원본이미지가 없을 경우도 있기때문에 원본이미지마져 존재하지 않을 경우에는 적절한 UI를 띄워줘야 한다.
