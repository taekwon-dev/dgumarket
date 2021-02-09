## Silent Refresh API : Get Access Token via the refresh token 

`Description` : 로그인 이후 클라이언트에 HTTP Cookie header with HttpOnly로 Refresh Token을 반환합니다. 로그인 시점에 Access Token을 반환하지 않고, 로그인 이후 이동하는 페이지에서 Refresh Token을 활용해서 Access Token을 얻는 구조입니다. (로그인 후 이동하는 페이지는 index 또는 마지막으로 위치했던 페이지가 될 수 있습니다.) 

___

### Silent refresh API (Gateway) 

**URL** : `/auth/refresh` ex: http://localhost:8081/auth/refresh

**Method** : `POST`

**Authentication required** : `Yes`

**Data** : `None` 

## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지 

`status`: 응답 상태 

`data`:  

​		`tokenType` : Token 타입, Authorization Header에 활용 (자세한 건 다른 API 요청에서 확인)

​	    `accessToken` : Access Token 값

​		`userId` : 유저 고유 ID

​		`userNickName` : 유저 닉네임

**example**

```json
{
    "message": "JWT 토큰 갱신 성공",
    "status": 200,
    "data": {
        "tokenType": "Bearer",
        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0YWVrd29uQGRvbmdndWsuZWR1IiwiaWF0IjoxNjEyNzcyMTQwLCJleHAiOjE2MTI3NzIyMDB9.g-26F0oEnlaVdX3ER9JUAwi7qN80ifKBJjrqKGiovKbVzBTSHFgyjgkZX0zeyLTaEVFALE-U7hHNY2Wj-MM0JQ",
        "userId": 1,
        "userNickName": "윤태권"
    }
}
```




