package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import com.radarqr.dating.android.utility.chipslayoutmanager.RowStrategy;

public interface IRowStrategyFactory {
    IRowStrategy createRowStrategy(@RowStrategy int rowStrategy);
}
