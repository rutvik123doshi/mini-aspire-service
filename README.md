# Mini Aspire Service

## About
Backend service built using java springboot and postgres as the database.

### Auth
In order to do auth, there are two tables- users and user_auth.
First, user needs to generate otp. The otp currently will be shared in api reponse itself. However, for prod, 
it can be sent to the user through comms channel.<br />
Then user logins by entering the phone number and otp. The user will get auth token in response<br />

Use this auth token for all api requests by passing in with header key: `X-Auth-Token` <br />

If user generates multiple otps, older otp will be marked invalid.

Default expiration time of token is `1000 minutes` - configuration in app properties. <br />

### Loan creation
Use auth token to create, view a loan and view all loans.
The initial status of loan will be `CREATED`

### Admin control
NOTE: even admin needs to generate a token for update loan status

Also, admins need another internalApiToken which is present in app properties in manager module

The admin can move the status of `CREATED` loans to anything.<br />
It is assumed that a separate cron will be written that will expire the loans if not approved within 7 days.


Loan status will be `APPROVED` for success case <br />
Loan repayment schedules will be then created in `loan_repayments` table with all repayments as `UNPAID`

### Loan repay

User needs to add a repayment amount up to two decimal places along with a payment ref id.

Repayment can only be done for approved loans.

If repayment is greater than remaining amt, an error is thrown.

Any repayment amount is allowed.

Cases for 100/3 amt of installment is also handled.

No date checks are added currently. However, there is a `scheduled_at` field that can be used for this in the future.


## Swagger link
http://localhost:9003/mini-aspire-service/swagger-ui.html

## Installation

1. Download and run postgres in local: `brew install postgresql@15`
2. Create a database in local with name: `mini_aspire`
3. Run this cmd on public schema of this db: <br />
``GRANT CREATE, USAGE ON SCHEMA public TO admin;``
4. Download maven (optional)
5. Run:
``
   ./mvnw clean install -DskipTests
``
<br /> OR <br />
``
mvn clean install -DskipTests
``
6. Run migration for setting up db: <br />``java -jar manager/target/database-0.0.1-SNAPSHOT.jar``
<br /> OR <br /> RUN `DatabaseMain.java` in intellij
7. Start service:<br /> ``java -jar manager/target/manager-0.0.1-SNAPSHOT.jar``
<br /> OR RUN `MiniAspireServiceMain.java` in intellij


## Sample API curls
Postman collection:<br /> 
auth module: https://api.postman.com/collections/8098009-1bc5c8f8-1af0-4208-882d-84ff422b5546?access_key=PMAT-01HB69Y5HD03D26Z581PWD7Y7H
loans module: https://api.postman.com/collections/8098009-19b1a4de-2098-4d63-9a6e-cb68a5157bef?access_key=PMAT-01HB69ZBSWPHB0ZJWAT0MHPE0N

ping api:
```
curl --location 'localhost:9003/mini-aspire-service/ping' \
--header 'X-Auth-Token: 123' \
--header 'Content-Type: application/json'
```

1. generate otp
```
curl --location 'localhost:9003/mini-aspire-service/authentication/generate-otp' \
--header 'Content-Type: application/json' \
--data '{
    "phoneNumber" : "1230123000"
}'
```
2. login:
use otp from generate otp response
```
curl --location 'localhost:9003/mini-aspire-service/authentication/login' \
--header 'Content-Type: application/json' \
--data '{
    "phoneNumber" : "1230123000",
    "otp": 574360
}'
```

NOTE: ENSURE TO UPDATE X-Auth-Token in headers 
and loan id in params/ json body

3. create loan
```
curl --location 'localhost:9003/mini-aspire-service/loans/' \
--header 'X-Auth-Token: 25c2f8c1-3418-4856-b44a-b68b478a8dd4' \
--header 'Content-Type: application/json' \
--data '{
    "totalTerm": 3,
    "amount": 100
}'
```
4. authorize / approve loan
```
curl --location --request PATCH 'localhost:9003/mini-aspire-service/admin/loans/7a2b70c5-dc7f-41f6-a86d-6ebe173f1947' \
--header 'X-Auth-Token: 25c2f8c1-3418-4856-b44a-b68b478a8dd4' \
--header 'Content-Type: application/json' \
--header 'internalApiToken: test-rutvik' \
--data '{
    "status": "APPROVED"
}'
```
5. repay loan
```
curl --location 'localhost:9003/mini-aspire-service/loans/7a2b70c5-dc7f-41f6-a86d-6ebe173f1947/repay' \
--header 'X-Auth-Token: 25c2f8c1-3418-4856-b44a-b68b478a8dd4' \
--header 'Content-Type: application/json' \
--data '{
    "repaymentAmount": 33.33,
    "paymentReferenceId": "50056"
}'
```
6. Get all loans
```
curl --location 'localhost:9003/mini-aspire-service/loans/' \
--header 'X-Auth-Token: db007d35-4aab-48d5-8502-b68df50d43ea' \
--header 'Content-Type: application/json'
```
7. Get a particular loan
```
curl --location 'localhost:9003/mini-aspire-service/loans/7a2b70c5-dc7f-41f6-a86d-6ebe173f1947' \
--header 'X-Auth-Token: 25c2f8c1-3418-4856-b44a-b68b478a8dd4' \
--header 'Content-Type: application/json'
```
8. logout
```
curl --location 'localhost:9003/mini-aspire-service/authentication/logout' \
--header 'X-Auth-Token: 8186356d-677d-4a7a-8778-e2592a6834af' \
--header 'Content-Type: application/json' \
--data '{
    "phoneNumber" : "9009123123",
    "otp": 385569
}'
```

