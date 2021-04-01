# **회원정보 조회 API**



**URL** : `/api/user/profile/read` 

**Method** : `GET`

**Authentication** : O (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  X

**Response** :

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "회원 정보 조회",
    "responseData": {
        "profileImageDir": "289d33f2055a4163b7acf3176e0bb326.jpg",
        "nickName": "윤태권",
        "productCategories": [
            {
                "category_id": 2,
                "category_name": "음반/DVD"
            },
            {
                "category_id": 5,
                "category_name": "가전/디지털"
            },
            {
                "category_id": 1,
                "category_name": "도서"
            },
            {
                "category_id": 14,
                "category_name": "의류/잡화"
            },
            {
                "category_id": 7,
                "category_name": "완구/취미"
            }
        ],
        "warn": false
    }
}

// 회원정보 조회 API에 대한 예외 처리는 공통적으로 처리되는 예외에 포함되어 여기에 기술하지 않습니다. 
```



---

## AWS S3 저장된 이미지 조회 

### 우리의 이미지는 어디에 있을까?

AWS S3 스토리지에 저장되어 있고, 원본 이미지가 업로드되면 해당 원본 이미지를 활용해서 필요한 리사이즈 처리를 진행합니다. (AWS  Lambda)

**이미지 요청 URL 예시**

https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/origin/user-profile/066a85603322404e8aaf4be78f157464.jpg

**공통 Prefix** 
https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/

**원본 이미지**

**프로필 이미지**
origin/user-profile/파일명.jpg

**채팅 이미지**
origin/chat/파일명.jpg

**상품 이미지 **
origin/product/파일명.jpg

**리사이즈 이미지**

** 프로필 이미지 ** 
(50 / 107 / 222)

resize/user-profile/50x50px/파일명.jpg
resize/user-profile/107x107px/파일명.jpg
resize/user-profile/222x222px/파일명.jpg

** 채팅 이미지 ** 
(400 / 1000)

resize/chat/400x400px/파일명.jpg
resize/chat/1000x1000px/파일명.jpg

** 상품 이미지 ** 
(50 / 247 / 700 / 1000) 

resize/product/50x50px/파일명.jpg
resize/product/247x247px/파일명.jpg
resize/product/700x700px/파일명.jpg
resize/product/1000x1000px/파일명.jpg

_____

**이미지가 저장된 위치를 DB에 저장할 때 파일명만 저장하는 이유**는 요청되는 이미지의 정해진 `적정` 크기가 상이하기 때문입니다. 또한 각 케이스별로 `Prefix`는 이미 정해진 값이므로 **`파일명`이 유일한 `변수`**가 됩니다. 

**예를** 들어서, 이미지 요청 URL을 만드는 과정을 설명하겠습니다.

**상황** : 유저 프로필 (50x50px) 요청하는 상황 

**공통 Prefix** : https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/

**유저 프로필 & 리사이즈 크기 Prefix** : resize/user-profile/50x50px/

**파일명** : 회원정보 조회 시 받은 (DB) 값 활용 

**최종 이미지 요청 주소** :  https://dmkimgserver.s3.ap-northeast-2.amazonaws.com/resize/user-profile/50x50px/파일명.jpg

____

AWS S3에 저장되어 있는 이미지를 요청하는 경우, 리사이즈 처리된 이미지를 우선적으로 요청합니다. (렌더링 효율성 측면)

하지만, 아래와 같은 상황에서는 **`리사이즈된 이미지가 생성되기 전`임에도 리사이즈된 이미지를 `요청`하는 문제**가 생길 수 있습니다.

예를 들어, 프로필 정보를 수정 완료 후 프로필 조회 페이지로 이동하는데, 그 시점에 리사이즈 처리가 되지 않았을 수 있습니다.

위 예시와 같은 경우, 다음과 같은 로직을 통해 이미지를 렌더링합니다.

**(*중요)**

최초 리사이즈 이미지를 요청하고, 리사이즈된 이미지가 아직 처리 전이라면 (존재하지 않는다면) **`403 Forbidden` 상태를 응답**한다. 이 경우 원본 이미지를 재요청을 통해 이미지 속성에 `원본 이미지` 주소를 넣어주시면 됩니다. 

이 과정에서 Ajax를 활용하게 되면 이미지를 요청하는 도메인과 이미지가 저장된 서버의 도메인이 다른 경우 CORS 이슈가 발생하지만,

AWS S3에서 `CORS` 관련 설정을 통해 현재(2021-02-26) 기준 `http:localhost:8081`에 대한 요청을 허용하도록 설정했습니다.

로컬에서 테스트 하실 때 CORS 문제가 그래도 발생하면 바로 알려주세요 :-) 

____

