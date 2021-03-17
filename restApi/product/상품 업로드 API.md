---
REST API - Product created by TK 
---

## Product 

___

**URL** : `/api/product/index`

**Method** : `POST`

**Auth required** : NO  

**Permissions required** : None

**Data constraints** : `{"lastCategoryId" : "0"}`, **로그인 상태**에서만 서버 측에서 활용하는 값, **비로그인 상태**에서도 값이 넘어가도 서버 측에서 활용하지 않으므로 **로그인 여부**와 관계없다. 최초 `/api/product/index`를 요청할 때 Default 값은 0. (서버 측에서 lastCategoryId 보다 큰 카테고리 id 중 3개(페이징 사이즈)를 응답하는 구조인데, 카테고리 id의 최솟값이 1 이므로 최초 디폴트 값은 0으로 설정해서 보낸다). **무한 스크롤 감지 후** /api/product/index를 요청하는 경우는 클라이언트가 응답 받은 가장 마지막 카테고리의 id를 보낸다. 

아래 응답 예시를 예로 들면, 가장 마지막으로 응답 받은 카테고리 ID가 **3**이므로 lastCategoryId의 값은 3으로 설정 후 보낸다. 

## Success Responses

---

**Condition** : [**비로그인 상태**] - 인기 카테고리 (<u>도서, 기프티콘, 의류</u>) 또는 [**로그인 상태**] - 유저의 관심 카테고리

**Code** : `200 OK`

**Content**

`category_id`: 카테고리 고유 ID 

`category_name`: 카테고리 명 

`productsList`: 해당 카테고리에 속하는 물건 리스트 

​		`id` : 중고물품 고유 ID

​	    `thumbnailImg` : 중고물품 메인 이미지의 썸네일 디렉토리 

​		`title` : 중고물품 게시글 타이틀

​	    `price` : 중고물품 가격

​		`likeNums` : 중고물품 게시글 좋아요 수 

​	    `chatroomNums` : 중고물품 게시글에서 생성된 채팅방 수 

​	    `category_id` : 중고물품의 카테고리 (서버 로직 활용 요소)

​		`lastUpdatedDatetime` : 중고물품 게시글 업로드 시간 (또는 마지막 수정 시간)



**example**

```json
[
    {
        "category_id": 1,
        "category_name": "도서",
        "productsList": [
            {
                "id": 1,
                "thumbnailImg": "이미지 파일명 리스트",
                "title": "도서1",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "category_id": 1,
                "lastUpdatedDatetime": "2020-12-27T18:13:28"
            },
            {
                "id": 2,
                "thumbnailImg": "이미지 파일명 리스트",
                "title": "도서2",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "category_id": 1,
                "lastUpdatedDatetime": "2020-12-27T18:13:33"
            },
            {
                "id": 3,
                "thumbnailImg": "이미지 파일명 리스트",
                "title": "도서3",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "category_id": 1,
                "lastUpdatedDatetime": "2020-12-27T18:13:37"
            }
        ]
    },
    {
        "category_id": 2,
        "category_name": "기프티콘",
        "productsList": [
            {
                "id": 5,
                "thumbnailImg": "이미지 파일명 리스트",
                "title": "기프티콘1",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "category_id": 2,
                "lastUpdatedDatetime": "2020-12-27T18:13:52"
            },
            {
                "id": 6,
                "thumbnailImg": "이미지 파일명 리스트",
                "title": "기프티콘2",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "category_id": 2,
                "lastUpdatedDatetime": "2020-12-27T18:13:54"
            },
            {
                "id": 7,
                "thumbnailImg": "이미지 파일명 리스트",
                "title": "기프티콘3",
                "price": "상품 가격",
                "likeNums": 0,
                "chatroomNums": 0,
                "category_id": 2,
                "lastUpdatedDatetime": "2020-12-27T18:13:57"
            }
        ]
    },
    {
        "category_id": 3,
        "category_name": "의류/잡화",
        "productsList": []
    }
]
```

