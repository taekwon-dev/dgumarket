# 비밀번호 찾기 API

- **핸드폰 인증문자 발송 API ... (1)**
- **핸드폰 인증 API ... (2)**
- **비밀번호 재설정 페이지 접근 시 토큰 검증 API ... (3)**
- **비밀번호 재전송 API ... (4)**

### 1. 핸드폰 인증문자 발송 API 



**URL** : `/api/send-sms/find-pwd` 

**Method** : `POST`

**Authentication** : X

**Request Body** :  

```json
// 웹메일과 핸드폰 번호를 요청 값으로 받고, 
// 실제 회원 테이블에서 요청한 웹메일에 해당하는 번호가 맞는 지 확인 후 인증문자를 전송합니다.
// 웹메일에 해당하는 번호가 틀리거나 해당 웹메일이 존재하지 않는 경우는 실제 인증문자를 전송하지는 않지만, 
// "인증문자를 발송했습니다. 인증문자가 도착하지 않은 경우 웹메일과 핸드폰 번호를 다시 한 번 확인 후 재시도 하시기 바랍니다."
// Alert 또는 다른 방식을 통해 안내해주시면 됩니다. 

// 실제 웹메일에 해당하는 번호를 역으로 추정할 수 없도록 하기 위해 위와 같이 처리합니다. 
// 단, 1일 요청 제한에 걸린 경우 또는 알리고 서버 API 문제인 경우는 해당 예외처리에 맞게 처리해주시면 됩니다. 

{
    "webMail" : "example@dongguk.edu"
    "phoneNumber" : "01000000000" // - 없이 번호만 입력 
}
```

**Response** :

```json
// 인증문자 발송 성공 
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "인증문자가 발송됐습니다.",
    "responseData": null
}

// [인증문자 발송 실패] 
// "인증문자를 발송했습니다. 인증문자가 도착하지 않은 경우 웹메일과 핸드폰 번호를 다시 한 번 확인 후 재시도 하시기 바랍니다." Alert 띄워주시면 됩니다.
[HTTP/1.1 200]
{
    "statusCode": 1,
    "message": "해당 웹메일로 유저 정보를 찾을 수 없는 경우",
    "responseData": null
}

// "인증문자를 발송했습니다. 인증문자가 도착하지 않은 경우 웹메일과 핸드폰 번호를 다시 한 번 확인 후 재시도 하시기 바랍니다." Alert 띄워주시면 됩니다.
[HTTP/1.1 200]
{
    "statusCode": 2,
    "message": "해당 웹메일의 핸드폰 번호와 요청한 핸드폰 번호가 일치하지 않은 경우",
    "responseData": null
}

// 1일 5회 이상 요청 제한으로 인증문자를 요청할 수 없습니다. Alert 띄워주시면 됩니다. 
[HTTP/1.1 200]
{
    "statusCode": 3,
    "message": "1일 5회 이상 요청으로 발송 실패된 경우",
    "responseData": null
}


// [알리고 서버 API 오류]
// 알리고 서버에서 문자 전송 실패했을 경우는 'statusCode'가 0보다 작은 경우입니다. 
// 'statusCode'가 0보다 작은 경우, 실패 사유를 확인할 수 있습니다. 

// 문자 전송 실패의 경우 
// "인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요."
// 메시지를 안내해주시면 됩니다. 

[HTTP/1.1 200 OK]
{
    "statusCode": 0 보다 작은 경우, (ex -1, -100, -102, ..)
    "timestamp": "2021-03-30T05:10:58.454+00:00",
    "message": "알리고 문자 전송 실패, 실패 사유 : API 인증오류입니다.",
    "requestPath": "/api/send-sms/change-phone",
    "pathToMove": null
}

```



### 2. 핸드폰 인증 API (인증 통과 후 비밀번호 재설정 페이지로 이동)

**URL** : `/api/user/find-pwd/verify-phone` 

**Method** : `POST`

**Authentication** : X

**Request Body** :  

```json
{
    "webMail" : "example@dongguk.edu",
    "phoneNumber" : "01000000000", // - 없이 번호만 
    "verificationNumber" : "000000" // 6자리 수
}
```

**Response** :

```json
// 핸드폰 인증 성공 

// 핸드폰 인증 성공 시 비밀번호 재설정 페이지로 이동합니다 
// 이동 시점에 'responseData' 응답되는 경로(토큰 값 포함)로 요청하시면 됩니다.
// 페이지 반환 후에 해당 토큰 값을 활용해서 
// 아래 3번 '비밀번호 재설정 페이지 접근 시 토큰 검증 API'를 진행해주시면 됩니다. 
// (위 토큰 값은 3번 API에서 Custom Header을 통해 요청하게 됩니다) 
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "[인증 성공] 비밀번호 재설정 위한 핸드폰 인증 성공",
    "responseData": "/shop/account/find_pwd_newPwd?token='JWT 토큰 값'"
}

// 핸드폰 인증 실패 
[HTTP/1.1 200]
{
    "statusCode": 1,
    "message": "[인증 실패] 대조할 인증번호를 참조할 수 없는 경우",
    "responseData": null
}

[HTTP/1.1 200]
{
    "statusCode": 2,
    "message": "[인증 실패] 발급된 인증번호와 일치하지 않는 경우 ",
    "responseData": null
}
```



### 3. 비밀번호 재설정 페이지 접근 시 토큰 검증 API (on Gateway Server)

**URL** : `/api/filter/find-pwd/verify-token`  

**Method** : `POST`

**Authentication** : X  

**Custom Header** : 

```json
// Custom Header 'token' 키 값에 토큰 값을 넣어서 요청해주시면 됩니다. 
"token" : "토큰 값"
```

**Request Body** : X

```json
// 비밀번호 재설정 페이지에 접근 가능한 경우 
[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-08T03:28:25.079+00:00",
    "path": "/api/filter/find-pwd/verify-token",
    "status": 200,
    "message": "[페이지 접근 성공]요청 페이지 접근 가능"
}

// 비밀번호 재설정 페이지 접근이 불가능한 경우 
// Alert ("") 띄우고
// 'pathToMove' 경로 값으로 이동시키시면 됩니다. 
[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-08T03:37:47.502+00:00",
    "path": "/api/filter/find-pwd/verify-token",
    "status": 302,
    "message": "[페이지 접근 실패]요청한 토큰 값과 대조할 토큰 값이 없는 경우",
    "pathToMove": "/shop/main/index"
}

// JWT 토큰 관련 예외가 발생하는 경우
// 1. 토큰이 유효하지 않은 경우
// 2. 토큰의 형식 문제가 있는 경우
// 3. 토큰 Secret Key가 일치하지 않는 경우 
[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-08T03:46:48.548+00:00",
    "path": "/api/filter/find-pwd/verify-token",
    "status": 302,
    "message": "[페이지 접근 실패] 요청 토큰 값 파싱 과정에서 JWTException 발생",
    "pathToMove": "/shop/main/index"
}

[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-08T03:48:10.200+00:00",
    "path": "/api/filter/find-pwd/verify-token",
    "status": 302,
    "message": "[페이지 접근 실패] 비밀번호 재설정 페이지 토큰 값 없는 상태 접근하는 경우",
    "pathToMove": "/shop/main/index"
}

[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-08T03:48:43.958+00:00",
    "path": "/api/filter/find-pwd/verify-token",
    "status": 302,
    "message": "[페이지 접근 실패]이용제재 또는 탈퇴한 유저가 비밀번호 재설정 페이지 접근하는 경우",
    "pathToMove": "/shop/main/index"
}
```



### 4. 비밀번호 재설정 API 

**URL** : `/api/user/find-pwd` 

**Method** : `POST`

**Authentication** : X

**Custom Header** : 

```json
// Custom Header 'token' 키 값에 토큰 값을 넣어서 요청해주시면 됩니다. 
"token" : "토큰 값"
```

**Request Body** :  

```json
{
    "token" : "비밀번호 재설정 관련 핸드폰 인증 후 발급 받은 토큰 값",
    "newPassword" : "새로운 비밀번호 값",
    "checkNewPassword" : "새로운 비밀번호 확인용 값"
}
```

```json
// 비밀번호 재설정 성공
[HTTP/1.1 200 OK]
{
    "statusCode": 200,
    "message": "[비밀번호 재설정 성공] 비밀번호 재설정 완료",
    "responseData": null
}


// 비밀번호 재설정 실패 
[HTTP/1.1 200 OK]

// 클라이언트 측에서 이미 검토 후 서버 측 요청이 있지만
// 서버 측에서도 추가 체크를 합니다. 
// 추후 statusCode 1번으로 정규식 관련 추가될 수 있습니다. (예정)
{
    "statusCode": 2,
    "message": "[비밀번호 재설정 실패] 새로 설정한 비밀번호, 확인 값이 서로 일치하지 않는 경우",
    "responseData": null
}

#(2021/04/27 추가)
// 비밀번호 재설정 요청 시 토큰 유효성 검사 결과 비밀번호 재설정 처리할 수 없는 경우 [예외처리] 
// 아래 예외 응답 반환 경우 Alert("잘못된 접근입니다.") 띄우고 
// pathToMove 값인 메인 페이지로 이동시켜주시면 됩니다.
[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-27T00:55:02.325+00:00",
    "path": "/api/user/find-pwd",
    "status": 302,
    "message": "[페이지 접근 실패]이용제재 또는 탈퇴한 유저가 비밀번호 재설정 페이지 접근하는 경우",
    "pathToMove": "/shop/main/index"
}

[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-27T00:54:29.596+00:00",
    "path": "/api/user/find-pwd",
    "status": 302,
    "message": "[페이지 접근 실패]요청한 토큰 값과 대조할 토큰 값이 없는 경우",
    "pathToMove": "/shop/main/index"
}

[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-27T00:53:44.739+00:00",
    "path": "/api/user/find-pwd",
    "status": 302,
    "message": "[페이지 접근 실패] 요청 토큰 값 파싱 과정에서 JWTException 발생",
    "pathToMove": "/shop/main/index"
}

[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-27T00:53:23.518+00:00",
    "path": "/api/user/find-pwd",
    "status": 302,
    "message": "[페이지 접근 실패] 비밀번호 재설정 페이지 토큰 값 없는 상태 접근하는 경우",
    "pathToMove": "/shop/main/index"
}

[HTTP/1.1 200 OK]
{
    "timestamp": "2021-04-27T00:50:32.030+00:00",
    "path": "/api/user/find-pwd",
    "status": 302,
    "message": "[페이지 접근 실패]해당 토큰으로 이미 비밀번호 재설정 완료된 경우",
    "pathToMove": "/shop/main/index"
}


```

