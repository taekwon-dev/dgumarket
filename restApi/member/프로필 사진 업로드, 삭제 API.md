## 유저 프로필 정보 수정 (*<u>이미지 로직</u>) - 프로필 사진 업로드 / 삭제 API 

**유저 프로필 정보 `수정하기` 버튼 클릭 시점** 

`수정하기 버튼 클릭` 했을 때 케이스는 크게 세 가지로 나뉩니다. 

**ㄱ) 수정하기 버튼 클릭 ---- 프로필 사진 업로드 API ---- 회원정보 수정 API** 

**ㄴ) 수정하기 버튼 클릭 ---- 프로필 삭제 API ---- 회원정보 수정 API**

**ㄷ) 수정하기 버튼 클릭 ---- 회원정보 수정 API** 



1) **`프로필 사진 업로드 API` 호출하는 경우** --- ㄱ 

유저의 프로필 이미지가 이미 있는 경우에 다른 프로필 이미지로 변경하는 경우 (A to B : A에서 B로 변경하는 경우)

유저의 프로필 이미지가 없다가 프로필 이미지를 등록하는 경우 (Null to A : Null에서 A로 등록하는 경우)

위 두 케이스에서는 프로필 사진 업로드 API를 호출한다. 

```json
프로필 이미지 업로드 API : `/api/user/profile/image-upload`

* 요청 

RequestBody (form-data)
file : 이미지 파일 (Multipart)
prevFileName : 기존 파일명 (example.jpg) 
	
	prevFileName에 기존 파일명을 입력하는 경우 (A -> B 사진으로 변경하는 경우)
	prevFileName에 기존 파일명에 null을 입력하는 경우 (null -> A 사진으로 변경하는 경우, 기존 파일명이 없으므로)

* 응답

[프로필 사진 업로드 성공]
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "유저 프로필 사진 업로드 성공",
    "responseData": "example.jpg"
  // (이 값을 활용하여 회원정보 수정하기 API에서 프로필 이미지 값으로 활용하시면 됩니다, 최초 이미지 업로드 할 때 서버 측에서 생성된 파일명을 해당 파일이 삭제되기 전까지 하나의 파일명으로 관리하게 됩니다)
}

[프로필 사진 업로드 실패]
// 350 : IOException, 회원 프로필 사진 업로드 API
// 350 : AmazonServiceException, 회원 프로필 사진 업로드 API
// 350 : SdkClientException, 회원 프로필 사진 업로드 API
// 350 : InterruptedException, 회원 프로필 사진 업로드 API

// 위 statusCode 응답 시, "요청하신 작업을 수행하지 못했습니다. 일시적인 현상이니 잠시 후 다시 시도해주세요. (오류코드 : statusCode)" Alert 띄어주고 해당 페이지(상품 업로드 페이지)에 그대로 유지 해주시면 됩니다. 

[HTTP/1.1 200]
{
    "statusCode": 350,
    "timestamp": "2021-03-29T01:09:40.160+00:00",
    "message": "IOException, 회원 프로필 사진 업로드 API",
    "requestPath": "/api/user/profile/image-upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 350,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "AmazonServiceException, 회원 프로필 사진 업로드 API",
    "requestPath": "/api/user/profile/image-upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 350,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "SdkClientException, 회원 프로필 사진 업로드 API",
    "requestPath": "/api/user/profile/image-upload",
    "pathToMove": null
}

[HTTP/1.1 200]
{
    "statusCode": 350,
    "timestamp": "2021-03-29T01:08:27.438+00:00",
    "message": "InterruptedException, 회원 프로필 사진 업로드 API",
    "requestPath": "/api/user/profile/image-upload",
    "pathToMove": null
}
```



2**) `프로필 사진 삭제 API` 호출하는 경우** --- ㄴ

유저의 프로필 이미지가 이미 있는 경우에서 다시 기본 프로필 사진으로 변경하는 경우 (**A to Null**로 변경하는 경우)

스토리지 서비스 AWS S3에 저장되어 있는 `원본 사진` 삭제 처리를 위한 API를 호출 하게 됩니다.

기존 프로필 이미지의 파일명(파일 타입 포함)을 서버에 전송합니다. (해당 파일에 대한 삭제 로직을 수행하기 위해서)

```json
* 요청 

RequestBody (form-data)
deleteFileName: 삭제할 파일명 (example.jpg) 
	
프로필 이미지 삭제 API : `/api/user/profile/image-delete`

* 응답

[프로필 사진 삭제 성공]
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "유저 프로필 사진 삭제 성공",
    "responseData": null // (*null 값을 활용해서 이 요청을 보내는 유저의 회원정보 중 프로필 이미지 값을 null로 처리)
}

[프로필 사진 삭제 실패]
// 351 : AmazonServiceException, 회원 프로필 사진 삭제 API

// 위 statusCode 응답 시, "요청하신 작업을 수행하지 못했습니다. 일시적인 현상이니 잠시 후 다시 시도해주세요. (오류코드 : statusCode)" Alert 띄어주고 해당 페이지(상품 업로드 페이지)에 그대로 유지 해주시면 됩니다. 

[HTTP/1.1 200]
{
    "statusCode": 351,
    "timestamp": "2021-03-29T01:31:44.454+00:00",
    "message": "AmazonServiceException, 회원 프로필 사진 삭제 API",
    "requestPath": "/api/user/profile/image-delete",
    "pathToMove": null
}
```



3) **`프로필 사진 업로드 또는 삭제 API` 호출 없이 프로필 정보 로직을 진행하는 경우** --- ㄷ

유저의 프로필 이미지가 NULL 인 경우에서 다른 이미지를 추가하지 않고 다른 프로필 정보 수정하는 경우 (**Null -> Null**)

유저의 프로필 이미지가 A로 등록된 경우 + 다른 정보만 수정하고 프로필 이미지는 그대로인 경우 (**A -> A**)

위 두 가지의 경우에서는 프로필 업로드 또는 삭제 API를 호출하지 않고 바로 **수정 API**를 호출한다. 



이 경우는 회원정보 API 수정을 위해 RequestBody를 넣으실 때 **`프로필 이미지` 요소는 제외해서 요청**하시면 됩니다

예로 위 내용을 다시 설명하겠습니다.

```json
[예시]

프로필 이미지 저장 경로가 변경되지 않으므로 (Null -> Null or A -> A)
프로필 이미지 저장 경로를 Request Body 요소에서 제외하시고 보내시면 됩니다. (아래 JSON 요청 참고)

* 주의 : 프로필 경로에 null을 입력해서 요청하는 것과 아래와 같이 해당 요소를 제외하고 보내는 것은 다른 경우입니다. null 을 입력해서 요청하는 경우는 프로필 이미지 경로 값에 null을 저장하게 됩니다. 

따라서 `프로필 사진 업로드 또는 삭제 API` 호출 없이 프로필 정보 로직을 진행하는 경우는 최종 회원정보 수정 API를 요청 하실 때 아래와 같은 Request Body를 구성하셔서 보내시면 됩니다. 

{
 "nickName" : "수정된 닉네임",
 "productCategories" :  
    [ 
                            {
                                "category_id" : 1, 
                                "category_name" : "도서"
                            },
                            {
                                "category_id" : 12, 
                                "category_name" : "홈 인테리어"
                            },
                                                    {
                                "category_id" : 13, 
                                "category_name" : "반려동물 용품"
                            },
                                                    {
                                "category_id" : 14, 
                                "category_name" : "기타"
                            }

    ]
}
```