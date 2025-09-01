package com.radarqr.dating.android.utility.chipslayoutmanager.layouter;

import androidx.recyclerview.widget.RecyclerView;

import com.radarqr.dating.android.utility.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.gravity.RTLRowStrategyFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker.RTLRowBreakerFactory;

class RTLRowsOrientationStateFactory implements IOrientationStateFactory {

    @Override
    public ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm) {
        return new RTLRowsCreator(lm);
    }

    @Override
    public IRowStrategyFactory createRowStrategyFactory() {
        return new RTLRowStrategyFactory();
    }

    @Override
    public IBreakerFactory createDefaultBreaker() {
        return new RTLRowBreakerFactory();
    }
}
