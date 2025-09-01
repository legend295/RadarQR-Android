package com.radarqr.dating.android.ui.home.main.recommended

import androidx.recyclerview.widget.DiffUtil
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData

class SpotDiffCallback(
        private val old: ArrayList<ProfileData>,
        private val new: List<ProfileData?>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition]._id == new[newPosition]?._id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition] == new[newPosition]
    }

}
