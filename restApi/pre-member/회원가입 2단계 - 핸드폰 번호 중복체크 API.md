## 회원가입 2단계 - 핸드폰 인증 

회원가입 2단계에서는 아래  API가 포함됩니다.

- **핸드폰 번호 중복체크 API**
- 핸드폰 인증문자 발송 API 
- 핸드폰 인증 API 



### 핸드폰 번호 중복체크 API 

핸드폰은 중복되지 않는 유일한 값이어야 합니다. 웹메일 중복체크와 동일한 시점(= 유저가 핸드폰 입력을 마무리한 시점)에 핸드폰 번호 중복체크 API를 요청하시면 됩니다. 현재 회원으로 등록된 유저의 핸드폰 정보와 중복되는 지를 체크합니다. 

**URL** : `/api/pre-member/check-duplicate-phone`  

**Method** : `POST`

**Authentication** : 인증 필요 X  

**Request Body** : 

```json
{ 
    "phoneNumber" : "01000000000" // - 없이 숫자만 입력
}
```

___

**Response**

```json
[HTTP/1.1 200 OK]
// 핸드폰 중복체크 결과 - 사용할 수 없는 핸드폰 번호인 경우
// 이 경우 -> "사용할 수 없는 번호입니다" 안내 띄워주시면 됩니다. 
{
    "statusCode": 1,
    "message": "사용 할 수 없는 핸드폰 번호입니다.",
    "responseData": null
}

[HTTP/1.1 200 OK]
// 핸드폰 중복체크 결과 - 사용할 수 있는 핸드폰 번호인 경우
{
    "statusCode": 2,
    "message": "사용 할 수 있는 핸드폰 번호입니다.",
    "responseData": null
}

```

