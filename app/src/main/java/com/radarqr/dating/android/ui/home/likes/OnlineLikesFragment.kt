package com.radarqr.dating.android.ui.home.likes

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.FragmentLikesLayoutBinding
import com.radarqr.dating.android.ui.home.likenew.LikeFragment
import com.radarqr.dating.android.ui.home.likenew.adapter.LikeAdapter
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.StaggeredItemDecoration
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import org.koin.androidx.viewmodel.ext.android.viewModel


class OnlineLikesFragment : BaseFragment<FragmentLikesLayoutBinding>() {


    private val likesViewModel: LikesViewModel by viewModel()
    private var adapter: LikeAdapter? = null
    var page = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        page = 1
        likesViewModel.onlineLikeRequest.page = page
        binding.progressBar.visible(isVisible = likesViewModel.onlineLikesList.isEmpty() && BaseUtils.isInternetAvailable())
        binding.tvNoInternet.visible(isVisible = likesViewModel.onlineLikesList.isEmpty() && !BaseUtils.isInternetAvailable())

        setAdapter()
        getOnlineLikes()

        binding.tvNoInternet.setOnClickListener {
            resetData()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            resetData()
        }
    }


    private fun resetData() {
        page = 1
        likesViewModel.onlineLikeRequest.page = page
        likesViewModel.onlineLikesList.clear()
        getOnlineLikes()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun getLayoutRes(): Int = R.layout.fragment_likes_layout

    private fun setAdapter() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = LikeAdapter(ArrayList()) { data, position ->
            data?.let {
                likesViewModel.handler?.openBottomSheet(data) {
                    position.removeFromPosition(data._id ?: "")
                }
            }
        }
        adapter?.setHasStableIds(true)
        binding.rvLikes.setHasFixedSize(true)
        binding.rvLikes.addItemDecoration(StaggeredItemDecoration(25.toPx.toInt()))
        binding.rvLikes.layoutManager = layoutManager
        binding.rvLikes.adapter = adapter
        if (likesViewModel.onlineLikesList.isNotEmpty())
            Handler(Looper.getMainLooper()).postDelayed({
                setList()
            }, 50)

        binding.rvLikes.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                likesViewModel.onlineLikesIsLoading = true
                adapter?.addLoadingView()
                page += 1
                likesViewModel.onlineLikeRequest.page = page
                getOnlineLikes(fromPagination = true)

            }

            override val isLastPage: Boolean
                get() = likesViewModel.onlineLikesIsLastPage
            override val isLoading: Boolean
                get() = likesViewModel.onlineLikesIsLoading
        })
    }


    private fun getOnlineLikes(fromPagination: Boolean = false) {
        if (BaseUtils.isInternetAvailable()) {
            if (view != null && isVisible && isAdded) {
                likesViewModel.getLikes(
                    likesViewModel.onlineLikeRequest.page,
                    likesViewModel.onlineLikeRequest.limit,
                    likesViewModel.onlineLikeRequest.category
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            binding.progressBar.visible(isVisible = false)
                            binding.isEmpty = (likesViewModel.onlineLikesList.isEmpty())
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            binding.progressBar.visible(isVisible = false)
                            (activity as HomeActivity?)?.setLikeCount(it.data.data.total_records)

                            if (fromPagination)
                                adapter?.removeLoadingView()
                            if (page == 1)
                                likesViewModel.onlineLikesList.clear()
                            likesViewModel.onlineLikesList.addAll(it.data.data.users)

                            likesViewModel.onlineLikesIsLastPage =
                                (it.data.data.online_count <= likesViewModel.onlineLikesList.size || it.data.data.users.isEmpty())

                            setList()
                            likesViewModel.onlineLikesIsLoading = false
                            binding.isEmpty = (likesViewModel.onlineLikesList.isEmpty())
                        }
                    }
                }
            }
        } else {
            binding.tvNoInternet.visible(likesViewModel.onlineLikesList.isEmpty())
        }
    }

    private fun setList() {
        adapter?.apply {
            val old = this.list
            val new = likesViewModel.onlineLikesList
            val callback = LikeFragment.SpotDiffCallback(old, new)
            val result = DiffUtil.calculateDiff(callback)
            setLikeList(new)
            result.dispatchUpdatesTo(this)
        }
    }

    private fun Int.removeFromPosition(id: String) {
        if (this < likesViewModel.onlineLikesList.size) {
            likesViewModel.onlineLikesList.removeAt(this)
            adapter?.list?.removeAt(this)
            adapter?.notifyItemRemoved(this)
            (activity as HomeActivity?)?.minusLikeCountByOne()
        }
        binding.isEmpty = (likesViewModel.onlineLikesList.isEmpty())
    }
}
