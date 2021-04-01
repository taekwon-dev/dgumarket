# 회원정보 수정 API



**URL** : `/api/user/profile/update` 

**Method** : `POST`

**Authentication** : O (Authorization Header - Access Token) / HTTP Cookie - Refresh Token

**Request Body** :  

프로필 수정 시 **프로필 이미지 값이 포함되는 지 여부**에 따라서 **RequestBody는 두 가지 케이스**로 나뉩니다.  

프로필 사진 업로드 또는 삭제로 인해 **프로필 이미지 값을 변경해야 하는 경우**에는 아래와 같이 프로필 이미지 경로 값에 **`profileImageDir`** 요소를 추가하시면 됩니다. 

```json
// 프로필 이미지를 새로 추가 또는 다른 사진으로 변경하는 경우
// 기존에 있던 사진을 삭제하는 경우 
{
"profileImageDir" : null or AWS S3에 저장되어 있는 프로필 이미지 파일명
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



```json
// 프로필 이미지 경로 값이 변동이 없는 경우는 아래와 같이 요청해주시면 됩니다.
// (프로필 경로 값이 포함된 `profileImageDir` 요소를 제거하시고 요청해주시면 됩니다)
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

**Response** :

```json
[HTTP/1.1 200]
{
    "statusCode": 200,
    "message": "회원 정보 수정",
    "responseData": null
}

// 회원정보 수정 API에 대한 예외 처리는 공통적으로 처리되는 예외에 포함되어 여기에 기술하지 않습니다. 
```

