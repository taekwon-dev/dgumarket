# 유저 관심물건 조회하기

* 인증여부 : 필요

url에 아무런 추가 param 을 붙히지 않고 요청하였을 경우 서버에서는 기본적으로
page=0, size=20, sort=createdDate,desc(최신순)
에 해당되는 자료를 뿌려준다.


**URL** : `/api/user/favorites`

**Method** : `GET`

**Authentication required** : `yes`

**Request Param** :

`page` : 보여줄 페이지, 무한스크롤시 스크롤 최하단에 닿을때마다 page 1씩 늘어남-> 추가적인 정보를 가져옴(number) ( 0 부터 시작 )

`size` : 한 페이지에 보여줄 물건개수(number) (임의로 정할 수 있음)

`sort` : 세부정렬(좋아요수(=likeNums), 채팅수(=chatroomNums, 저가순(=price,asc), 고가순(=price,desc), 오래된 순(=createDatetime,asc), 최신 순(=createDatetime,desc)

오래된순과 최신순은 물건업로드 순을 뜻한다.

http://localhost:8081/api/user/favorites?sort=createdDate,desc&page=0&size=10

=> 최신순/관심물건 10개씩 가져오기

---

### response body

`id`: 물건고유아이디,

`thumbnailImg`: "물건 섬네일 이미지(이미지 없을경우에는 null로 온다)",

`title`: "물건제목"

`price`: "물건가격"

`likeNums`: 14,

`chatroomNums`: 14,

`likeStatus`: 좋아요상태("like" ->
해당물건좋아요 상태, 이미 "like" 가 된 상태가 내려온다.)

`lastUpdatedDatetime`: 물건수정시간,

`uploadDatetime`: 물건업로드시간,

`transaction_status_id`: 물건거래상태

--- 
이 부분은 좋아요에 대한 기능을 설명할 때 다시 언급하겠습니다.

간단히 설명하자면 현재 물건의 likeStatus가 nolike 인지 like인지를 서버에게 전달해서 like 일 경우에는 "좋아요취소", nolike 인 경우에는 "좋아요" 처리를 하는 구조입니다.

이 과정에서
`likeStatus` 의 "like"가 현재 물건이 좋아요인지 좋아요상태가 아닌지를 알려주는 역할을 합니다.



### example response


```json

{
    "message": "favorites products",
    "status": 200,
    "data": {
        "total_size": 3,
        "page_size": 3,
        "productsList": [
            {
                "id": 1,
                "thumbnailImg": null,
                "title": "도서_1",
                "price": "￦1,000",
                "likeNums": 2,
                "chatroomNums": 0,
                "likeStatus": "like",
                "lastUpdatedDatetime": "2021-02-05T12:01:56",
                "uploadDatetime": "2021-01-02T06:42:18",
                "transaction_status_id": 0
            },
            {
                "id": 4,
                "thumbnailImg": null,
                "title": "맨큐의경제학팝니다",
                "price": "￦4,000",
                "likeNums": 1,
                "chatroomNums": 1,
                "likeStatus": "like",
                "lastUpdatedDatetime": "2021-03-11T22:47:11",
                "uploadDatetime": "2021-01-02T06:42:34",
                "transaction_status_id": 0
            },
            {
                "id": 5,
                "thumbnailImg": "https://dgu-springboot-build.s3.ap-northeast-2.amazonaws.com/sample/138720282_1_1611149403_w292.jpg",
                "title": "음반/DVD_1",
                "price": "￦5,000",
                "likeNums": 1,
                "chatroomNums": 0,
                "likeStatus": "like",
                "lastUpdatedDatetime": "2021-03-09T22:31:36",
                "uploadDatetime": "2021-01-02T06:51:58",
                "transaction_status_id": 0
            }
        ]
    }
}



```