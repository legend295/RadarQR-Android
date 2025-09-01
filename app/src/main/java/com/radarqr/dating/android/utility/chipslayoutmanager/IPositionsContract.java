package com.radarqr.dating.android.utility.chipslayoutmanager;

interface IPositionsContract {
    int findFirstVisibleItemPosition();
    int findFirstCompletelyVisibleItemPosition();
    int findLastVisibleItemPosition();
    int findLastCompletelyVisibleItemPosition();
}
