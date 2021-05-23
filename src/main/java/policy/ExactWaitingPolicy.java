package policy;

import java.time.Duration;

public class ExactWaitingPolicy extends WaitingPolicy {

    public ExactWaitingPolicy(Duration duration) {
        super(duration);
    }

    @Override
    public long toMillis() {
        return super.toMillis();
    }


}
