package com.radarqr.dating.android.hotspots.closefriend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentCloseFriendAndRequestBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.utility.Utility.color
import com.radarqr.dating.android.utility.Utility.drawable
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CloseFriendAndRequestFragment : VenueBaseFragment<FragmentCloseFriendAndRequestBinding>(),
    ViewClickHandler {

    private var isCloseFriend: Boolean = true
    private var fragment: Fragment? = null
    private val closeFriendFragment = CloseFriendFragment()
    private val friendRequestFragment = FriendRequestFragment()
    private val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel by viewModel()

    override fun getLayoutRes(): Int = R.layout.fragment_close_friend_and_request

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        closeFriendAndRequestViewModel.resetData()

    }

    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.clickHandler = this
        val closeFriend = arguments?.getBoolean(Constants.EXTRA)
        closeFriend?.let {
            isCloseFriend = it
        }

        handleView(isCloseFriend)

        Handler(Looper.getMainLooper()).postDelayed({
            // if already on Requests tab screen than didn't need to hit the api as same api got hit on requests tab screen
            if (isCloseFriend)
                getCloseFriendInvitations()
        }, 200)

        setHelper()
    }

    private fun setHelper() {
        closeFriendAndRequestViewModel.requestCountHelper =
            object : CloseFriendAndRequestViewModel.RequestCountHelper {
                override fun setCount(count: Int) {
                    count.setRequestsCount()
                }
            }
    }

    private fun handleView(isCloseFriend: Boolean) {
        this.isCloseFriend = isCloseFriend
        binding.tvCloseFriends.background =
            requireContext().drawable(if (isCloseFriend) R.drawable.bg_round_with_radius_6_teal else R.drawable.bg_round_stroke_with_radius_6)
        binding.tvCloseFriends.setTextColor(requireContext().color(if (isCloseFriend) R.color.white else R.color.iconsColorDark))
//        binding.tvCloseFriends.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),if (isCloseFriend) R.color.mobile_back else R.color.transparent))

        binding.tvRequest.background =
            requireContext().drawable(if (!isCloseFriend) R.drawable.bg_round_with_radius_6_teal else R.drawable.bg_round_stroke_with_radius_6)
        binding.tvRequest.setTextColor(requireContext().color(if (!isCloseFriend) R.color.white else R.color.iconsColorDark))

        loadFragment()
    }

    private fun getCloseFriendInvitations() {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.getCloseFriendInvitations(1, 1)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                (0).setRequestsCount()
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                (it.data.data?.total_count ?: 0).setRequestsCount()
                            }
                        }
                    }
            }
        }
    }

    private fun Int.setRequestsCount() {
        if (this > 0) {
            binding.tvRequestsCount.visible(isVisible = true)
            binding.tvRequestsCount.text = this.toString()
        } else binding.tvRequestsCount.visible(isVisible = false)
    }

    private fun loadFragment() {
        fragment = if (isCloseFriend) closeFriendFragment else friendRequestFragment
        fragment?.let {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.container, it)
            transaction.commit()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvCloseFriends -> {
                if (!isCloseFriend)
                    handleView(isCloseFriend = true)
            }

            R.id.tvRequest -> {
                if (isCloseFriend)
                    handleView(isCloseFriend = false)
            }
        }
    }
}