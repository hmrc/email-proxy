# email-proxy

==========


All the calls to email-proxy are forwarded to the email microservice.

[![Build Status](https://travis-ci.org/hmrc/email-proxy.svg)](https://travis-ci.org/hmrc/email-proxy) [ ![Download](https://api.bintray.com/packages/hmrc/releases/email-proxy/images/download.svg) ](https://bintray.com/hmrc/releases/email-proxy/_latestVersion)


# API

| Method | Path                               | Description                                                                |
|--------|------------------------------------|----------------------------------------------------------------------------|
| POST   | ```email-proxy/<domain>/email``` | Sends request to email m/s |

## ```POST email-proxy/<domain>/email```

Sends a request to the email m/s via the email-proxy

### Request format

See email microservice for json body format.

_Note: the email-proxy will convert the body to json but will do no other validation, all other errors will 
be supplied by the connected email m/s._ 


### Response

Response of 2xx should be treated as ok all others as an exception 

| Status | Message                                                                           |
|--------|-----------------------------------------------------------------------------------|
| 202    | Success - request was submitted to email to send an email               |
| 400    | Invalid json format or rejected by email  m/s |
| 404    | If endpoint is not reachable |
| 502    | Could not contact email m/s                                                            |

**Response body**

Only returned in the case of an exception response (status code >= 400)

```json
{
    "statusCode": 400,
    "message": "Template NNN does not exist"
}
```



### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
