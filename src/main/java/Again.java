import config.RetryConfig;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Again<T> {

    private final RetryConfig retryConfig;
    private final Supplier<T> operation;
    private Consumer<Throwable> onEachRetry;

    private Again(RetryConfig retryConfig, Supplier<T> operation) {
        this.retryConfig = retryConfig;
        this.operation = operation;
    }

    public static <A> Again<A> of(RetryConfig retryConfig, Supplier<A> operation) {
        return new Again<>(retryConfig, operation);
    }

    @SafeVarargs
    public final Optional<T> retry(Class<? extends Throwable>... exceptions) {
        Retryable<T> retryable = new Retryable<>(this.retryConfig, this.operation, this.onEachRetry);
        return retryable.retry(exceptions);
    }

    public static final class Retryable<T> {

        private final RetryConfig retryConfig;
        private final Supplier<T> operation;
        private final Consumer<Throwable> onEachRetry;
        private Predicate<T> condition;

        private Retryable(RetryConfig retryConfig, Supplier<T> operation, Consumer<Throwable> onEachRetry) {
            this.retryConfig = retryConfig;
            this.operation = operation;
            this.onEachRetry = onEachRetry;
        }

        private Retryable(RetryConfig retryConfig, Supplier<T> operation, Predicate<T> condition, Consumer<Throwable> onEachRetry) {
            this.retryConfig = retryConfig;
            this.operation = operation;
            this.condition = condition;
            this.onEachRetry = onEachRetry;
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
                        doOnEachRetry(new RuntimeException("Condition failed"));
                    } else {
                        return Optional.ofNullable(result);
                    }

                } catch (Exception ex) {
                    if (exceptions.length != 0) {
                        boolean anyMatch = Arrays.stream(exceptions)
                                .anyMatch(__ -> __.equals(ex.getClass()));
                        if (anyMatch) {
                            currentRetry++;
                            doOnEachRetry(ex);
                        } else {
                            return Optional.empty();
                        }
                    } else {
                        currentRetry++;
                        doOnEachRetry(ex);
                    }
                }
                try {
                    Thread.sleep(retryConfig.toMillis());
                } catch (InterruptedException e) {
                    doOnEachRetry(e);
                }
            }

            return Optional.empty();
        }

        private void doOnEachRetry(Throwable throwable) {
            if (this.onEachRetry != null) {
                this.onEachRetry.accept(throwable);
            }
        }
    }

    public final Retryable<T> withCondition(Predicate<T> condition) {
        return new Retryable<>(this.retryConfig, this.operation, condition, this.onEachRetry);
    }

    public final Again<T> doOnEachRetry(Consumer<Throwable> onEachRetry) {
        this.onEachRetry = onEachRetry;
        return this;
    }
}
