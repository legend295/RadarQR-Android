package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker;

public class LTRRowBreakerFactory implements IBreakerFactory {
    @Override
    public ILayoutRowBreaker createBackwardRowBreaker() {
        return new LTRBackwardRowBreaker();
    }

    @Override
    public ILayoutRowBreaker createForwardRowBreaker() {
        return new LTRForwardRowBreaker();
    }
}
