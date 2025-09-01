package com.radarqr.dating.android.utility.chipslayoutmanager.layouter;

import android.view.View;

import com.radarqr.dating.android.utility.chipslayoutmanager.IScrollingController;
import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.AnchorViewState;
import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.IAnchorFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria.AbstractCriteriaFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria.ICriteriaFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.placer.IPlacerFactory;


public interface IStateFactory {
    @SuppressWarnings("UnnecessaryLocalVariable")
    LayouterFactory createLayouterFactory(ICriteriaFactory criteriaFactory, IPlacerFactory placerFactory);

    AbstractCriteriaFactory createDefaultFinishingCriteriaFactory();

    IAnchorFactory anchorFactory();

    IScrollingController scrollingController();

    ICanvas createCanvas();

    int getSizeMode();

    int getStart();

    int getStart(View view);

    int getStart(AnchorViewState anchor);

    int getStartAfterPadding();

    int getStartViewPosition();

    int getStartViewBound();

    int getEnd();

    int getEnd(View view);

    int getEndAfterPadding();

    int getEnd(AnchorViewState anchor);

    int getEndViewPosition();

    int getEndViewBound();

    int getTotalSpace();
}
