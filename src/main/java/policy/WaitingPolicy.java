package policy;

import java.time.Duration;

public abstract class WaitingPolicy {

    private Duration duration;

    public WaitingPolicy(Duration duration) {
        this.duration = duration;
    }

    public long toMillis() {
        return duration.toMillis();
    };
}
