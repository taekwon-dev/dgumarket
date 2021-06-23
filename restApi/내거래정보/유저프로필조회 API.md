## 수정사항이력

3.17 null 값 설명추가, 필드에 사용자가 경고당하고 있는 유저인지 알려주는 `warn`(boolean) 필드 추가

### 꼭! 읽을 것 ( 2/28 )
예외처리에 대해서 어떻게 클라이언트가 처리해야 할지는 아직 최종적인 문서로 정립되지 않은 상태입니다. 에러코드가 정립되면 어떤 에러코드가 있고 그에 따라서 어떻게 클라이언트가 행동해야 하는 지에 대한 문서를 작성하여 넘겨줄 생각입니다.
아직은 우선 요청성공 -> 200 코드가 나왔을 경우에 대한 렌더링 작업등을 먼저 하시면 됩니다.

except)

`statusCode` : 응답코드

`timeStamp` : 에러 발생 시간

`message` : 메시지 내용

`description` : 에러가 발생한 엔드포인트


### 꼭 읽을 것! 경고 필드 및 프로필 이미지 없을 경우 null로 온다는 사실 추가(3/17)
추가되는 `warn` 필드의 경우 false 또는 true 값이 오게 됩니다.
만약 false 의 경우에는 아무런 조치를 하지 않아도 괜찮지만, 만약 true 일 경우 유저거래정보 페이지에 있는 유저프로필정보를 보여주는 부분에 해당 유저가 현재 '경고'를 받고 있다는 사실을 알려주어야 합니다. 이 경우 클라이언트는 간단한 UI로 알려줄 수 있습니다.( 경고 마크나, 기타 등등)

실제로 경고에대해서 자세히 설명하면, 관리자로부터 유저가 경고를 받아도 실제로 바로 경고 유무가 true가 되지는 않는다. 경고가 3 또는 4개가 되었을 때 경고값이 true 가 되며 최대 경고 표시가 노출되는 기간은 최대 일주일이다. 가령 경고를 2번까지 받은 유저는 실제로 경고는 받았지만, 유저들에게 보여지는 부분에 있어서 경고유무값이 false 이기 때문에 어떤 패널티도 없다. 그런데 만약 관리자로 부터 경고를 3회 받았을 경우에는 3회째 경고를 받은 시점으로 부터 일주일이 지나지 않았다면 warn 필드의 값은 true 로 내려온다.


또한 유저프로필이 만약 없을경우 즉, db 상에 아무것도 없을 경우(null) 클라이언트로 정보를 보여줄 때는 profileImageDir 필드는 존재하며 값은 null의 형태로 오게됩니다.

### 꼭 읽을 것! (5/2 수정사항 추가)
기존 접근불가능 했던 api에 대해서 예외처리 화면으로 이동한 것을 이제는 /shop/main/index (인덱스페이지)로 이동하기위해 예외 응답 pathToMove 필드 값은 /shop/main/index 로 내려옴


## 수정사항 추가 6/18
- 예외발생 시 커스텀에러응답코드 같이 반환하도록 수정

---

# 유저 간략 프로필 조회하기

* 인증여부 : 불필요(선택)

구체적으로 인증을 하고 요청할 때와 인증을 하지 않고 요청을 하지 않을 때 응답형태가 다릅니다.

인증 -> 나의 프로필 조회시 응답 필드 중 `message` : my_profile 이고

인증 X -> 프로필 조회시 응답 필드 중 `message` : user_profile 이 된다.

**URL** : `/api/user/{user-id}/shop-profile`

**Method** : `GET`

**Authentication required** : `no`

### response body

`message` : 응답메시지
인증하고 본인의 프로필 조회시 : "my_profile"
인증하지 않고 프로필 조회시 : "user_profile"


`status` : 응답코드

`data` : 프로필 정보

---

`profileImageDir` : 프로필이미지경로 ( 만약 이미지가 없을 경우에는 null 로 온다. )

`nickName` : 닉네임

`productCategories` : 관심있어어 하는 유저 관심카테고리 목록 (json object array)

`warn`(boolean) : 경고 유무(경고를 받았을 경우에는 true, 그렇지 않았을 경우에는 false 를 받게된다.)

---

`productCategories`(json object array)

`category_id` : 관심 카테고리 고유 아이디

`category_name` : 관심 카테고리의 이름


ex)

```json


{
    "message": "user_profile",
    "status": 200,
    "data": {
        "profileImageDir": null,
        "nickName": "윤태권",
        "productCategories": [
            {
                "category_id": 3,
                "category_name": "뷰티"
            },
            {
                "category_id": 10,
                "category_name": "홈 인테리어"
            },
            {
                "category_id": 2,
                "category_name": "음반/DVD"
            },
            {
                "category_id": 1,
                "category_name": "도서"
            }
        ],
        "warn": false
    }
}



```


## 예외응답
아래와 같은 예외 응답을 받을 경우에는 바로 예외페이지으로 이동시킨다.
굳이 경고문구를 안띄워줘도 된다고 생각함(띄워줘도 상관없음)

**Code** : `400 Bad Request`

**Content**

`statusCode`: custom 에러 응답 코드
`timestamp` : 요청시간
`message` : 요청에러이유
`description` : 요청한 URL
`pathToMove` : 리다이렉트 해야하는 페이지 URL

// 존재하지 않거나 탈퇴한 유저의 프로필 정를 조회하려고 하는 경우
```json
{
  "statusCode": 102,
  "timestamp": "2021-06-18T10:24:37.569+00:00",
  "message": "존재하지 않거나 탈퇴한 유저 입니다.",
  "requestPath": "uri=/api/user/1/shop-profile",
  "pathToMove": "/shop/main/index"
}
```

// 관리자에 의해 이용제재 받고 있는 유저의 프로필을 조회하려고 하는 경우
```json
{
  "statusCode": 103,
  "timestamp": "2021-06-18T10:26:40.416+00:00",
  "message": "관리자로부터 이용제재 받고 있는 유저입니다.",
  "requestPath": "uri=/api/user/1/shop-profile",
  "pathToMove": "/shop/main/index"
}
```

// 내가 차단한 유저의 프로필정보를 조회할 경우(로그인)

```json

{
  "statusCode": 104,
  "timestamp": "2021-06-18T10:27:51.230+00:00",
  "message": "차단한 유저에 대한 정보를 조회할 수 없습니다.",
  "requestPath": "uri=/api/user/1/shop-profile",
  "pathToMove": "/shop/main/index"
}

```

// 나를 차단당한 유저의 프로필정보를 조회할 경우(로그인)

```json

{
  "statusCode": 105,
  "timestamp": "2021-06-18T10:27:23.682+00:00",
  "message": "나를 차단한 유저의 정보를 조회할 수 없습니다.",
  "requestPath": "uri=/api/user/1/shop-profile",
  "pathToMove": "/shop/main/index"
}

```