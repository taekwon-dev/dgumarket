## 복수 이미지 업로드 API (인증)



복수 이미지 업로드 API는 AWS S3를 이용한 복수의 이미지 업로드를 처리해주는 API 입니다.

현재 우리 서비스에서 복수의 이미지를 S3 스토리지에 저장하는 경우는 다음과 같습니다. 

- **상품 업로드 API, 상품 수정 API**
- **신고 사진 업로드 API**
- **채팅방 내에서 메시지 전송 API** 

따라서 위와 같은 상황에서 복수의 이미지를 전송하는 상황 (1장 이상을 포함)에서는 동일한 API를 사용하는 것입니다. 



이미지를 업로드하는 경우, 클라이언트가 보내야 할 값은 다음과 같습니다.

- **파일 (MultipartFile[] - 복수의 사진이 담길 수 있으므로, 배열 형태)**
- **업로드 경로 (어디에 저장하는 지를 나타냄)**



***파일명 관리 로직 설명***

하지만, 우리 서비스에서는 매번 이미지 업로드 API가 호출될 때마다 고유한 파일명을 생성하지 않습니다. 이유는 불필요한 API 요청이 많아지기 때문입니다. 매 업로드 요청마다 파일명을 생성한다면, 기존에 S3에 저장되어 있지만, 더 이상 해당 물품과 관련이 없어진 이미지 파일을 삭제하기 위해 삭제 API 역시 부수적으로 요청을 해야 하는 상황이 생깁니다. (이유에 대해서 이해가 가지 않는다면 알려주시면 다시 구두로 설명드리겠습니다.) 

따라서 기존 저장된 파일이 있는 경우는 해당 파일명들을 활용해서 업로드할 때 재활용하게 됩니다. (한 번 생성된 파일명은 삭제되기 전까지 재활용됩니다.)

따라서 다시 정리하면, 이미지를 업로드하는 과정에서 클라이언트가 보내야 할 값은 다음과 같습니다. 

**필수**

- 파일 (MultipartFile[] - 복수의 사진이 담길 수 있으므로, 배열 형태)
- 업로드 경로 (어디에 저장하는 지를 나타냄)

**조건부**

- 기존 업로드 때 활용됐던 파일명 (리스트)

*위에서도 언급했지만 특정 상품에 대해서 저장되어 있는 사진이 있는 경우에만 기존 파일명을 활용할 수 있으므로 **조건**부로 클라이언트가 요청하게 됩니다.*

___



**<요청 예시> : 기존 파일명이 없는 경우** 

```json
1. 신고 또는 채팅방에서 복수의 이미지를 업로드 할 때
2. 최초 상품 업로드 할 때 
3. 상품 수정 시 기존 이미지가 없었는데 수정하면서 이미지를 추가할 때
(예를 들어 0장 -> 3장, 0장 -> 5장 등)

위 세 케이스에서는 아래와 같이 form-data를 요청하시면 됩니다. 
(기존에 저장된 이미지가 없으므로, 이전 파일명들을 보낼 값이 없다고 생각하셔도 좋습니다.)

// form-data 

// 이미지 파일명 아닙니다. 이미지 파일입니다. 
"files" : 이미지 파일(최소 한 장 이상) 

// 이미지 저장 경로 값은 아래 기준을 참고해주세요.

// 상품 업로드 시점 : origin/product/
// 신고 시점 : origin/report/ 
// 채팅 이미지 메시지 시점 : origin/chat/ 

"uploadDirPrefix" : 이미지 저장 경로 

이 때, "prevFileNames" 요소는 form-data에서 제외한 상태로 보내셔야 합니다. 
null 값을 넣어서 보내는 것과 다르다는 점 꼭 인지하고 클라이언트 측 코딩 부탁드립니다. 
(이 부분 역시 이해가 안 되시면 이 부분에 대해서 질문해주시면 구두로 설명드리겠습니다.)


```

**<응답 예시>**

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "AWS 복수 이미지 업로드 성공",
    "responseData": [
        "example.jpg",
        "example2.jpg"
    ]
    // 복수 이미지 업로드 API 결과 반환되는 값은 List 입니다.
    // 이 값을 활용해서 상품 업로드 또는 상품 수정 시 이미지 경로 값으로 활용하면 됩니다.
 
}


[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "IOException, 복수 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 업로드 API, 파일 타입 문제로 이미지 삭제 API 처리과정에서 예외발생",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "InterruptedException, 복수 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}


```



**반대로**, 기존 파일명을 서버에 전송하는 상황은 어떤 것들이 있을까요?

키워드는 **상품 수정**입니다. 특히 기존 이미지가 이미 있었고, 이 이미지(들)을 수정하는 경우에 해당합니다.

다음과 같은 상황이 예가 됩니다.

- 상품 수정 시, 기존 이미지가 N장이 있고, 수정하면서 업로드한 이미지 수가 N장인 경우

예) 

기존 업로드 했던 이미지 수가 5장이고, 새로 업로드한 이미지 수 역시 5장인 경우 

(주의! 이 경우는 유저가 기존 이미지 5장에서 이미지를 수정한 경우! 수가 같다고 동일한 이미지를 의미하는 것이 아님!)

- 상품 수정 시, 기존 이미지가 N장이 있고, 수정하면서 업로드 한 이미지 수가 N+M인 경우 

예)

기존 업로드 했던 이미지 수가 2장이고, 새로 업로드한 이미지가 5장인 경우

위 두 가지 상황에서는 아래와 같이 요청하시면 됩니다.

**<요청 예시> : 기존 파일명이 있는 경우**

```json
// form-data //


// 이미지 파일명 아닙니다. 이미지 파일입니다. 
"files" : 이미지 파일(최소 한 장 이상) 

// 기존 파일명을 활용하는 경우는 상품 수정 뿐이므로, 업로드 경로 Prefix는 다음과 같습니다. 
"uploadDirPrefix" : "origin/product/"

// 기존 저장됐던 파일명 리스트
"prevFileNames" : ["example.jpg", "example2.jpg"]
```



**<응답 예시>**

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "AWS 복수 이미지 업로드 성공",
    "responseData": [
        "example.jpg",
        "example2.jpg"
    ]
    // 복수 이미지 업로드 API 결과 반환되는 값은 List 입니다.
    // 이 값을 활용해서 상품 업로드 또는 상품 수정 시 이미지 경로 값으로 활용하면 됩니다.
}


// 아래는 복수 이미지 사진 업로드 API 처리 과정 중 에러가 발생한 경우입니다.

// 352 : IOException, 복수 이미지 사진 업로드 API
// 352 : AmazonServiceException, 복수 이미지 사진 업로드 API
// 352 : InterruptedException, 복수 이미지 사진 업로드 API

// 위 statusCode 응답 시, "요청하신 작업을 수행하지 못했습니다. 일시적인 현상이니 잠시 후 다시 시도해주세요. (오류코드 : statusCode)" Alert 띄어주고 해당 페이지(상품 업로드 페이지)에 그대로 유지 해주시면 됩니다. 

[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "IOException, 복수 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}


[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 업로드 API, 파일 타입 문제로 이미지 삭제 API 처리과정에서 예외발생",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}


[HTTP/1.1 200]
{
    "statusCode": 352,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "InterruptedException, 복수 이미지 사진 업로드 API",
    "requestPath": "/api/multi-img/upload",
    "pathToMove": null
}
```