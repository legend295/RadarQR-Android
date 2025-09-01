package com.radarqr.dating.android.utility.chipslayoutmanager.util.log;

import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.AnchorViewState;

public interface IFillLogger {

    void onStartLayouter(int startPosition);

    void onItemRequested();

    void onItemRecycled();

    void onFinishedLayouter();

    void onAfterLayouter();

    void onRemovedAndRecycled(int position);

    void onAfterRemovingViews();

    void onBeforeLayouter(AnchorViewState state);
}

