---
## stomp error_code - created by MS (21-01-25)


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
`2` | `You are blocked by user, you  can't send message` | 상대방으로부터 차단된 경우, 상대방에게 메시지를 보낼 수 없다. | `MESSAGE` | `SUB /user/queue/error` | 상대방으로 부터 차단되었지만 메시지를 보낼 경우 (`SEND /message`) 를 보낼 경우ds 다음과 같은 메시지를 서버로부터 받게 된다.해당 메시지를 받게 되면 클라이언트는 간단한 토스트 메시지로 "상대로부터 차단되었기에 상대방에게 메시지를 전송할 수 없습니다." 등의 문구를 띄어준다. 