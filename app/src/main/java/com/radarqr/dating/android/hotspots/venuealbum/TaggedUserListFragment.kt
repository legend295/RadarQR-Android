package com.radarqr.dating.android.hotspots.venuealbum

import android.os.Bundle
import android.view.View
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.FragmentTaggedUserListBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment

class TaggedUserListFragment : VenueBaseFragment<FragmentTaggedUserListBinding>() {

    override fun getLayoutRes(): Int = R.layout.fragment_tagged_user_list

    override fun init(view: View, savedInstanceState: Bundle?) {

    }
}