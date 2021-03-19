## Log-in API 

`Description` : 로그인 요청

___

### Log-in API  (Authorization-server)

**URL** : `/auth/login`

**Method** : `POST`

**Authentication required** : `No`

**Data** : 

{

  "webMail" : "웹메일@dongguk.edu",

  "password" : 비밀번호

 }

## Success Responses

___

**Code** : `200 OK`

**Content**

`message`: 응답 메시지 

`status`: 응답 상태 

`data`:  null

**example**

```json
{
    "message": "로그인 성공",
    "status": 200,
    "data": null
}
```



