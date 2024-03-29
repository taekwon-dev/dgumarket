---
## stomp error_code - created by MS (21-01-25)


### 수정사항 3/17

여기서 1번과 2번은 이미 채팅방에 들어온 상태를 가졍하는 경우입니다.
가령 1번은 다른 브라우저에서 각각 로그인 할 경우의 상황을 가정했을 경우, 
1번유저가 크롬브라우저에서 채팅방까지 들어갔고, 사파리에서 로그인 후 회원탈퇴까지 진행한다면, 1번유저가 들어가 있는
채팅방은 그대로 남아있는 상태입니다. 여기서 만약 1번유저가 메시지를 보내려고 할 때 탈퇴된 상태이므로
상대방에게 메시지가 보내져서는 안됩니다. 

2번의 경우역시 마찬가지로 현재 1번유저가 채팅방에 들어온 상태이고 관리자가 이때 1번유저에게
유저 제재를 가한 상태입니다. 유저제재의 경우 모든 서비스를 이용하게해서는 안됩니다. 하지만 이때
채팅입력창은 활성화상태로 되어있기때문에 이때 만약 1번유저가 다른 사람에게 메시지를 보내려고 시도했을 경우
보낼 수 없도록 예외처리를 해야하는 데 그것에 대한 예외가 아래에 나와있습니다.

3번의 경우, 이미 채팅방에 들어온 상태에서 이번에는 상대방이 탈퇴/탈퇴 후 데이터보존기간이 지나 완전히 삭제된경우
입니다. 이 경우 상대방에게 메시지를 보낼경우 예외처리를 해야합니다. 그런 상대방에게 메시지를 보냈을 경우 아래와 같은 예러메시지
형태로 응답을 받게 됩니다. 

4번의 경우, 이미 채팅방에 들어온 상태에서 메시지를 보내는 상대방이 관리자로 부터 유저제재 받은 경우라면 
보내지 못하도록 예외처리를 해야합니다. 그런 상대방에게 메시지를 보냈을 경우 아래와 같은 예러메시지
형태로 응답을 받게 됩니다.

1. 본인이 탈퇴했음에도 불구하고 채팅방화면에서 상대방에게 메시지를 보내는 경우
2. 본인이 유저제재 당한 상태임에도 불구하고 상대방에게 메시지를 보내는 경우
3. 상대방이 탈퇴/아예 존재하지 않음에도 불구하고 채팅방에서 상대방에게 메시지를 보내는 경우
4. 상대방이 유저제재 당한 상태임에도 불구하고 상대방에게 메시지를 보내려고 하는 경우


에러를 받환하는 형태는 서버에서 MESSAGE 프레임으로 줄 수 도 있고 ERROR 프레임으로도 줄 수도 있다.
따로 MESSAGE 프레임 형태로 에러 메시지를 클라이언트로 전달하는 형태는 주로 웹소켓을 재연결할 필요가 없는 경우에 사용할 것이다. (가령 아래와 같은 경우)
반면 ERROR 프레임으로 주는 경우는 클라이언트로 부터 웹소켓 접속을 끊거나, 혹은 다시 연결을 요구할 경우 ERROR 프레임 형태로 클라이언트에 에러메시지를 전달한다.
(어떤 ERROR 프레임을 구체적으로 언제 어떤상황에 주어야 할 지는 계속해서 아래에 추가할 예정이다. )

---

**example**

클라이언트가 받는 메시지의 형태는 다음과 같다.
```
<<< MESSAGE
destination:/user/queue/error
content-type:application/json
subscription:sub-1
message-id:4a5uk4km-13
content-length:83

{"error_code":1,"error_description":"You cannot send messages to user you blocked"}

```

## stomp error code

error_code | error_message | meaning | STOMP Frame | 메시지 받는 곳 | 상황
:---|:---:|:---:|:---:|:---:|:---:|
`1` | `You already block user, you can't send message to user you blocked` | 상대방을 차단한 경우, 상대방에게 메시지를 보낼 수 없다. | `MESSAGE` | `SUB /user/queue/error` | 차단된 상대방에게 메시지(`SEND /message`) 를 보낼 경우 다음과 같은 메시지를 서버로부터 받게 된다.  해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "차단한 상대와 채팅을 주고 받을 수 없습니다." 등의 문구를 띄어준다. 
`2` | `You are blocked by user, you  can't send message` | 상대방으로부터 차단된 경우, 상대방에게 메시지를 보낼 수 없다. | `MESSAGE` | `SUB /user/queue/error` | 상대방으로 부터 차단되었지만 메시지를 보낼 경우 (`SEND /message`) 를 보낼 경우 다음과 같은 메시지를 서버로부터 받게 된다.해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "상대로부터 차단되었기에 상대방에게 메시지를 전송할 수 없습니다." 등의 문구를 띄어준다.
`3` | `탈퇴하거나, 존재하지 않는 유저는 메시지 기능을 이용하실 수 없습니다.` | 본인은 탈퇴를 했음에도 불구하고(가령 다른 브라우저에서 탈퇴를 먼저 한 상황)상대방에게 메시지를 보내려고 하는 경우 | `MESSAGE` | `SUB /user/queue/error` | 본인이 탈퇴했음에도 불구하고 (`SEND /message`) 를 보낼 경우 다음과 같은 메시지를 서버로부터 받게 된다.해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "탈퇴하거나 존재하지 않는 유저는 해당 기능을 이용할 수 없습니다." 라는 문구를 띄워주며 강제 로그아웃 시킨다.
`4` | `관리자로 부터 이용제재를 받고있습니다. 더 이상 서비스를 이용하실 수 없습니다.` | 본인은 관리자로 부터 이용제재 받았음에도 불구하고 상대방에게 메시지를 보내려고 하는 경우 | `MESSAGE` | `SUB /user/queue/error` | 본인이 관리자로부터 제재를 받고 있음에도 불구하고 (`SEND /message`) 를 보낼 경우 다음과 같은 메시지를 서버로부터 받게 된다.해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "관리자로부터 제재를 받고있습니다. 더 이상 서비스를 이용하실 수 없습니다." 라는 문구를 띄워주며 강제 로그아웃 시킨다.
`5` | `탈퇴하거나 존재하지 않는 유저에게 메시지를 보낼 수 없습니다. ` | 탈퇴하거나 탈퇴 후 정보보존기간이 지나 완전히 상대방에 대한 정보가 없어졌음에도 불구하고 상대방에게 메시지를 보내려고 하는 경우 | `MESSAGE` | `SUB /user/queue/error` | 상대방이 탈퇴하거나 존재하지 않음에도 불구하고 (`SEND /message`) 를 보낼 경우 다음과 같은 메시지를 서버로부터 받게 된다.해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "탈퇴하거나 존재하지 않는 유저에게 메시지를 보낼 수 없습니다." 라는 문구를 띄워주며 클라이언트 채팅방 UI를 적절하게 바꾸어 준다. 신고하기/차단하기(차단해제)하기 버튼을 제외한 모든 버튼 비활성화
`6` | `관리자로 부터 제재를 받고 있는 유저에게 메시지를 전달할 수 없습니다.` | 상대방이 현재 관리자로 부터 제재를 받고 있음에도 불구하고 메시지를 보내려고 할 때 | `MESSAGE` | `SUB /user/queue/error` | 상대방이 관리자로 부터 제재를 당하고 있음에도 불구하고 (`SEND /message`) 를 보낼 경우 다음과 같은 메시지를 서버로부터 받게 된다.해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "관리자로부터 제재를 받고 있는 유저에게 메시지를 보낼 수 없습니다." 라는 문구를 띄워주며 클라이언트 채팅방 UI를 적절하게 바꾸어 준다. 신고하기/차단하기(차단해제)하기 버튼을 제외한 모든 버튼 비활성화