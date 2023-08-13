# Homework application

Implement the aggregation API, batching the requests (actually keys) to downstream APIs. 
Each of the downstream APIs has its own queue of items with dedicated worker, which will schedule batch API querying and delivering results.

Requests on the Aggregation API concurrently put items ond dedicated queues. Incoming request threads on Aggregation API are put on wait, until results are asynchronously available.
Each API key submitted for processing would have its own `CompletableFuture`, containing either the result upon completion.
On error of downstream API call, Aggregation API will return null values for keys in question.

Application is built and tested using Amazon Corretto-17.0.8.7.1

To build and run the app:
- `mvn clean install`
- `java -jar target/homework-0.0.1-SNAPSHOT.jar`

App will listen on `8081`, ready to be tested.

An integration test using is also available for testing, however a backing Docker image of downstream services is required.
- `mvn test -DintegrationTest=true`
