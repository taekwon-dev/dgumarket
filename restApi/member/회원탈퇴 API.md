## 회원 탈퇴 



### 회원탈퇴  API  

**URL** : `/api/user/profile/withdraw` 

**Method** : `POST`

**Authentication** : 인증 필요 (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  X

___

** Response **

**Code** : `200 OK`

**Content**

**Response Example**

```json
1. 유저의 탈퇴 요청 처리 성공 
// 유저의 탈퇴 요청이 성공으로 끝난 경우, 인덱스 페이지로 이동시키시면 됩니다.
// 유저가 탈퇴 요청 시 리프레시 토큰이 삭제되므로, 비로그인 상태로 인덱스 화면으로 이동하게 됩니다. 
{
    "message": "유저 탈퇴 요청 처리 성공",
    "status": 200,
    "data": null
}

2. 유저의 탈퇴 요청 처리 실패 
// 탈퇴 요청에 대한 예외처리는 추후 보완할 예정입니다. (따로 처리하실 부분이 아직 없습니다)
// 일반적인 상황에 대한 예외처리가 아닌, DB 상에서 데이터를 수정하는 과정에서 에러가 나는 경우가 대표적인 예외입니다. 

{
    "message": "유저 탈퇴 요청 처리 실패",
    "status": 200,
    "data": null
}
```