package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.placer;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


class DisappearingViewAtStartPlacer extends AbstractPlacer {

    DisappearingViewAtStartPlacer(RecyclerView.LayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void addView(View view) {
        getLayoutManager().addDisappearingView(view, 0);

//        Log.i("added disappearing view, position = " + getLayoutManager().getPosition(view));
//        Log.d("name = " + ((TextView)view.findViewById(R.id.tvName)).getText().toString());
    }
}
