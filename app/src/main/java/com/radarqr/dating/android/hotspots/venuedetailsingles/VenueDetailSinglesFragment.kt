package com.radarqr.dating.android.hotspots.venuedetailsingles

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.SinglesUser
import com.radarqr.dating.android.databinding.FragmentVenueSinglesBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.hotspots.venue.MyVenueViewModel
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.serializable
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class VenueDetailSinglesFragment : VenueBaseFragment<FragmentVenueSinglesBinding>(),
    ViewClickHandler {

    private val list = ArrayList<SinglesUser?>()
    private var isMyVenue: Boolean = false
    private var isVenueCheckedIn: Boolean = false
    private val myVenueViewModel: MyVenueViewModel by viewModel()
    private var venueData: MyVenuesData? = null

    private var pageNo: Int = 1
    private var isLastPage = false
    private var isLoading = false

    private val adapter by lazy { VenueDetailSinglesAdapter(list, adapterClickHandler) }

    override fun getLayoutRes(): Int = R.layout.fragment_venue_singles

    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this

        // Get data from the previous screen
        isMyVenue = arguments?.getBoolean(Constants.IS_MY_VENUE, false) ?: false
        isVenueCheckedIn = arguments?.getBoolean(Constants.IS_VENUE_CHECKED_IN, false) ?: false
        venueData = arguments?.serializable(Constants.EXTRA_DATA) as MyVenuesData?
        venueData?.let { getSinglesForVenue() }
        // set layout binding values
        binding.isMyVenue = isMyVenue
        binding.isVenueCheckedIn = isVenueCheckedIn

        // set adapter fields
        val layoutManager = LinearLayoutManager(requireContext())
//        adapter.setHasStableIds(true)
        binding.rvVenueSingles.setHasFixedSize(true)
        binding.rvVenueSingles.layoutManager = layoutManager
        binding.rvVenueSingles.adapter = adapter
        binding.rvVenueSingles.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                ++pageNo
                getSinglesForVenue()
            }

            override val isLastPage: Boolean
                get() = this@VenueDetailSinglesFragment.isLastPage
            override val isLoading: Boolean
                get() = this@VenueDetailSinglesFragment.isLoading
        })

        // swipe refresh layout
        binding.swipeRefreshLayout.setOnRefreshListener {
            pageNo = 1
            getSinglesForVenue()
        }
    }

    private val adapterClickHandler = { data: SinglesUser ->
        data.handleAdapterClick()
    }

    private fun SinglesUser.handleAdapterClick() {
        val bundle = Bundle().apply {
            putString(Constants.USER_ID, _id)
            putSerializable(Constants.EXTRA, venueData)
            putInt(Constants.FROM, ProfileFragment.FROM_VENUE_SINGLES)
            putBoolean(Constants.TYPE, false)
        }
        this@VenueDetailSinglesFragment.view?.findNavController()
            ?.navigate(R.id.profileFragment, bundle)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }
        }
    }


    private fun getSinglesForVenue(limit: Int = 10) {
        if (view != null && isAdded && isVisible) {
            binding.progressBar.visible(list.isEmpty())
            lifecycleScope.launch {
                myVenueViewModel.getSinglesForVenue(venueData?._id!!, pageNo, limit)
                    .observe(viewLifecycleOwner) {
                        binding.swipeRefreshLayout.isRefreshing = false
                        isLoading = false
                        binding.progressBar.visible(isVisible = false)
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                if (pageNo > 1)
                                    pageNo--
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                if (pageNo == 1) {
                                    list.clear()
                                }
                                list.addAll(it.data.data?.users ?: ArrayList())
                                adapter.refresh()
                                isLastPage = (list.size >= (it.data.data?.total_count ?: 0))
                            }
                        }
                        binding.tvEmpty.visible(list.isEmpty())
                    }
            }
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
        }


    }
}