package com.radarqr.dating.android.hotspots.tag

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.FriendInfo
import com.radarqr.dating.android.data.model.RequesterInfo
import com.radarqr.dating.android.data.model.SearchCloseFriendRequest
import com.radarqr.dating.android.data.model.SearchUser
import com.radarqr.dating.android.databinding.FragmentSearchCloseFriendBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.DebouncingQueryTextListener
import com.radarqr.dating.android.utility.Utility.openKeyboard
import com.radarqr.dating.android.utility.Utility.visible
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchCloseFriendFragment : VenueBaseFragment<FragmentSearchCloseFriendBinding>() {

    private val list = ArrayList<SearchUser?>()
    private var searchText = ""
    private val tagViewModel: TagViewModel by viewModel()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val adapter by lazy { SearchCloseFriendAdapter(list, tagViewModel.tagModel) }

    override fun getLayoutRes(): Int = R.layout.fragment_search_close_friend

    override fun init(view: View, savedInstanceState: Bundle?) {
        HomeActivity.activeFragment.value = this
        binding.searchViewFriend.requestFocus()
        binding.searchViewFriend.openKeyboard()

        bindRecyclerView()
        addSelfUserInUI()
        setSearchView()

        binding.tvCancel.setOnClickListener { this.view?.findNavController()?.popBackStack() }
    }

    private fun setSearchView() {
        // Search view
        binding.searchViewFriend.addTextChangedListener(DebouncingQueryTextListener(lifecycle) {
            // added hasFocus() check, to prevent unwanted api call when switching through the tabs
            searchText = it ?: ""
            it?.apply {
                tagViewModel.searchPageNo = 1
                if (searchText.isNotEmpty()) {
                    tagViewModel.isLoading = false
                    tagViewModel.isLastPage = false
                    binding.progressBarSearch.visible(isVisible = true)
                    searchCloseFriend()
                } else {
                    addSelfUserInUI()
                }
            }
            /*  if (binding.searchViewFriend.hasFocus()) {
                  it?.apply {
                      tagViewModel.searchPageNo = 1
                      tagViewModel.isLastPage = false
                      binding.progressBarSearch.visible(isVisible = true)
                      searchCloseFriend()
                  }
              } else if (searchText.isEmpty()) {
                  tagViewModel.searchPageNo = 1
                  tagViewModel.isLastPage = false
                  binding.progressBarSearch.visible(isVisible = true)
                  searchCloseFriend()
              }
  */
        })
    }

    private fun addSelfUserInUI() {
        if (searchText.isEmpty()) {
            tagViewModel.isLastPage = true
            list.clear()
            list.add(getSelfUser())
            adapter.refresh()
            binding.tvEmptyMessage.visible(isVisible = list.isEmpty())
        }
    }

    private fun bindRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.apply {
            adapter = this@SearchCloseFriendFragment.adapter
            this.layoutManager = layoutManager
        }

        adapter.apply {
            // open dialog when user want to add friend to close friend
            clickHandler = { searchUser: SearchUser, _: Int ->
                tagViewModel.selectedSearchedUser = searchUser
                this@SearchCloseFriendFragment.view?.findNavController()?.popBackStack()
            }

        }

        // THIS IS DONE LIKE THIS BECAUSE WE NEED TO HIDE KEYBOARD WHEN USER STARTS TO SCROLLING ELSE WE WILL USER PAGINATION-SCROLL-LISTENER CLASS
        binding.rvFriends.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!tagViewModel.isLoading && !tagViewModel.isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        tagViewModel.isLoading = true
                        adapter.addLoader()
                        tagViewModel.searchPageNo += 1
                        searchCloseFriend()
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    // IN TIME OF DRAGGING AND SETTLING  HIDE THE KEYBOARD
                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                        hideKeyboard(recyclerView)
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchCloseFriend() {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                tagViewModel.searchCloseFriend(
                    SearchCloseFriendRequest(
                        10,
                        tagViewModel.searchPageNo,
                        searchText
                    )
                ).observe(viewLifecycleOwner) {
                    adapter.removeLoader()
                    tagViewModel.isLoading = false
                    binding.progressBarSearch.visible(isVisible = false)
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            if (it.statusCode == 404) {
                                tagViewModel.searchPageNo = 1
                                list.clear()
                                adapter.notifyDataSetChanged()
                            }
                            binding.tvEmptyMessage.visible(list.isEmpty())
                        }
                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            if (tagViewModel.searchPageNo == 1) {
                                list.clear()
                            }
                            list.addAll(it.data.data?.users ?: ArrayList())
                            tagViewModel.isLastPage =
                                list.size >= (it.data.data?.total_count
                                    ?: 0)
                            adapter.notifyDataSetChanged()
                            binding.tvEmptyMessage.visible(list.isEmpty())
                        }
                    }
                }

            }
        }
    }

    private fun getSelfUser(): SearchUser? {
        var searchUser: SearchUser? = null
        try {
            getProfileViewModel.profileData.value?.let {
                searchUser = it.getSearchUser()
            } ?: kotlin.run {
                getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
                    data?.let {
                        getProfileViewModel.profileData.value = it
                        searchUser = it.getSearchUser()
                    }
                }
            }

        } catch (_: java.lang.Exception) {

        }
        return searchUser
    }

    private fun ProfileData.getSearchUser(): SearchUser {
        return SearchUser(
            "",
            false,
            "",
            FriendInfo(
                _id ?: "",
                name ?: "",
                if (images.isNullOrEmpty()) "" else images[0],
                username ?: ""
            ),
            false,
            "",
            RequesterInfo(
                _id ?: "",
                name ?: "",
                if (images.isNullOrEmpty()) "" else images[0],
                username ?: ""
            )
        )
    }
}