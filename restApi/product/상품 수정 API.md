### 상품 수정 API 

**URL** : `/api/product/modify` 

**Method** : `POST`

**Authentication** : 인증 필요 (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  O

**Request Body 예시** :

```json
// 상품 수정 API 요청 시 상품 이미지 경로값이 있는 경우 (= 해당 상품과 관련한 이미지가 있는 경우)
{   
   "productId" : 1, // 수정할 상품 고유 ID 
   "title" : "상품 타이틀",
   "information" : "상품 설명",
   "price" : "상품 가격",
   "imgDirectory" : "상품 이미지 경로", // ex) ["A.jpg"] or ["A.jpg, B.jpg"]
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1,
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}

// 상품 수정 API 요청 시 상품 이미지 경로값이 없는 경우 (= 해당 상품과 관련한 이미지가 없는 경우)
{   
   "productId" : 1, // 수정할 상품 고유 ID 
   "title" : "상품 타이틀",
   "information" : "상품 설명",
   "price" : "상품 가격", // 숫자만 입력 (₩ 또는 , 제외)
   "imgDirectory" : null, // 해당 상품과 관련한 이미지가 없기 때문에 저장 경로 값에 null을 넣어서 요청 
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1,
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}

```



## 상황1 : 상품의 사진 정보가 변경되지 않은 경우

**1) 0 to 0 : 기존 상품 관련 사진이 없는 경우에서 수정할 때도 이미지를 첨부하지 않은 경우**

**2) N to N : 기존 상품 관련 사진이 N장 있었던 경우, 수정할 때 해당 이미지를 온전히 유지하는 경우** 

위 두 상황인 경우에는 **상품 수정 API**를 요청하는 상황에서 **상품 사진 관련 API**를 요청할 필요 없이 바로 상품 수정 API를 요청하시면 됩니다.  이미지 수정 여부와 관계 없이 요청 형태는 동일하므로 아래와 같은 요청 예시 형태로 요청하시면 됩니다. 

**<요청 예시>** 

```json
{   
   "productId" : 1, // 수정할 상품 고유 ID 
   "title" : "상품 타이틀",
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

___

## **상황2 : 상품의 사진 정보가 변경되는 경우**

**상품의 사진 정보가 변경되는 양상에 따라**서 상품 사진 관련 API(이미지 업로드, 삭제, 수정 중)를 어떻게 조합되는 지가 영향을 받습니다.

**세 가지 기준**으로 나뉘게 되는데 순서대로 설명하겠습니다.

### ㄱ) 상품 이미지 업로드 API만 요청하는 경우 

- 0 to N : 기존에 상품 관련 사진이 없는 경우에서 사진을 업로드하는 경우
- N to N+1 : 기존에 상품 관련 사진이 3장일 때, 수정 후 4장을 업로드하는 경우 
- N to N` : 수정 후 상품 관련 사진의 수가 동일하지만, 사진을 변경한 경우 

위 **세 경우**에는 상품 수정 API를 요청하기 전 이미지 저장 경로를 반환 받기 위해 **복수 이미지 업로드 API**만을 요청하시면 됩니다. 단, 기존 이미지가 있는 경우에서 상품 이미지 업로드 API를 요청하는 상황에서는 **기존 이미지 저장 경로 값**을 이미지 업로드 API에 전달해야 합니다. 이는 이미지 업로드 API에서 더 자세히 설명하겠습니다. 우선 상품 수정 API 요청 예시는 아래와 같습니다. 

**<요청 예시>**

```json
{   
   "productId" : 1, // 수정할 상품 고유 ID 
   "title" : "상품 타이틀",
   "information" : "상품 설명",
   "price" : "상품 가격", // 숫자만 입력 (₩ 또는 , 제외)
   "imgDirectory" : "상품 이미지 경로", // 복수 이미지 업로드 API에서 받은 이미지 저장 경로를 활용해서 요청
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1,
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}
```

___

### ㄴ) 상품 이미지 삭제 API만 요청하는 경우

- N to 0 : 기존 상품 관련 사진이 N장 있었지만, 수정 후 0장이 되는 경우 

이미지 삭제 API에 **기존 이미지 경로 값**을 서버측에 전송해야 합니다. 이 부분 역시 해당 API 명세에 자세히 설명되어 있습니다. 우선 상품 수정 API 요청 예시는 아래와 같습니다.  

**<요청 예시>**

```json
{   
   "productId" : 1, // 수정할 상품 고유 ID 
   "title" : "상품 타이틀",
   "information" : "상품 설명",
   "price" : "상품 가격", // 숫자만 입력 (₩ 또는 , 제외)
    // 이미지 삭제 API에서 받은 이미지 저장 경로를 활용해서 요청
    // 단, 이 경우에는 기존 이미지를 모두 삭제하는 경우이므로 이미지 삭제 API에서 null을 반환 받는다.
    // 따라서 상품 수정 API에서 요청하는 값 역시 null로 요청 값을 설정한다. 
   "imgDirectory" : null, 
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1,
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}
```

___

### ㄷ) 상품 이미지 업로드 & 상품 이미지 삭제 API 모두 요청해야 하는 경우

- N to N-M : 기존 상품 관련 사진이 N장 있었지만, 수정 후 N-M장이 되는 경우 

이 경우 상품 이미지 업로드와 삭제 API가 모두 요청되어야 하는 이유는 예를 들어 5장에서 3장이 되는 경우, 수정 후 3장에 대해서는 기존 상품 이미지 파일명을 활용해서 업로드를 진행합니다. 또한 나머지 기존 5장 중 3장을 제외한 2개의 파일명에 대해서는 S3에 삭제 요청을 보내야 하기 때문에 두 API가 모두 호출되어야 하고. 이는 상품 이미지 업로드, 삭제 API 외 다른 API 하나를 더 추가해서 위에서 설명한 처리를 진행할 예정입니다. -> **(복수 이미지 수정 API)**

이 경우에도 마찬가지로 해당 API에 **기존 이미지 경로 값**을 서버측에 전송해야 합니다. 해당 API 문서에서 자세히 설명되어 있습니다. 우선 상품 수정 API 요청 예시는 아래와 같습니다.

**<요청 예시>**

```JSON
{   
   "productId" : 1, // 수정할 상품 고유 ID 
   "title" : "상품 타이틀",
   "information" : "상품 설명",
   "price" : "상품 가격", // 숫자만 입력 (₩ 또는 , 제외)
    // 복수 이미지 수정 (업로드 + 삭제 포함된) API에서 받은 이미지 저장 경로를 활용해서 요청
    // 이 경우에는 상품 이미지가 최소 1장 이상인 경우이므로, 해당 사진이 저장된 저장 경로를 반환 받는다.
   "imgDirectory" : "상품 이미지 경로", 
   "productCategory" : 1,
   "isNego" : 1,
   "transactionStatusId" : 1,
   "transactionModeId" : 1,
   "selfProductStatus" : 1 // selfProductStatus : 1로 고정해서 보내주시면 됩니다.
}
```

____

마지막으로 **응답 예시**와 응답 받은 값을 어떻게 활용하면 되는 지 설명하겠습니다.

**<응답 예시>**

```json
[HTTP/1.1 200 OK]
{
    "statusCode": 200,
    "message": "상품 정보 업데이트 성공",
    // 업데이트 한 상품의 고유 아이디 입니다. 
    // 업데이트 이후에도 해당 상품 상세 페이지로 이동하므로, 해당 고유 값을 활용해서 해당 페이지로
    // 이동시키시면 됩니다.
    "responseData": {productId}
}

// 상품 정보 수정 실패 
// (-> 'pathToMove' 값으로 페이지 이동 + Alert)
// Alert 내용

// "요청하신 작업을 수행하지 못했습니다. (오류코드 : -305)"

[HTTP/1.1 200 OK]
{
    "statusCode": 305,
    "timestamp": "2021-04-01T01:26:55.317+00:00",
    "message": "수정하려는 상품 정보를 찾을 수 없는 경우",
    "requestPath": "/api/product/modify",
    "pathToMove": "/shop/main/index"
}
```