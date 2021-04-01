### 비밀번호 변경 API

비밀번호 변경은 다음과 같은 조건에 모두 부합했을 때 변경할 수 있습니다.

- **현재 비밀번호**가 현재 로그인한 유저의 비밀번호와 일치하는 경우
- **새 비밀번호**와 **새 비밀번호 확인** 두 값이 일치하는 경우
- 현재 비밀번호와, 새 비밀번호가 다른 경우 
- **비밀번호 정규식**에 통과하는 경우

위 조건에 부합하지 않은 경우에는 비밀번호 변경 버튼이 비활성화 처리가 되어 API 요청을 못하도록 막으시면 됩니다. 

**URL** : `/api/user/profile/change-pwd` 

**Method** : `POST`

**Authentication** : O (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  

```json
{
    "prevPassword" : "기존 비밀번호",
    "newPassword" : "새로운 비밀번호",
    "checkNewPassword" : "새로운 비밀번호 확인"
}
```

**Response** :

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "회원 비밀번호 변경 성공",
    "responseData": null
}

// 비밀번호 변경 실패 
// 1. 기존 비밀번호가 틀린 경우
// 2. 새로 변경할 비밀번호 & 확인 값이 일치하지 않은 경우 

// statusCode 가 1 또는 2인 경우에는 Alert 또는 안내 출력을 해주시면 됩니다 :)

[HTTP/1.1 200]
{
    "statusCode": 1,
    "message": "기존 비밀번호의 값이 일치하지 않은 경우",
    "responseData": null
}

[HTTP/1.1 200]
{
    "statusCode": 2,
    "message": "새 비밀번호와 새 비밀번호 확인 값이 서로 다릅니다.",
    "responseData": null
}



```

