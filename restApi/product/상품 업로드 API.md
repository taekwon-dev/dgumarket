### 상품 업로드 API  (인증)

상품 정보에 포함되어 있는 부분 중, 판매자가 상품을 소개하기 위해 업로드한 이미지들이 있습니다. 이미지는 0장 (= 사진 등록 안 하는 경우) 부터 최대 5장까지 등록할 수 있습니다. 

먼저 **상품에 대한 이미지를 등록하지 않은 경우**에는 **사진 업로드 API**를 보낼 필요가 없습니다. 반면 **상품에 대한 이미지 수가 1장 ~ 5장이 업로드 되는 경우**엔, 앞서 말한 것과 같이 **상품 이미지 업로드 API를 요청** 하고, 업로드된 **파일 경로를 반환**받아서 최종적으로 **이미지 저장경로를 포함한 상품 정보를 상품 업로드 API에 전송**하여 상품을 업로드하게 됩니다.

유저 프로필 사진과 다른점은 복수의 이미지를 다룬다는 점이고, 따라서 **상품 이미지 업로드 API**에서 반환되는 이미지 경로의 예시는 다음과 같습니다.

**<예시 : 이미지 업로드 API 반환 값> - 상품 업로드 API 반환 값이 아닙니다!** 

(참고 : AWS S3 이미지 저장 경로를 저장할 땐 파일명만 저장합니다.)

1) **한 장**을 업로드 하는 경우

파일명이 A.jpg 인 경우 

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "AWS 복수 이미지 업로드 성공",
    "responseData": [
        "A.jpg"
    ]
}
```

2) **두 장 이상**을 업로드 하는 경우

파일명이 A.jpg, B.jpg인 경우 

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "AWS 복수 이미지 업로드 성공",
    "responseData": [
        "A.jpg",
        "B.jpg"
    ]
}
```

___

위 로직을 기반으로 요청 값을 어떻게 보내야 하는 지 그리고 반환 받는 값은 어떤 형태이고 반환 받은 이후 어떻게 활용해야 하는 지를 설명하겠습니다.

### 상품 업로드  API  

**URL** : `/api/product/upload` 

**Method** : `POST`

**Authentication** : 인증 필요 (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  O

**Request Body 예시** :

```json
// 상품 관련 이미지가 없는 경우 -> 상품 이미지 경로 : null

// transactionModeId 
// 0 : 직거래, 1 : 비대면 거래(택배), 2 : 구매자와 조율 가능

// transactionStatusId 
// 0 : 판매중 (Default), 
// 1 : 예약중 (판매자가 특정 구매 의사를 보인 사람과 거래 약속을 잡은 경우, 판매자가 직접 설정),
// 2 : 판매완료 (거래 완료 후, 판매자가 직접 설정) 
// 3 : 신고처리중 (관리자가 직접 설정) 

// isNego 
// 0 : 가격 조정 가능 1: 가격 조정 불가능

{
   "title" : "상품 타이틀"
   "information" : "상품 설명",
   "price" : "상품 가격", // 숫자만 입력 (₩ 또는 , 제외)
   "imgDirectory" : null, // null : 상품 관련 이미지가 없는 경우
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1, 
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}


// 상품 관련 이미지가 있는 경우 
{
   "title" : "상품 타이틀"
   "information" : "상품 설명",
   "price" : "상품 가격", // 숫자만 입력 (₩ 또는 , 제외)
   "imgDirectory" : "상품 이미지 경로", // ex) ["A.jpg"] or ["A.jpg, B.jpg"]
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1,
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}
```

**Response 예시**

```json
// 상품 업로드 이후 -> 해당 상품의 상세 페이지로 이동합니다.
// 따라서 /product/{product_id}로 이동을 하게되고 이 때 '변수'인 '고유 아이디 값'을 반환합니다.
// 해당 값을 활용해서 해당 상품의 상세 페이지로 페이지를 이동시키시면 됩니다. 
[HTTP/1.1 200 OK]
{
    "statusCode": 200,
    "message": "상품 업로드 성공",
    "responseData": {product_id} 
}
```

