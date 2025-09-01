package com.radarqr.dating.android.ui.home.settings

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.profile.BasicInfoData
import com.radarqr.dating.android.databinding.FragmentViewProfileBinding
import com.radarqr.dating.android.databinding.LayoutImageVideoViewBinding
import com.radarqr.dating.android.ui.home.main.adapter.BasicCommonAdapter
import com.radarqr.dating.android.ui.home.settings.adapter.HobbyAdapter
import com.radarqr.dating.android.ui.home.settings.adapter.ImageMoreAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.addOnPageChangedListener
import com.radarqr.dating.android.utility.BaseUtils.getCurrentPosition
import com.radarqr.dating.android.utility.BaseUtils.stopAllPlayers
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class ViewProfileFragment : BaseFragment<FragmentViewProfileBinding>(), View.OnClickListener,
    ViewClickHandler, SwipeRefreshLayout.OnRefreshListener {

    private val preferencesHelper: PreferencesHelper by inject()
    lateinit var hobbyAdapter: HobbyAdapter
    lateinit var hobbyWorkCommonAdapter: BasicCommonAdapter
    lateinit var basiccommonadapter: BasicCommonAdapter
    var listItem: ArrayList<BasicInfoData> = ArrayList()
    var hobbiesList: ArrayList<HobbiesData> = ArrayList()
    var workItem: ArrayList<BasicInfoData> = ArrayList()
    var newImageList: ArrayList<String> = ArrayList()
    lateinit var imageMoreAdapter: ImageMoreAdapter
    var itemPos = 0
    private var player: SimpleExoPlayer? = null
    private val playerList: TreeMap<Int, SimpleExoPlayer> = TreeMap()
    private var currentlyPlayingPosition = 0
    private val getProfileViewModel: GetProfileViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = getProfileViewModel
        binding.viewHandler = this
        binding.viewProfileFragment = this
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        binding.tvNoInternet.setOnClickListener {
            try {
                getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
                    data?.let {
                        getProfileViewModel.profileData.value = it
                    }
                }
            } catch (e: java.lang.Exception) {

            }
        }

        clickListener()
        checkVisiblePortion()
        setData()
    }

    private fun setData() {
        getProfileViewModel.profileData.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            it?.let {
                try {
                    for (player in playerList.values) {
                        player.playWhenReady = false
                        player.release()
                    }
                    binding.tvNoInternet.visibility = View.GONE
                    binding.clDetails.visibility = View.VISIBLE
                    binding.moreView.clMoreImage.visibility = View.VISIBLE
                    binding.clTop.visibility = View.VISIBLE
                    listItem.clear()
                    workItem.clear()
                    newImageList.clear()
                    hobbiesList.clear()
                    playerList.clear()
                    binding.clCmain.visibility = View.VISIBLE

                    BaseUtils.getListItem(it) { list ->
                        listItem.addAll(list)
                    }

                    BaseUtils.getWorkItem(it) { list ->
                        workItem.addAll(list)
                    }

                    it.hobbies_interest?.apply {
                        binding.rvHobby.visibility =
                            if (isNotEmpty()) View.VISIBLE else View.GONE
                        binding.tvHobby.visibility =
                            if (isNotEmpty()) View.VISIBLE else View.GONE
                    }
                    hobbiesList = it.hobbies_interest as ArrayList<HobbiesData>
                    try {
                        val chipsLayoutManager =
                            ChipsLayoutManager.newBuilder(requireActivity())
                                .setChildGravity(Gravity.TOP)
                                .setScrollingEnabled(true)
                                .setGravityResolver { Gravity.CENTER }
                                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                                .build()

                        binding.rvHobby.layoutManager = chipsLayoutManager

                        hobbyAdapter = HobbyAdapter(
                            ArrayList(),
                            requireActivity(), "1"
                        )
                        binding.rvHobby.apply {
                            itemAnimator = DefaultItemAnimator()
                            adapter = hobbyAdapter
                        }
                        hobbyAdapter.clear()
                        hobbyAdapter.addAll(hobbiesList as ArrayList<HobbiesData>)

                        binding.rvWork.visibility =
                            if (workItem.isEmpty()) View.GONE else View.VISIBLE
                        binding.tvWork.visibility =
                            if (workItem.isEmpty()) View.GONE else View.VISIBLE

                        basiccommonadapter = BasicCommonAdapter(
                            listItem,
                            requireActivity()
                        )

                        hobbyWorkCommonAdapter = BasicCommonAdapter(
                            workItem,
                            requireActivity()
                        )
                        binding.rvWork.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            itemAnimator = DefaultItemAnimator()
                            adapter = hobbyWorkCommonAdapter
                            adapter?.notifyDataSetChanged()
                        }

                        val chipsLayoutManager2 =
                            ChipsLayoutManager.newBuilder(requireActivity())
                                .setChildGravity(Gravity.TOP)
                                .setScrollingEnabled(true)
                                .setGravityResolver { Gravity.CENTER }
                                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                                .build()
                        binding.rvBasic.layoutManager = chipsLayoutManager2

                        binding.rvBasic.apply {
                            itemAnimator = DefaultItemAnimator()
                            adapter = basiccommonadapter
                            adapter?.notifyDataSetChanged()
                        }
                        val list = ArrayList<String>()
                        it.images?.let {
                            list.addAll(it)
                            /*for ((i, data) in it.withIndex()) {
                                getProfileViewModel.viewModeUserImages[data]?.let { image ->
                                    list.add(
                                        image
                                    )
                                }
                            }*/
                        }

                        setImage(list)
                        if (list.size > 6) {
                            binding.moreView.clMoreImage.visibility = View.VISIBLE
                            binding.moreView.tvMorePhoto.visibility = View.VISIBLE
                            for (i in list.indices) {
                                if (i > 5) {
                                    newImageList.add(list[i])
                                }
                            }
                            imageMoreAdapter = ImageMoreAdapter(
                                newImageList,
                                requireActivity()
                            )

                            imageMoreAdapter.errorException = {
                                handleVideoError()
                            }

                            binding.moreView.rvMoreImages.isNestedScrollingEnabled = false
                            binding.moreView.rvMoreImages.apply {
                                itemAnimator = DefaultItemAnimator()
                                adapter = imageMoreAdapter

                            }
                            binding.moreView.rvMoreImages.addOnPageChangedListener { position ->
                                itemPos = position
                                imageMoreAdapter.playPlayer(itemPos)
                            }
                            val snapHelper: SnapHelper = PagerSnapHelper()
                            try {
                                snapHelper.attachToRecyclerView(binding.moreView.rvMoreImages)
                            } catch (e: Exception) {
                            }
                        } else {
                            binding.moreView.clMoreImage.visibility = View.GONE
                            binding.moreView.tvMorePhoto.visibility = View.GONE
                        }
                    } catch (e: Exception) {

                    }
                } catch (e: Exception) {

                }
            }
        }
    }


    fun clickListener() {
        binding.tvName.setOnClickListener(this)
        binding.moreView.rlLeft.setOnClickListener(this)
        binding.moreView.rlRight.setOnClickListener(this)
    }


    override fun getLayoutRes(): Int = R.layout.fragment_view_profile

    override fun onResume() {
        super.onResume()
        if (BaseUtils.isInternetAvailable()) {
            binding.tvNoInternet.visibility = View.GONE
            binding.clDetails.visibility = View.VISIBLE
            if (getProfileViewModel.profileData.value?.images?.size ?: 0 > 6)
                binding.moreView.clMoreImage.visibility = View.VISIBLE
            binding.clTop.visibility = View.VISIBLE
            if (currentlyPlayingPosition == 6) {
                if (this::imageMoreAdapter.isInitialized)
                    imageMoreAdapter.playPlayer(itemPos)
            } else
                playPlayer(currentlyPlayingPosition)
        } else {
            if (getProfileViewModel.profileData.value == null) {
                binding.tvNoInternet.visibility = View.VISIBLE
                binding.moreView.clMoreImage.visibility = View.GONE
                binding.clDetails.visibility = View.GONE
                binding.clTop.visibility = View.GONE
            }
        }


    }


    private fun checkVisiblePortion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.svProfile.setOnScrollChangeListener(
                BaseUtils.scrollListener(
                    scrollView = binding.svProfile,
                    list = getImageViewsWithProgress(),
                    recyclerTopView = binding.moreView.viewTop,
                    recyclerBottomView = binding.moreView.viewBottom
                ) {
//                    if (playerList.isEmpty() || this::imageMoreAdapter.isInitialized) return@scrollListener
                    when (it) {
                        6 -> {
                            stopAllPlayers(playerList)
                            currentlyPlayingPosition = 6
                            try {
                                if (this::imageMoreAdapter.isInitialized)
                                    imageMoreAdapter.playPlayer(itemPos)
                            } catch (e: Exception) {

                            }
                        }
                        else -> {
                            currentlyPlayingPosition = it
                            playPlayer(it)
                        }
                    }
                })
        }
    }


    private fun playPlayer(position: Int) {
        if (playerList.isNotEmpty()) {
            for (pos in 0..6) {
                if (pos == position) {
                    if (playerList.containsKey(pos)) {
                        playerList[pos]?.playWhenReady = true
                    }
                } else {
                    if (playerList.containsKey(pos)) {
                        playerList[pos]?.playWhenReady = false
                    }
                }
            }
        }

        try {
            if (this::imageMoreAdapter.isInitialized)
                imageMoreAdapter.stopAllPlayers()
        } catch (e: Exception) {

        }
    }

    private fun setImage(list: List<String>) {
        for (position in list.indices) {
            if (position >= 6) return
            getImageViewsWithProgress()[position].cardView.visibility = View.VISIBLE
            getImageViewsWithProgress()[position].progressBarVideo.visibility =
                if (!list[position].contains(Constants.MP4)) View.GONE else View.VISIBLE

            if (!list[position].contains(Constants.MP4)) {
                getImageViewsWithProgress()[position].ivVolume.visibility = View.GONE
                getImageViewsWithProgress()[position].videoView.visibility = View.GONE
                getImageViewsWithProgress()[position].ivImage.visibility = View.VISIBLE
                Glide.with(binding.root).load(list[position])
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            getImageViewsWithProgress()[position].progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            getImageViewsWithProgress()[position].progressBar.visibility = View.GONE
                            return false
                        }
                    }).into(getImageViewsWithProgress()[position].ivImage)
            } else {
                getImageViewsWithProgress()[position].ivVolume.visibility = View.VISIBLE
                getImageViewsWithProgress()[position].ivImage.visibility = View.GONE
                getImageViewsWithProgress()[position].videoView.visibility = View.VISIBLE
                getImageViewsWithProgress()[position].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                Log.d("VIDEO_URL", list[position])
                initializePlayer(
                    Uri.parse(list[position]),
                    getImageViewsWithProgress()[position].videoView,
                    position
                )

                getImageViewsWithProgress()[position].progressBar.visibility = View.GONE
            }
        }
    }

    private fun getImageViewsWithProgress(): ArrayList<LayoutImageVideoViewBinding> {
        val list = ArrayList<LayoutImageVideoViewBinding>()
        list.add(binding.firstView)
        list.add(binding.secondView)
        list.add(binding.thirdView)
        list.add(binding.forthView)
        list.add(binding.fifthView)
        list.add(binding.sixthView)
        BaseUtils.handleClick(list, playerList)
        return list
    }

    private fun initializePlayer(uri: Uri, videoView: PlayerView, position: Int) {
        player = null
        player = SimpleExoPlayer.Builder(requireActivity())
            .build()
            .also { exoPlayer ->
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)

                exoPlayer.prepare()
                exoPlayer.play()
            }
        player!!.volume = 0f


        if (position != 0)
            player!!.playWhenReady = false

        playerList[position] = player!!
        playerList[position]?.addListener(
            BaseUtils.playerListener(
                position,
                getImageViewsWithProgress()[position].progressBarVideo
            ) {
                handleVideoError()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        stopAllPlayers(playerList)
        try {
            if (this::imageMoreAdapter.isInitialized)
                imageMoreAdapter.stopAllPlayers()
        } catch (e: Exception) {

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        for (player in playerList.values) {
            player.playWhenReady = false
            player.release()
        }
//        stopAllPlayers(playerList)
        if (this::imageMoreAdapter.isInitialized)
            imageMoreAdapter.stopAllPlayers()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.rl_left -> {
                if (itemPos != 0) {
                    itemPos = binding.moreView.rvMoreImages.getCurrentPosition() - 1
                    binding.moreView.rvMoreImages.smoothScrollToPosition(itemPos)
                    imageMoreAdapter.playPlayer(itemPos)
                }
            }
            R.id.rl_right -> {
                if (itemPos < (newImageList.size - 1)) {
                    itemPos = binding.moreView.rvMoreImages.getCurrentPosition() + 1
                    binding.moreView.rvMoreImages.smoothScrollToPosition(itemPos)
                    imageMoreAdapter.playPlayer(itemPos)
                }
            }
        }
    }

    override fun onRefresh() {
        if (BaseUtils.isInternetAvailable()) {
            try {
                when (getProfileViewModel.profileData.value) {
                    null -> {
                        try {
                            getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
                                data?.let {
                                    getProfileViewModel.profileData.value = it
                                }
                            }
                        } catch (e: java.lang.Exception) {
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    }
                    else -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        if (!BaseUtils.isInternetAvailable()) {
                            CommonCode.setToast(
                                requireContext(),
                                resources.getString(R.string.no_internet_msg)
                            )
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        } else CommonCode.setToast(requireContext(), resources.getString(R.string.no_internet_msg))
    }

    private fun handleVideoError() {
//        getProfileViewModel.viewModeUserImages.clear()
        getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
            data?.let {
                getProfileViewModel.profileData.value = it
            }
        }
    }
}
