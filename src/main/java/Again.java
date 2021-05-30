import config.RetryConfig;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Again<T> {

    private final RetryConfig retryConfig;
    private final Supplier<T> operation;

    private Again(RetryConfig retryConfig, Supplier<T> operation) {
        this.retryConfig = retryConfig;
        this.operation = operation;
    }

    public static <A> Again<A> of(RetryConfig retryConfig, Supplier<A> operation) {
        return new Again<>(retryConfig, operation);
    }

    @SafeVarargs
    public final Optional<T> retry(Class<? extends Throwable>... exceptions) {
        Retryable<T> retryable = new Retryable<>(this.retryConfig, this.operation);
        return retryable.retry(exceptions);
    }

    public static class Retryable<T> {

        private final RetryConfig retryConfig;
        private final Supplier<T> operation;
        private Predicate<T> condition;

        private Retryable(RetryConfig retryConfig, Supplier<T> operation) {
            this.retryConfig = retryConfig;
            this.operation = operation;
        }

        private Retryable(RetryConfig retryConfig, Supplier<T> operation, Predicate<T> condition) {
            this.retryConfig = retryConfig;
            this.operation = operation;
            this.condition = condition;
        }

        @SafeVarargs
        public final Optional<T> retry(Class<? extends Throwable>... exceptions) {
            int maxRetry = this.retryConfig.retryCount();
            int currentRetry = 0;

            while (currentRetry < maxRetry) {
                try {
                    T result = operation.get();
                    if (condition != null && condition.test(result)) {
                        currentRetry++;
                    } else {
                        return Optional.ofNullable(result);
                    }

                } catch (Exception ex) {
                    if (exceptions.length != 0) {
                        boolean anyMatch = Arrays.stream(exceptions)
                                .anyMatch(__ -> __.equals(ex.getClass()));
                        if (anyMatch) {
                            currentRetry++;
                        } else {
                            return Optional.empty();
                        }
                    } else {
                        currentRetry++;
                    }
                }
                try {
                    Thread.sleep(retryConfig.toMillis());
                } catch (InterruptedException e) {

                }
            }

            return Optional.empty();
        }
    }

    public Retryable<T> withCondition(Predicate<T> condition) {
        return new Retryable<>(this.retryConfig, this.operation, condition);
    }
}
