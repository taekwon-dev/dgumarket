## 회원가입 2단계 - 핸드폰 인증 

회원가입 2단계에서는 아래  API가 포함됩니다.

- 핸드폰 번호 중복체크 API
- 핸드폰 인증문자 발송 API 
- **핸드폰 인증 API** 



### 핸드폰 인증 API 

핸드폰 인증문자를 수신한 유저가 입력한 인증번호의 유효성을 검사하는 API 입니다. 유효성 검사를 통과한 경우 회원가입 3단계 세부정보 입력 페이지로 이동합니다. 

**URL** : `/api/pre-member/verify-phone`  

**Method** : `POST`

**Authentication** : 인증 필요 X  

**Request Body** : 

```json
{
    "webMail" : "example@dongguk.edu",  
    "phoneNumber" : "01000000000", // - 없이 숫자만 입력 
    "verificationNumber" : "6자리 숫자조합" 
}
```

___

**Response**

```json
[HTTP/1.1 200 OK]
// 핸드폰 인증 실패 
// 이 경우 -> "핸드폰 인증 번호가 일치하지 않습니다" 문구를 안내하면 됩니다. 
{
    "statusCode": 1,
    "message": "핸드폰 인증 실패",
    "responseData": null
}


[HTTP/1.1 200 OK]
// 핸드폰 인증 성공 
{
    "statusCode": 2,
    "message": "핸드폰 인증 성공",
    "responseData": "회원가입 3단계 페이지 요청 URL" 
    
  	// URL 예시 
    // http://localhost:8081/shop/account/smartPhone_certification?user_id=토큰값
}



```