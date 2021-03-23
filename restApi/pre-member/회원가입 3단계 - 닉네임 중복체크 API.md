## 회원가입 3단계 - 세부정보 입력

회원가입 3단계에서는 아래  API가 포함됩니다.

- **닉네임 중복체크 API**
- 회원가입  API 



### 닉네임 중복체크 API 

이미 가입되어 있는 회원의 닉네임과 대조하여 유저가 입력한 닉네임을 사용할 수 있는 지 여부를 확인합니다. 

**URL** : `/api/pre-member/check-duplicate-nickname`

**Method** : `POST`

**Authentication** : 인증 필요 X  

**Request Body** : 

```json
{
    "nickName" : "닉네임"
}
```

___

**Response**

```json
[HTTP/1.1 200 OK]
// 중복체크 결과 해당 닉네임을 사용할 수 없는 경우
{
    "statusCode": 1,
    "message": "입력 닉네임을 사용할 수 없습니다.",
    "responseData": null
}

[HTTP/1.1 200 OK]
// 중복체크 결과 해당 닉네임을 사용할 수 있는 경우 
{
    "statusCode": 2,
    "message": "입력 닉네임을 사용할 수 있습니다.",
    "responseData": null
}

```

