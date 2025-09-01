package com.radarqr.dating.android.utility.chipslayoutmanager.layouter;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.AnchorViewState;

interface ILayouterCreator {
    //---- up layouter below
    Rect createOffsetRectForBackwardLayouter(@NonNull AnchorViewState anchorRect);

    AbstractLayouter.Builder createBackwardBuilder();

    AbstractLayouter.Builder createForwardBuilder();

    Rect createOffsetRectForForwardLayouter(AnchorViewState anchorRect);
}
