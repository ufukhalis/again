package config;

import policy.WaitingPolicy;

public class RetryConfig {

    private final int retryCount;
    private final WaitingPolicy policy;

    public int retryCount() {
        return retryCount;
    }

    public long toMillis() {
        return policy.toMillis();
    }

    private RetryConfig(int retryCount, WaitingPolicy policy) {
        this.retryCount = retryCount;
        this.policy = policy;
    }

    public static class RetryConfigBuilder {
        private int retryCount;
        private WaitingPolicy policy;

        public RetryConfigBuilder withRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public RetryConfigBuilder withPolicy(WaitingPolicy policy) {
            this.policy = policy;
            return this;
        }

        public RetryConfig build() {
            return new RetryConfig(this.retryCount, this.policy);
        }
    }
}
