package policy;

import java.time.Duration;

public class ExponentialWaitingPolicy extends WaitingPolicy {

    private int qualifier = 1;

    public ExponentialWaitingPolicy(Duration duration) {
        super(duration);
    }

    @Override
    public long toMillis() {
        long millis = super.toMillis() * qualifier;
        qualifier++;
        return millis;
    }
}
