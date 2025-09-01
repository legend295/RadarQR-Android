package com.radarqr.dating.android.hotspots.venue

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentMyVenuesBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.helpers.*
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.hotspots.model.SubmitVenue
import com.radarqr.dating.android.hotspots.venuedetail.VenueDetailsFragment
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyVenuesFragment : VenueBaseFragment<FragmentMyVenuesBinding>(), ViewClickHandler,
        (Int, MyVenuesData, Int) -> Unit {

    private val myVenueViewModel: MyVenueViewModel by viewModel()
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private val adapter by lazy {
        VenuesListAdapter(myVenueViewModel.myVenueData, this)
    }

    override fun getLayoutRes(): Int = R.layout.fragment_my_venues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        venueUpdateViewModel.updatingVenueData.value = null
        myVenueViewModel.clear()
    }


    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this

        setObservers()

        binding.swipeRefreshLayout.setOnRefreshListener {
            myVenueViewModel.isLastPage = false
            myVenueViewModel.pageNo = 1
            getMyVenues()
        }

        if (this.view != null && isAdded)
            (activity is HomeActivity).apply {
                if (!this) return
                val ivInfo = (activity as HomeActivity?)?.findViewById<AppCompatImageView>(
                    R.id.ivInfo
                )
                ivInfo?.setOnClickListener {
                    requireContext().howVenueWorkSheet()
                }
            }


        pagination()
    }

    private fun pagination() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvVenues.layoutManager = layoutManager
        if (!adapter.hasObservers())
            adapter.setHasStableIds(true)
        binding.rvVenues.adapter = adapter
        binding.rvVenues.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                myVenueViewModel.isLoading = true
                myVenueViewModel.pageNo++
                adapter.addLoadingView()
                Handler(Looper.getMainLooper()).postDelayed({
                    getMyVenues()
                }, 400)

            }

            override val isLastPage: Boolean
                get() = myVenueViewModel.isLastPage
            override val isLoading: Boolean
                get() = myVenueViewModel.isLoading
        })
    }

    private fun setObservers() {
        if (this.view != null && isAdded) {
            venueUpdateViewModel.updatingVenueData.observe(viewLifecycleOwner) {
                it?.let {
                    if (myVenueViewModel.position >= 0 &&
                        myVenueViewModel.position < myVenueViewModel.myVenueData.size
                    ) {
                        myVenueViewModel.myVenueData[myVenueViewModel.position] = it
                    }
                }
            }

            if (myVenueViewModel.myVenueData.isEmpty()) {
                binding.progressBarDataLoading.visible(isVisible = true)
                getMyVenues()
            }
        }
    }

    private fun getMyVenues() {
        if (this.view != null && isAdded)
            lifecycleScope.launchWhenCreated {
                myVenueViewModel.getMyVenueList().observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            adapter.removeLoadingView()
                            if (it.statusCode == 404) {
                                myVenueViewModel.isLastPage = true
                            }
                            if (myVenueViewModel.pageNo > 1) {
                                myVenueViewModel.pageNo--
                            } else myVenueViewModel.pageNo = 1

                            myVenueViewModel.isLoading = false

                            binding.progressBarDataLoading.visible(isVisible = false)
                            binding.swipeRefreshLayout.isRefreshing = false
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            binding.progressBarDataLoading.visible(isVisible = false)
                            myVenueViewModel.isLoading = false

                            if (myVenueViewModel.pageNo == 1) {
                                myVenueViewModel.myVenueData.clear()
                                adapter.refresh()
                                myVenueViewModel.isLastPage = false
                            } else if (myVenueViewModel.pageNo > 1) {
                                adapter.removeLoadingView()
                            }

                            it.data.data?.venues?.let { it1 ->
                                if (it1.isNotEmpty())
                                    setData(it1)
                            }
                            myVenueViewModel.isLastPage =
                                (it.data.data?.total_count
                                    ?: 0) <= myVenueViewModel.myVenueData.size
                        }
                    }
                }
            }

    }

    private fun setData(it: ArrayList<MyVenuesData>) {
        it.forEachIndexed { _, myVenuesData ->
            if (!myVenueViewModel.myVenueData.contains(myVenuesData)) {
                myVenueViewModel.myVenueData.add(myVenuesData)
                adapter.notifyItemInserted(myVenueViewModel.myVenueData.size - 1)
            }
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.tvAddNewHotspot -> {
                /*requireContext().registerToBeAHotSpotFullScreenDialog {
                    this.view?.findNavController()
                        ?.navigate(R.id.action_yourVenueFragment_to_NameFragment)
                }*/
                this.view?.findNavController()
                    ?.navigate(R.id.action_yourVenueFragment_to_NameFragment)
            }
        }
    }

    private fun String.submitVenue(callback: (MyVenuesData?) -> Unit) {
        venueUpdateViewModel.submitVenue(SubmitVenue(this)).observe(viewLifecycleOwner) {
            when (it) {
                DataResult.Empty -> {}
                is DataResult.Failure -> {
                    it.message?.let { it1 -> requireContext().showToast(it1) }
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    callback(it.data.data)
                }
            }
        }
    }

    override fun invoke(id: Int, venueData: MyVenuesData, position: Int) {
        if (view == null || !isAdded) return
        myVenueViewModel.position = position
        venueUpdateViewModel.updatingVenueData.value = venueData
        when (id) {
            R.id.tvStatus -> {
                when (venueData.status) {
                    Constants.VenueStatus.SEND_FOR_APPROVAL -> {
                        if (venueData.isReadyForApproval) {
                            with(requireContext()) {
                                sendForApprovalSheet { _, bottomSheetDialog, layout ->
                                    bottomSheetDialog.dismiss()
                                    //Show approval dialog
                                    approvalDialog { dialog, layoutApprovalDialogBinding ->
                                        venueData._id.submitVenue { data ->
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                dialog.dismiss()
                                            }, 200)
                                            data?.let {
                                                myVenueViewModel.myVenueData[myVenueViewModel.position] =
                                                    data
                                                venueUpdateViewModel.updatingVenueData.value = data
                                                adapter.notifyItemChanged(position)
                                                // use send for approval api then show approval complete dialog \
                                                // Update venue status for the selected item if response is in success
                                                // because on behalf of status we are showing text of approval or approved
                                                approvalCompleteDialog {
                                                    it.cancel()
                                                }
                                            }
                                            /*Handler(Looper.getMainLooper()).postDelayed(
                                                { dialog.dismiss() },
                                                100
                                            )*/

                                        }
                                    }
                                }
                            }
                        } else {
                            val bundle = Bundle().apply {
                                putSerializable(Constants.EXTRA_DATA, venueData)
                            }
                            this.view?.findNavController()
                                ?.navigate(
                                    R.id.action_yourVenueFragment_to_venueTypeFragment,
                                    bundle
                                )
                        }
                    }

                    Constants.VenueStatus.DISAPPROVED -> {
                        requireContext().actionNeeded()
                    }

                    Constants.VenueStatus.IN_PROGRESS -> {
                        // Open bottom sheet or dialog which will show the status of the venue something like this - will be approved shortly our team is viewing
                    }

                    Constants.VenueStatus.APPROVED -> {
                        // open bottom sheet or dialog showing everything is done
                    }

                    Constants.VenueStatus.VENUE_PAUSED -> {
                        venueData.pauseComment?.let { requireContext().venuePausedDialog(it) }
                    }
                }


            }

            R.id.tvViewVenue, R.id.clParent -> {
                val bundle = Bundle().apply {
                    putBoolean(Constants.IS_MY_VENUE, true)
                    putString(Constants.VENUE_ID, venueData._id)
                    putInt(Constants.FROM, VenueDetailsFragment.FROM_MY_VENUE)
                }
                this.view?.findNavController()
                    ?.navigate(R.id.action_yourVenueFragment_to_venueDetailsFragment, bundle)
            }
        }

    }
}