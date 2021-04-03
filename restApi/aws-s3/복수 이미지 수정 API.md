## **복수 이미지 수정 API** (인증)

복수 이미지 수정API는 AWS S3를 이용한 복수의 이미지 **업로드와 삭제** 처리를 처리해주는 API 입니다.

현재 우리 서비스에서 **복수 이미지 수정 API**를 요청하는 경우는 아래와 같습니다.

- **상품 수정**

상품 수정 시, 기존 이미지가 N장인 경우에서 수정하면서 N-M 장으로 수정하는 경우에 해당합니다.

예를 들어, 기존 이미지가 5장이고, 수정하면서 2장으로 수정하는 경우

**이 경우는 기존 5장에 대한 파일명 리스트에서 순서대로, 첫 번째 두 번째의 파일명은 재활용해서 복수 이미지 업로드 처리를 하고, 나머지 세 장에 대해서는 삭제 처리를 하게 됩니다.** 



**<요청 예시>**

```json
1. 기존 이미지가 N장 있었고, 수정하면서 N-M장이 된 경우 (5 -> 3, 5 -> 2 ...)


// form-data

// 이미지 파일명 아닙니다. 이미지 파일입니다. 
"files" : 이미지 파일(최소 한 장 이상) 

// 기존 파일명을 활용하는 경우는 상품 수정 뿐이므로, 업로드 경로 Prefix는 다음과 같습니다. 
"uploadDirPrefix" : "origin/product/"

// ex) 기존 업로드했던 이미지 파일명 리스트
"prevFileNames" : ["a.jpg" , "b.jpg", "c.jpg"]


```

**<응답 예시>**

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "AWS 복수 이미지 수정 성공 (=복수 업로드 + 삭제 성공)",
    "responseData": [
        "example.jpg",
        "example2.jpg"
    ]
  
    // 복수 이미지 업로드 API 결과 반환되는 responseData는 List 형태 입니다. 
    // 이 값을 활용해서 상품 수정 시 이미지 경로 값으로 활용하면 됩니다.
}


// 아래는 복수 이미지 사진 수정 API 처리 과정 중 에러가 발생한 경우입니다.

// 354 : AmazonServiceException, 복수 이미지 사진 수정 API
// 354 : IOException, 복수 이미지 사진 수정 API
// 354 : InterruptedException, 복수 이미지 사진 수정 API

// 위 statusCode 응답 시, "요청하신 작업을 수행하지 못했습니다. 일시적인 현상이니 잠시 후 다시 시도해주세요. (오류코드 : statusCode)" Alert 띄어주고 해당 페이지(상품 업로드 페이지)에 그대로 유지 해주시면 됩니다. 

 




[HTTP/1.1 200]
{
    "statusCode": 354,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "IOException, 복수 이미지 사진 수정 API",
    "requestPath": "/api/multi-img/patch",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 354,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 수정 API",
    "requestPath": "/api/multi-img/patch",
    "pathToMove": null
}


[HTTP/1.1 200]
{
    "statusCode": 354,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 수정 API, 기존 파일명 수보다 큰 인덱스 사진의 원본 삭제 과정",
    "requestPath": "/api/multi-img/patch",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 354,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 복수 이미지 사진 수정 API, 파일 타입 문제로 이미지 삭제 API 처리과정에서 예외발생",
    "requestPath": "/api/multi-img/patch",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 354,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "InterruptedException, 복수 이미지 사진 수정 API",
    "requestPath": "/api/multi-img/patch",
    "pathToMove": null
}
```