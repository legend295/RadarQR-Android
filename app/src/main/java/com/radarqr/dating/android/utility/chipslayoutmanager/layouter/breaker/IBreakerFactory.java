package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker;

public interface IBreakerFactory {
    ILayoutRowBreaker createBackwardRowBreaker();

    ILayoutRowBreaker createForwardRowBreaker();
}
