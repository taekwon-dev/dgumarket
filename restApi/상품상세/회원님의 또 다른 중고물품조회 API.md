# 이 회원님의 또 다른 중고물품 조회하기(개별물건정보 밑에 있는 섹션 )

* 인증여부 : 불필요

'이 회원님의 또 다른 중고물품을 조회하기' 위해서는 '물건을 올린 회원(유저)의 고유 아이디' 가 필요하다. '물건을 올린 회원(유저)의 고유 아이디' 는 개별물건정보조회API 요청시 응답 response body 안에 `userId(유저 고유 아이디)`가 있다. 이 값을 이용해 해이 회원님의 또 다른 중고물품을 조회하는 API 경로를 만들고 서버로 요청하게 된다.

---
### 결론
최초로 1. 개별물건정보조회( /api/product/{product}/info ) 요청을 통해 데이터를 가져오게 되면 여러 값들 중 `userId` 값을 받게 된다. 그리고 받은 `userId` 를 이용해  2. 회원(유저)님의 또 다른 중고물품들을 가져오게 된다. `/api/user/{{userId}}/products?size=4&product_set=sale&except_pid={product}` )

=> 기본적으로 가장 최신으로 올려진 물건들을 가져오게 된다.

**URL** : `/api/user/{{userId}}/products?size=4&product_set=sale`

**Method** : `GET`

**Authentication required** : `no`

### request param(path)

`{{userId}}` : 유저 아이디 ( 이 유저아이디는 개별물건정보 조회 요청을 통해 받은 결과값 중

`userId` 를 가져와 요청한다.)

### request param(url)

`except_pid` : 11 (제외하고 싶은 물건 번호)
만약 1번물건을 제외해야한다면
1을 넣고 요청한다.

`size` : 4 (최대 가져오는 물건개수)

`product_set` : sale ( 판매중 )

해당 유저가 `판매(sale)` 하고 있는 물건들을 최대 4개까지 최신순(판매물건 최신 업로드 순)으로 가져온다.

--- 

### response body

`message` : 응답메시지

`status` : 응답코드

`data` : 물건리스트 정보

---

`total_size` : 총 판매 물건 개수

`page_size` : 가져오는 데이터 크기

`productsList` (json oject array): 물건리스트

---
`productsList`

개별 물건들(json obejct) 에 대한 json 필드 설명

`id` : 물건아이디( 이용 해당 물건페이지로 이동할 수 있도록 한다. )

`thumbnailImg` : 물건 썸네일 이미지 경로

`title` : 물건제목

`price` : 물건가격

`likeNums` : 좋아요 수

`chatroomNums` : 채팅방 수

`lastUpdateDatetime` : 물건 수정 시간

`uploadDatetime` : 물건 업로드 시간

`transaction_status_id` : 물건 거래상태(0: 판매중, 2:거래완료)