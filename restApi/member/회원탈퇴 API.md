## 회원 탈퇴 



### 회원탈퇴  API  

**URL** : `/api/user/profile/withdraw` 

**Method** : `POST`

**Authentication** : O (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  X

___

**Response** :

```json
// 유저의 탈퇴 요청 처리 성공 
// 유저의 탈퇴 요청이 성공으로 끝난 경우, 인덱스 페이지로 이동시키시면 됩니다.
// 유저가 탈퇴 요청 시 '리프레시 토큰이 삭제'되므로, 비로그인 상태로 인덱스 화면으로 이동하게 됩니다. 
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "유저 탈퇴 요청 처리 성공",
    "responseData": null
}


// 유저의 탈퇴 요청에 대한 예외 처리는 공통적으로 처리되는 예외에 포함되어 여기에 기술하지 않습니다. 
```

