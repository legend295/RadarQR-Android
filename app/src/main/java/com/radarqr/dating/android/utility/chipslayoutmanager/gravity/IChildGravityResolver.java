package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import com.radarqr.dating.android.utility.chipslayoutmanager.SpanLayoutChildGravity;

/** class which determines child gravity inside row from child position */
public interface IChildGravityResolver {
    @SpanLayoutChildGravity
    int getItemGravity(int position);
}
