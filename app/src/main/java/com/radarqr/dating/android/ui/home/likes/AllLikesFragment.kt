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
import com.radarqr.dating.android.base.HomeBaseFragment
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

class AllLikesFragment : BaseFragment<FragmentLikesLayoutBinding>() {

    private val likesViewModel: LikesViewModel by viewModel()
    private var adapter: LikeAdapter? = null
    var page = 1

    override fun getLayoutRes(): Int = R.layout.fragment_likes_layout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        page = 1
        likesViewModel.allLikesRequest.page = page
        binding.progressBar.visible(isVisible = likesViewModel.allLikesList.isEmpty() && BaseUtils.isInternetAvailable())
        binding.tvNoInternet.visible(isVisible = likesViewModel.allLikesList.isEmpty() && !BaseUtils.isInternetAvailable())
        setAdapter()
        getAllLikes()

        binding.tvNoInternet.setOnClickListener {
            resetData()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            resetData()
        }
    }

    private fun resetData() {
        page = 1
        likesViewModel.allLikesRequest.page = page
        likesViewModel.allLikesList.clear()
        getAllLikes()
        binding.swipeRefreshLayout.isRefreshing = false
    }


    private fun setAdapter() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = LikeAdapter(ArrayList()) { data, position ->
            data?.let {
                likesViewModel.handler?.openBottomSheet(it) {
                    position.removeFromPosition(data._id ?: "")
                }
            }
        }
        adapter?.setHasStableIds(true)
        binding.rvLikes.setHasFixedSize(true)
        binding.rvLikes.addItemDecoration(StaggeredItemDecoration(25.toPx.toInt()))
        binding.rvLikes.layoutManager = layoutManager
        binding.rvLikes.adapter = adapter
        if (likesViewModel.allLikesList.isNotEmpty())
            Handler(Looper.getMainLooper()).postDelayed({
                setList()
            }, 50)

        binding.rvLikes.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                likesViewModel.allLikesIsLoading = true
                adapter?.addLoadingView()
                page += 1
                likesViewModel.allLikesRequest.page = page
                getAllLikes(fromPagination = true)

            }

            override val isLastPage: Boolean
                get() = likesViewModel.allLikesIsLastPage
            override val isLoading: Boolean
                get() = likesViewModel.allLikesIsLoading
        })
    }


    private fun getAllLikes(fromPagination: Boolean = false) {
        if (BaseUtils.isInternetAvailable()) {
            if (view != null && isVisible && isAdded) {
                likesViewModel.getLikes(
                    likesViewModel.allLikesRequest.page,
                    likesViewModel.allLikesRequest.limit,
                    likesViewModel.allLikesRequest.category
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            binding.progressBar.visible(isVisible = false)
                            binding.isEmpty = (likesViewModel.allLikesList.isEmpty())
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            binding.progressBar.visible(isVisible = false)
                            if (fromPagination)
                                adapter?.removeLoadingView()

                            (activity as HomeActivity?)?.setLikeCount(it.data.data.total_records)
                            if (page == 1)
                                likesViewModel.allLikesList.clear()
                            likesViewModel.allLikesList.addAll(it.data.data.users)

                            likesViewModel.allLikesIsLastPage =
                                (it.data.data.total_records <= likesViewModel.allLikesList.size || it.data.data.users.isEmpty())

                            setList()
                            likesViewModel.allLikesIsLoading = false
                            binding.isEmpty = (likesViewModel.allLikesList.isEmpty())
                        }
                    }
                }
            }
        } else {
            binding.tvNoInternet.visible(likesViewModel.allLikesList.isEmpty())
        }
    }

    private fun setList() {
        adapter?.apply {
            val old = this.list
            val new = likesViewModel.allLikesList
            val callback = LikeFragment.SpotDiffCallback(old, new)
            val result = DiffUtil.calculateDiff(callback)
            setLikeList(new)
            result.dispatchUpdatesTo(this)
        }
    }

    private fun Int.removeFromPosition(id: String) {
        if (this < likesViewModel.allLikesList.size) {
            likesViewModel.allLikesList.removeAt(this)
            adapter?.list?.removeAt(this)
            adapter?.notifyItemRemoved(this)
            (activity as HomeActivity?)?.minusLikeCountByOne()
        }
        binding.isEmpty = (likesViewModel.allLikesList.isEmpty())
    }

}
