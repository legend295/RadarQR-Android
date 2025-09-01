package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

/** this is basis row breaker for {@link com.radarqr.dating.android.utility.chipslayoutmanager.layouter.RTLDownLayouter} */
class RTLForwardRowBreaker implements ILayoutRowBreaker {

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return al.getViewRight() < al.getCanvasRightBorder()
                && al.getViewRight() - al.getCurrentViewWidth() < al.getCanvasLeftBorder();

    }
}
