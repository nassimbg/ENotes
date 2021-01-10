# ENotes

A simple API that provides the user with the functionality of authentication and then creating, reading and deleting notes.
## Getting Started

### How it Works
The user should sign up to get his access token through calling the Authentication API
and then he will be able to call the Notes API with the Authorization HTTP header of type bearer
to send the JWT access token to the service for Authorization purposes


### Launching Service

Launch using spring maven plugin
```
mvn spring-boot:run
```

< host > = localhost; < port > = 8080

### Calling Authentication API

#### SignUp a user
This API is used to sign up a user given his credentials and returns two tokens, the first is an access token 
signed by a private key so that other services with the public key can verify this token
and the second is a refresh token signed by a secret key so that only the Authentication service can verify this token

##### Request

```
POST http://<host>:<port>/v1/authentication/signup
``` 

Example
```
POST http://localhost:8080/v1/authentication/signup
```
Request Body
```
{
    "userName": <userNameValue>, 
    "password": <passwordValue>
}
```

##### Response

Response Body
```
{
    "accessToken": <generatedAccessToken>,
    "refreshToken": <generatedRefreshToken>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 409       | The user you want to sign up to already exists |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---

#### SignIn a user
This API is used to sign in a user given his credentials and returns two tokens, the first is an access token
signed by a private key so that other services with the public key can verify this token
and the second is a refresh token signed by a secret key so that only the Authentication service can verify this token

##### Request

```
POST http://<host>:<port>/v1/authentication/signin
``` 

Request Body
```
{
    "userName": <userNameValue>, 
    "password": <passwordValue>
}
```

##### Response

Response Body
```
{
    "accessToken": <generatedAccessToken>,
    "refreshToken": <generatedRefreshToken>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 400       | The username or password is incorrect |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---

#### SignOut a user
This API is used to sign out a user given his tokens and returns the user's logout status

##### Request

```
POST http://<host>:<port>/v1/authentication/signout
``` 

Request Body
```
{
    "accessToken": <generatedAccessToken>,
    "refreshToken": <generatedRefreshToken>
}
```

##### Response

Response Body
```
{
    "userName": <userNameValue>,
    "logOut": <booleanValue>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 400       | Either the object you requested does not exist or the provided Refresh Token is either expired or has been revoked |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |



---

#### refresh Access token
This API is used to refresh the access token for a user given his tokens and returns an access token but same refresh token

##### Request

```
POST http://<host>:<port>/v1/authentication/token
``` 

Request Body
```
{
    "accessToken": <generatedAccessToken>,
    "refreshToken": <generatedRefreshToken>
}
```

##### Response

Response Body
```
{
    "accessToken": <generatedAccessToken>,
    "refreshToken": <generatedRefreshToken>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 400       | Either the object you requested does not exist or the provided Refresh Token is either expired or has been revoked |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---

---


### Calling Notes API

#### Creating a note
This API is used to create a note for a specific user based on his access token that is found
in the Authorization header in the request

##### Request

```
POST http://<host>:<port>/v1/notes
``` 

Request Body
```
{
    "title": <titleValue>, 
    "body": <bodyValue>
}
```

##### Response

Response Body
```
{
    "id": <noteId>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 403       | The user is not authorized to undergo this request |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---

#### Getting a note
This API is used to get a note given its note id for a specific user based on his access token that is found
in the Authorization header in the request

##### Request

```
GET http://<host>:<port>/v1/notes/{noteId}
``` 

Request Header
```
{
    Authorization : Brearer <accessToken>
}
```

##### Response

Response Body
```
{
    "id": <noteId>,
    "title": <titleValue>, 
    "body": <bodyValue>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 400       | This note id is not valid or note not found for the user |
| 403       | The user is not authorized to undergo this request |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---

#### Getting all notes for a user
This API is used to get all notes for a specific user based on his access token that is found
in the Authorization header in the request

##### Request

```
GET http://<host>:<port>/v1/notes
``` 

Request Header
```
{
    Authorization : Brearer <accessToken>
}
```

##### Response

Response Body
```
[
    {
        "id": <noteId>,
        "title": <titleValue>, 
        "body": <bodyValue>
    },
    
    {
        "id": <noteId>,
        "title": <titleValue>, 
        "body": <bodyValue>
    }
    ...
]
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  200      | The request was successful. The response will contain a JSON body.   | 
| 403       | The user is not authorized to undergo this request |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---

#### Delete a note
This API is used to delete a note given its note id for a specific user based on his access token that is found
in the Authorization header in the request

##### Request

```
DELETE http://<host>:<port>/v1/notes/{noteId}
``` 

Request Header
```
{
    Authorization : Brearer <accessToken>
}
```

Response Code

| Code      | Description  | 
| :-------- | :----------: | 
|  204      | The request was successful.  | 
| 400       | This note id is not valid or note not found for the user |
| 403       | The user is not authorized to undergo this request |
| 500       | There was an internal error. A stack trace is provided and logged based on log4j configuration. The response will be empty |


---
