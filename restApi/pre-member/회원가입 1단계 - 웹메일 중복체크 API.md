## 회원가입 1단계 - 웹메일 인증 

회원가입 1단계에서는 아래 두 API가 포함됩니다.

- **웹메일 중복체크** 
- 웹메일 전송 



### 웹메일 중복체크  API  

회원가입 1단계 - 웹메일 인증 과정에서 유저가 입력한 웹메일의 중복체크를 통해 회원가입 가능 여부를 알려줍니다. 

**URL** : `/api/user/check-webmail` 

**Method** : `POST`

**Authentication** : 인증 필요 X 

**Request Body** : 

```json
{
  "webMail" : "유저가 입력한 웹메일@dongguk.edu"
}
```

___

** Response **

**Code** : `200 OK`

**example**

```json

각 케이스 별로 유저한테 안내 메시지를 띄어주시면 됩니다.

[HTTP/1.1 200 OK]
1. 중복체크 결과, 입력 받은 웹메일로 회원가입이 불가능한 경우
{
    "statusCode": 1,
    "message": "회원가입 1단계 - 웹메일 중복체크 통과 실패 : 회원가입 불가능",
    "responseData": null
}

[HTTP/1.1 200 OK]
2. 중복체크 결과, 입력 받은 웹메일로 회원가입이 가능한 경우
{
    "statusCode": 2,
    "message": "회원가입 1단계 - 웹메일 중복체크 통과 : 회원가입 가능",
    "responseData": null
}
```

