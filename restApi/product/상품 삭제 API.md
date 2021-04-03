**상품 삭제 API** 

판매자가 자신이 업로드한 상품 상세 페이지에서 삭제 버튼 클릭 시 

"삭제되면 복구할 수 없습니다. 삭제를 진행하시겠습니까?" 

"예" 클릭 : 서버 측에서 해당 상품의 상태를 '삭제' 상태로 바꾸게 됩니다. 

**URL** : `/api/product/delete` 

**Method** : `POST`

**Authentication** : 인증 필요 (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  O

**Request Body 예시** :

```json
{
    "productId" : {productId} // 삭제할 상품의 고유 ID 
}
```

**응답 예시**

```json
// 상품 삭제 이후, 인덱스 화면으로 이동시키시면 됩니다. 
[HTTP/1.1 200 OK]
{
    "statusCode": 200,
    "message": "상품 삭제 성공",
    "responseData": null
}


// 상품 정보 삭제 실패 
// (-> 'pathToMove' 값으로 페이지 이동 + Alert)
// Alert 내용

// "요청하신 작업을 수행하지 못했습니다. (오류코드 : -305)"

[HTTP/1.1 200 OK]
{
    "statusCode": 305,
    "timestamp": "2021-04-01T01:30:46.888+00:00",
    "message": "삭제하려는 상품 정보를 찾을 수 없는 경우",
    "requestPath": "/api/product/delete",
    "pathToMove": "/shop/main/index"
}
```

