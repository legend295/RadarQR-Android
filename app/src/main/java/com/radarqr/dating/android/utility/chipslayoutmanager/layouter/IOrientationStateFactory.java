package com.radarqr.dating.android.utility.chipslayoutmanager.layouter;


import androidx.recyclerview.widget.RecyclerView;

import com.radarqr.dating.android.utility.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker.IBreakerFactory;

interface IOrientationStateFactory {
    ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm);
    IRowStrategyFactory createRowStrategyFactory();
    IBreakerFactory createDefaultBreaker();
}
