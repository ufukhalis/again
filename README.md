# AGAIN

`Again` is a simple Java retry library which only needs Java 8+ version.

## How to use

Firstly, you should add latest `Again` dependency to your project.

```$xslt
<dependency>
    <groupId>io.github.ufukhalis</groupId>
    <artifactId>again</artifactId>
    <version>0.0.3</version>
</dependency>
```

Then you need to make configuration for your retry like below

```java
RetryConfig retryConfig = new RetryConfig.RetryConfigBuilder()
                .withRetryCount(3)
                .withPolicy(new ExactWaitingPolicy(Duration.ofMillis(100)))
                .build();
```

You can choose also `Exponential` policy.

```java
new ExponentialWaitingPolicy((Duration.ofMillis(100))
```

After that you can create your retry operation like below.

```java
Supplier<Integer> operation = () -> 100;

Optional<Integer> maybeResult = Again.of(retryConfig, operation)
        .retry();

```

With above configuration, it will be retried based on any exception from calling your `operation` method.
So, you can also pass specific exceptions to `retry` method like below.

```java
Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .retry(RuntimeException.class);
```

In that case, it will be retried for only specified exceptions otherwise that it won't be retried.

Besides, the exception based retry that you can also use the conditions like below.

```java
Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .withCondition(__ -> __ == 500)
                .doOnEachRetry(t -> log.info(t))
                .retry();
```

In that case, it will be retried also if value is `500` like defined above.

License
------------
All code in this repository is licensed under the Apache License, Version 2.0. See [LICENCE](./LICENSE).
