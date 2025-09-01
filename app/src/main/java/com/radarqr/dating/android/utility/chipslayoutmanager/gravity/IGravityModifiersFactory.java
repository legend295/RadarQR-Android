package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import com.radarqr.dating.android.utility.chipslayoutmanager.SpanLayoutChildGravity;

public interface IGravityModifiersFactory {
    IGravityModifier getGravityModifier(@SpanLayoutChildGravity int gravity);
}
