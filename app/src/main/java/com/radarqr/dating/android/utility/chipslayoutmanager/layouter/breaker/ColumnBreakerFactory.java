package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker;

public class ColumnBreakerFactory implements IBreakerFactory {
    @Override
    public ILayoutRowBreaker createBackwardRowBreaker() {
        return new LTRBackwardColumnBreaker();
    }

    @Override
    public ILayoutRowBreaker createForwardRowBreaker() {
        return new LTRForwardColumnBreaker();
    }
}
