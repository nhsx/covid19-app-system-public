# Virology Testing API V1 (deprecated) and V2

API group: [Submission](../guidebook.md#system-apis-and-interfaces)

## HTTP request and response

### V1 (deprecated)

Order test via the app:
- Order a home kit test: ```POST https://<FQDN>/virology-test/home-kit/order```
- Register home kit test (alias for order): ```POST https://<FQDN>/virology-test/home-kit/register``` 

Retrieve the result of a test ordered via the app:
- Poll for test results: ```POST https://<FQDN>/virology-test/results```
- Theoretically, LFD test results could also be returned to old app versions (old app versions can not distinguish between PCR and LFD results)
  
Retrieve the result for a cta token that was not generated by the mobile application:
- Return of the test result from in exchange for a cta token: ```POST https://<FQDN>/virology-test/cta-exchange```
- PCR-based and LFD-based test results (depending on the current policy) could be returned to old app versions (old app versions can not distinguish between PCR and LFD results)

### V2

Order test via the app:
- Order a home kit test: ```POST https://<FQDN>/virology-test/v2/order```

Retrieve the result of a test ordered via the app:
- Poll for test results: ```POST https://<FQDN>/virology-test/v2/results```
- Theoretically, LFD test results could also be returned
  
Retrieve the result for a cta token that was not generated by the mobile application:
- Return of the test result from in exchange for a cta token: ```POST https://<FQDN>/virology-test/v2/cta-exchange```
- PCR-based and LFD-based test results will be returned

### Parameters

- Authorization required and signatures provided - see [API security](./security.md)
- Payload content-type: ```application/json```

## Scenarios

The Virology Testing API provides HTTP endpoints to retrieve URLs and tokens for the interaction with the virology testing website and to enable retrieving test results later.

Virology tokens follow the [Crockford and Damm](../../design/details/crockford-damm.md) algorithm.

Please note: testResult INDETERMINATE reported by Fiorano will be delivered as VOID to the mobile application - the behaviour could change in the future.

### Example (V1 and V2 API): Ordering a home kit test / registering a home kit test

```POST https://<FQDN>/virology-test/home-kit/order``` (deprecated)

```POST https://<FQDN>/virology-test/home-kit/register``` (deprecated)

```POST https://<FQDN>/virology-test/v2/order```

Request body: empty

Response body:
```json
{
    "websiteUrlWithQuery": "https://self-referral.test-for-coronavirus.service.gov.uk/cta-start?ctaToken=tbdfjaj0",
    "tokenParameterValue": "tbdfjaj0",
    "testResultPollingToken" : "61EEFD4B-E903-4294-B595-B1D491134E3D",
    "diagnosisKeySubmissionToken": "6B162698-ADC5-47AF-8790-71ACF770FFAF" 
}
```
Notes: `tokenParameterValue` is only to be displayed in the app. See bellow for more info about the `testResultPollingToken` and `diagnosisKeySubmissionToken` fields. 

### Example (V1 API): Poll for test result using the token from the ordering requests

```POST https://<FQDN>/virology-test/results``` (deprecated)

Request body:
```json
{
    "testResultPollingToken": "61EEFD4B-E903-4294-B595-B1D491134E3D"
}
```

Response body:
```json
{
    "testEndDate": "2020-04-23T00:00:00Z",
    "testResult": "POSITIVE"|"NEGATIVE"|"VOID",
}
```

### Example (V2 API): Poll for test result using the token from the ordering requests

```POST https://<FQDN>/virology-test/v2/results```

Request body:
```json
{
    "testResultPollingToken": "61EEFD4B-E903-4294-B595-B1D491134E3D",
    "country": "England"|"Wales"
}
```

Response body:
```json
{
    "testEndDate": "2020-04-23T00:00:00Z",
    "testResult": "POSITIVE"|"NEGATIVE"|"VOID",
    "testKit": "LAB_RESULT"|"RAPID_RESULT"|"RAPID_SELF_REPORTED",
    "diagnosisKeySubmissionSupported": true|false,
    "requiresConfirmatoryTest": true|false
}
```
Notes: 
- `requiresConfirmatoryTest` indicates whether the user should be taken through the confirmatory test journey or not.

### Example (V1 API): Obtaining a test result in exchange for a cta token that was not generated by the mobile application

```POST https://<FQDN>/virology-test/cta-exchange``` (deprecated)

- Security: This endpoint will be made slow (response time) by design to avoid brute-force attacks.

Request body:
```json
{
    "ctaToken": "1234abcd"
}
```

Response body:
```json
{
    "diagnosisKeySubmissionToken": "6B162698-ADC5-47AF-8790-71ACF770FFAF",
    "testEndDate": "2020-04-23T00:00:00Z",
    "testResult": "POSITIVE"|"NEGATIVE"|"VOID"
}
```

Notes: 
- See [diagnosis-key-submission.md](diagnosis-key-submission.md) for more info about the `diagnosisKeySubmissionToken` field.

### Example (V2 API): Obtaining a test result in exchange for a cta token that was not generated by the mobile application

```POST https://<FQDN>/virology-test/v2/cta-exchange```

- Security: This endpoint will be made slow (response time) by design to avoid brute-force attacks.

Request body:
```json
{
    "ctaToken": "1234abcd",
    "country": "England"|"Wales"
}
```

Response body:
```json
{
    "diagnosisKeySubmissionToken": "6B162698-ADC5-47AF-8790-71ACF770FFAF"|null,
    "testEndDate": "2020-04-23T00:00:00Z",
    "testResult": "POSITIVE"|"NEGATIVE"|"VOID",
    "testKit": "LAB_RESULT"|"RAPID_RESULT"|"RAPID_SELF_REPORTED",
    "diagnosisKeySubmissionSupported": true|false,
    "requiresConfirmatoryTest": true|false
}
```

Notes: 
- `diagnosisKeySubmissionToken` can be `null`, if `diagnosisKeySubmissionSupported` is `false`
- `requiresConfirmatoryTest` indicates whether the user should be taken through the confirmatory test journey or not.
- See [diagnosis-key-submission.md](diagnosis-key-submission.md) for more info about the `diagnosisKeySubmissionToken` field.

### Response Codes

- Order a home kit test / Register home kit test
  - `HTTP 200` ok
  - `HTTP 500` internal server error
- Poll for test results
  - `HTTP 200` ok
  - `HTTP 204` no result yet
  - `HTTP 404` polling token not found (also for V1: polling token refers to a LFD test result - depending on the current policy)
  - `HTTP 422` invalid json request body
  - `HTTP 500` internal server error
- Exchange cta token
  - `HTTP 200` ok
  - `HTTP 404` cta token not found (also  for V1: ctaToken refers to a LFD test result - depending on the current policy)

### Maintenance Mode

When the service is in maintenance mode, all services will return 503. Maintenance mode is used when performing operations like backup and restore of the service data.

## Notes and Links

- Confluence: Search Virology
- [Conceptual system flow](../../architecture/drafts/ag-architecture-guidebook-v4.md#system-flow-request-virology-testing-and-get-result-using-a-temporary-token)
