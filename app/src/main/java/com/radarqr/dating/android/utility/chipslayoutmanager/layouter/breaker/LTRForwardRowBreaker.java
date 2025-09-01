package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

/** this is basis row breaker for {@link com.radarqr.dating.android.utility.chipslayoutmanager.layouter.LTRDownLayouter} */
class LTRForwardRowBreaker implements ILayoutRowBreaker {

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return al.getViewLeft() > al.getCanvasLeftBorder()
                && al.getViewLeft() + al.getCurrentViewWidth() > al.getCanvasRightBorder();
    }
}
