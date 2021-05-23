import config.RetryConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import policy.ExactWaitingPolicy;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public class AgainTests {

    int retryCount = 0;

    @Test
    public void whenRetry_thenReturnExpectedValue() {
        // given
        RetryConfig retryConfig = new RetryConfig.RetryConfigBuilder()
                .withRetryCount(3)
                .withPolicy(new ExactWaitingPolicy(Duration.ofMillis(100)))
                .build();

        Supplier<Integer> operation = () -> 100;

        // when
        Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .retry();

        // then
        Assertions.assertEquals(maybeResult.get(), 100);
    }

    @Test
    public void whenRetryWithError_thenReturnEmptyValue() {
        // given
        RetryConfig retryConfig = new RetryConfig.RetryConfigBuilder()
                .withRetryCount(3)
                .withPolicy(new ExactWaitingPolicy(Duration.ofMillis(100)))
                .build();

        Supplier<Integer> operation = () -> {
            throw new RuntimeException("Error");
        };

        // when
        Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .retry();

        // then
        Assertions.assertFalse(maybeResult.isPresent());
    }

    @Test
    public void whenRetryWithSpecifiedException_thenReturnEmptyValue() {
        // given
        RetryConfig retryConfig = new RetryConfig.RetryConfigBuilder()
                .withRetryCount(3)
                .withPolicy(new ExactWaitingPolicy(Duration.ofMillis(100)))
                .build();

        Supplier<Integer> operation = () -> {
            throw new RuntimeException("Error");
        };

        // when
        Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .retry(RuntimeException.class);

        // then
        Assertions.assertFalse(maybeResult.isPresent());
    }

    @Test
    public void whenRetryWithError_thenShouldRetryAsCount() {
        // given
        RetryConfig retryConfig = new RetryConfig.RetryConfigBuilder()
                .withRetryCount(3)
                .withPolicy(new ExactWaitingPolicy(Duration.ofMillis(100)))
                .build();

        Supplier<Integer> operation = () -> {
            if (retryCount < 2) {
                retryCount++;
                throw new RuntimeException("Error");
            }
            return 100;
        };

        // when
        Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .retry(RuntimeException.class);

        // then
        Assertions.assertEquals(maybeResult.get(), 100);
    }

    @Test
    public void whenRetryWithCondition_thenShouldRetryAsCount() {
        // given
        RetryConfig retryConfig = new RetryConfig.RetryConfigBuilder()
                .withRetryCount(3)
                .withPolicy(new ExactWaitingPolicy(Duration.ofMillis(100)))
                .build();

        Supplier<Integer> operation = () -> {
            if (retryCount < 2) {
                retryCount++;
                return 500;
            }
            return 100;
        };

        // when
        Optional<Integer> maybeResult = Again.of(retryConfig, operation)
                .condition(__ -> __ == 500)
                .retry();

        // then
        Assertions.assertEquals(maybeResult.get(), 100);
    }
}
