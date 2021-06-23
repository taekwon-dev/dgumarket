## 회원가입 1단계 - 웹메일 인증 

- 웹메일 중복체크 
- **웹메일 전송**



### 웹메일 전송  API  

유저가 입력한 웹메일에 회원가입 2단게 페이지로 이동할 수 있는 링크 메일을 전송합니다.

**URL** : `/api/user/send-webmail` 

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

**Content**

`message`: 응답 메시지 

`resultCode`: 응답 상태 

`responseData`:  null

**example**

```json

// 유저한테 안내 메시지를 띄어주시면 됩니다. 
// (아래 메시지를 그대로 활용해서 안내 문구를 띄어주셔도 됩니다 :))
// 인증 메일 발송 처리까지 대략 4초 정도 소요됩니다. 이 응답을 받기 전까지 클라이언트 측에서 로딩바 등을 활용하면 좋을 것 같습니다. 
[HTTP/1.1 200 OK]
{
    "statusCode": 1,
    "message": "인증메일을 발송했습니다.",
    "responseData": null
}


// 2021/06/22 추가 
// 인증 메일 발송 과정에서 예외 발생 시, Alert로 아래 "message" 필드 값 띄어주시면 됩니다.
[HTTP/1.1 200 OK]
{
    "statusCode": 2,
    "message": "인증메일 발송 실패했습니다. 잠시 후 다시 시도해주세요.",
    "responseData": null
}


```