package com.radarqr.dating.android.utility.chipslayoutmanager.layouter;


import androidx.recyclerview.widget.RecyclerView;

import com.radarqr.dating.android.utility.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.gravity.LTRRowStrategyFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker.LTRRowBreakerFactory;

class LTRRowsOrientationStateFactory implements IOrientationStateFactory {

    @Override
    public ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm) {
        return new LTRRowsCreator(lm);
    }

    @Override
    public IRowStrategyFactory createRowStrategyFactory() {
        return new LTRRowStrategyFactory();
    }

    @Override
    public IBreakerFactory createDefaultBreaker() {
        return new LTRRowBreakerFactory();
    }
}
