package com.radarqr.dating.android.ui.home.profile

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.profile.BasicInfoData
import com.radarqr.dating.android.databinding.*
import com.radarqr.dating.android.ui.home.main.adapter.BasicCommonAdapter
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.settings.adapter.HobbyAdapter
import com.radarqr.dating.android.ui.home.settings.adapter.ImageMoreAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.welcome.mobileLogin.*
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.addOnPageChangedListener
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList


class OtherProfileFragment : BaseFragment<FragmentOtherProfileBinding>(), View.OnClickListener {
    val getRecommendationViewModel: GetRecommendationViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    lateinit var hobbyAdapter: HobbyAdapter
    lateinit var hobbyWorkcommonadapter: BasicCommonAdapter
    lateinit var basiccommonadapter: BasicCommonAdapter
    var listItem: ArrayList<BasicInfoData> = ArrayList()
    var HobbyItem: List<HobbiesData> = ArrayList()
    var workItem: ArrayList<BasicInfoData> = ArrayList()
    var message = ""
    var Image_list: List<String> = ArrayList()
    var itemPos = 0
    var newImageList: ArrayList<String> = ArrayList()
    lateinit var imageMoreAdapter: ImageMoreAdapter
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    var user_id = ""
    var user_id_array: ArrayList<String> = ArrayList()
    var category = ""
    var my_user_id = ""
    var api_type = ""
    var tagnew = ""
    var name = ""
    var age = ""
    var image = ""
    var alertDialog: AlertDialog? = null
    var type: Boolean? = null
    lateinit var vibrator: Vibrator
    private var player: SimpleExoPlayer? = null
    private val playerList: TreeMap<Int, SimpleExoPlayer> = TreeMap()
    private var isUserProfilePaused = false

    companion object {
        const val EXIST = "exist"
        const val SEND = "send"
        const val ACCEPT = "accept"
        const val REQUEST_SENT = "request_sent"
        const val REQUEST_RECEIVE = "request_revceive"
        const val MATCH = "match"
        const val UN_MATCH = "un_match"
        const val DECLINE = "decline"
    }

    override fun getLayoutRes(): Int = R.layout.fragment_other_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.otherProfileFragment = this
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        showToolbarLayout(false)
        showBackButton(true)
        showToolbar(true)
        showToolbarWhite(false)
        showBackButtonWhite(false)
        showProgress(false)
        showNavigation(false)
        showSkip(false)
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        runBlocking(Dispatchers.IO) {
            my_user_id =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""
        }
        try {
            val data: Bundle? = arguments

            user_id = data?.getString("user_id")!!
            tagnew = data?.getString("tag")!!
            if (data?.containsKey("category") == true) {
                category = data?.getString("category")!!
            }
        } catch (e: Exception) {

        }

//set hobby
        if (tagnew.equals("1")) {
            getProfile()
        } else {
            binding.clParent.visibility = View.GONE
            radarDialog()

        }
        binding.moreView.rlLeft.setOnClickListener(this)
        binding.moreView.rlRight.setOnClickListener(this)
        checkVisiblePortion()
        binding.activityToolbarBack.setOnClickListener {
            showNavigation(true)
            findNavController().navigateUp()
        }
        binding.ivCancelRequest.setOnClickListener {
            /* if (isUserProfilePaused) {
                 showAlert("User's profile is paused")
                 return@setOnClickListener
             }
 */
            type = false
            when (api_type) {
                SEND -> {
                    like_dislike()
                }

                REQUEST_RECEIVE -> {
                    showAlert(Constants.REQUEST_RECEIVE)
                }
                REQUEST_SENT -> {
                    showAlert(Constants.REQUEST_SENT)
                }
                MATCH -> {
                    showAlert(Constants.MATCH_MESSAGE)
                }
                UN_MATCH -> {
                    showAlert(Constants.UN_MATCH_MESSAGE)
                }
                DECLINE -> {
                    showAlert(Constants.DECLINE)
                }

                /* EXIST -> {
                     findNavController().navigate(R.id.action_other_Profile_to_request_already_exist)
                 }

                 else -> {
                     accept_reject()
                 }*/
            }
        }

        binding.ivOptions.setOnClickListener {
            openReportSheet()
        }

        binding.ivSendRequest.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        250,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                );
            } else {
                //deprecated in API 26
                vibrator.vibrate(250)
            }

            /*  if (isUserProfilePaused) {
                  showAlert("User's profile is paused")
                  return@setOnClickListener
              }*/

            type = true

            when (api_type) {
                SEND -> {
                    like_dislike()
                }

                REQUEST_RECEIVE -> {
                    showAlert(Constants.REQUEST_RECEIVE)
                }
                REQUEST_SENT -> {
                    showAlert(Constants.REQUEST_SENT)
                }
                MATCH -> {
                    showAlert(Constants.MATCH_MESSAGE)
                }
                UN_MATCH -> {
                    showAlert(Constants.UN_MATCH_MESSAGE)
                }
                DECLINE -> {
                    showAlert(Constants.DECLINE)
                }

                /* EXIST -> {
                     findNavController().navigate(R.id.action_other_Profile_to_request_already_exist)
                 }

                 else -> {
                     openBottomSheet()
                 }*/
            }
        }
    }

    private fun showAlert(message: String) {
        showCustomAlert(message, "Ok", object : DialogClickHandler<Any> {
            override fun onClick(value: Any) {
                findNavController().popBackStack()
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    @ExperimentalCoroutinesApi
    private fun getProfile() {
        user_id_array.clear()
        user_id_array.add(user_id)
        lifecycleScope.launch {
            getProfileViewModel.getProfile(getProfileRequest(user_id = user_id_array))
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }
                        is DataResult.Success -> {
                            listItem.clear()
                            binding.clMain.visibility = View.VISIBLE
                            isUserProfilePaused =
                                it.data.data.account_settings?.pause_profile ?: false

//                            alertDialog?.dismiss()
                            binding.tvName.text =
                                it.data.data.name + "," + BaseUtils.convertAge(it.data.data)
                            name = it.data.data.name ?: ""
                            age = it.data.data.age ?: ""


                            it.data.data.about_me?.let {
                                binding.tvAboutDescription.text = it
                            }
                            if (binding.tvAboutDescription.text.equals("")) {
                                binding.tvAbout.visibility = View.GONE
                            } else {
                                binding.tvAbout.visibility = View.VISIBLE
                            }

                            BaseUtils.getListItem(it.data.data) { list ->
                                listItem.addAll(list)
                            }


                            val requestSent = it.data.data.like_status.request_sent
                            val requestReceive = it.data.data.like_status.request_receive
                            val match = it.data.data.like_status.is_match
                            val unMatch = it.data.data.like_status.is_unmatch
                            val isDeclined = it.data.data.like_status.is_decline
                            binding.ivSendRequest.visibility = View.VISIBLE
                            binding.ivCancelRequest.visibility = View.VISIBLE
                            when {
                                my_user_id == user_id -> {
                                    binding.ivSendRequest.visibility = View.GONE
                                    binding.ivCancelRequest.visibility = View.GONE
                                }

                                unMatch -> {
                                    api_type = UN_MATCH
                                }

                                match -> {
                                    api_type = MATCH
                                }

                                isDeclined -> {
                                    api_type = DECLINE
                                }

                                requestReceive -> {
                                    api_type = REQUEST_RECEIVE
                                }

                                requestSent -> {
                                    api_type = REQUEST_SENT
                                }

                                else -> {
                                    api_type = SEND
                                }

                            }
                            it.data.data.hobbies_interest?.apply {
                                if (isNotEmpty()) {
                                    binding.rvHobby.visibility = View.VISIBLE
                                    binding.tvHobby.visibility = View.VISIBLE
                                    HobbyItem = this
                                } else {
                                    binding.rvHobby.visibility = View.GONE
                                    binding.tvHobby.visibility = View.GONE
                                }
                            }

                            hobbyAdapter = HobbyAdapter(
                                HobbyItem,
                                requireActivity(), "1"
                            )
                            basiccommonadapter = BasicCommonAdapter(
                                listItem,
                                requireActivity()
                            )
                            val chipsLayoutManager =
                                ChipsLayoutManager.newBuilder(requireActivity())
                                    .setChildGravity(Gravity.TOP)
                                    .setScrollingEnabled(true)
                                    .setGravityResolver { Gravity.CENTER }
                                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                                    .build()
                            binding.rvHobby.layoutManager = chipsLayoutManager
                            binding.rvHobby.apply {
                                hobbyAdapter.setHasStableIds(true)
                                itemAnimator = DefaultItemAnimator()
                                adapter = hobbyAdapter
                                adapter?.notifyDataSetChanged()
                            }

                            BaseUtils.getWorkItem(it.data.data) { list ->
                                workItem.addAll(list)
                            }

                            binding.rvWork.visibility =
                                if (workItem.size == 0) View.GONE else View.VISIBLE
                            binding.tvWork.visibility =
                                if (workItem.size == 0) View.GONE else View.VISIBLE

                            hobbyWorkcommonadapter = BasicCommonAdapter(
                                workItem,
                                requireActivity()
                            )

                            binding.rvWork.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                itemAnimator = DefaultItemAnimator()
                                adapter = hobbyWorkcommonadapter
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
                            Image_list = it.data.data.images ?: ArrayList()

                            for (player in playerList.values) {
                                player.playWhenReady = false
                                player.release()
                            }
                            playerList.clear()

                            setImage(it.data.data.images ?: ArrayList())
                            it.data.data.images?.apply {
                                if (it.data.data.images.size > 6) {
                                    binding.moreView.clMoreImage.visibility = View.VISIBLE

                                    binding.moreView.tvMorePhoto.visibility = View.VISIBLE
                                    for (i in Image_list.indices) {
                                        if (i > 5) {
                                            newImageList.add(
                                                BaseUtils.getImageUrl(
                                                    requireActivity(),
                                                    Image_list[i]
                                                )
                                            )
                                        }
                                    }
                                    imageMoreAdapter = ImageMoreAdapter(
                                        newImageList,
                                        requireActivity()
                                    )

                                    binding.moreView.rvMoreImages.addOnPageChangedListener { visiblePosition ->
                                        itemPos = visiblePosition
                                        imageMoreAdapter.playPlayer(itemPos)
                                    }

                                    binding.moreView.rvMoreImages.apply {
                                        layoutManager = LinearLayoutManager(
                                            requireActivity(), LinearLayoutManager.HORIZONTAL, false
                                        )
                                        itemAnimator = DefaultItemAnimator()
                                        adapter = imageMoreAdapter
                                    }
                                    val snapHelper: SnapHelper = PagerSnapHelper()
                                    snapHelper.attachToRecyclerView(binding.moreView.rvMoreImages)
                                }
                            }
                        }
                        is DataResult.Failure -> {
                            binding.clMain.visibility = View.GONE
                            alertDialog?.dismiss()
                            alertDialog()


                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/get-profile",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/get-profile Api Error"))
                        }

                        else -> {}
                    }
                }

        }
    }

    private fun like_dislike() {

        lifecycleScope.launch {
            getRecommendationViewModel.sendRequest(
                LikeDislikeRequest(
                    user_id,
                    type,
                    Constants.IN_PERSON
                )
            )
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }
                        is DataResult.Success -> {
                            showNavigation(true)
                            findNavController().navigateUp()

                        }
                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/send-request",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/send-request Api Error"))
                        }

                        else -> {}
                    }
                }

        }
    }

    @ExperimentalCoroutinesApi
    private fun accept_reject() {

        lifecycleScope.launch {
            getRecommendationViewModel.acceptRequest(
                AcceptRequest(
                    user_id,
                    type,
                    message.trim(),
                    category
                )
            )
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {
                            CommonCode.setToast(requireContext(), "Loading")
                        }
                        is DataResult.Success -> {
                            alertDialog?.dismiss()
//                            chatViewModel.isDataAvailable = false
                            showNavigation(true)
                            findNavController().popBackStack()

                        }
                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/accept-request",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/accept-request Api Error"))
                        }

                        is DataResult.Empty -> {
                            CommonCode.setToast(requireContext(), "Empty")
                        }
                    }
                }

        }
    }


    private fun radarDialog() {
        val dialog = AlertDialog.Builder(requireActivity())

        val inflater = this.layoutInflater
        val view: View = inflater.inflate(R.layout.layout_scan_dialog, null)
        dialog.setView(view)
        val animationView: LottieAnimationView = view.findViewById(R.id.lottie_animation_view)

        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                alertDialog?.dismiss()
                binding.clParent.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

        alertDialog = dialog.create()
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()


        Handler(Looper.getMainLooper()).postDelayed({
            animationView.cancelAnimation()
        }, 5000)

        getProfile()

    }

    fun openBottomSheet() {
        val dialog = AlertDialog.Builder(requireActivity())

        val inflater = this.layoutInflater
        val view: View = inflater.inflate(R.layout.layout_accept_request, null)
        dialog.setView(view)
        val tv_name = view.findViewById<TextView>(R.id.tv_name)
        val et_message = view.findViewById<TextView>(R.id.et_message)
        val iv_send = view.findViewById<ImageView>(R.id.iv_send)
        val iv_view1 = view.findViewById<ImageView>(R.id.iv_view1)
        val tv_match = view.findViewById<TextView>(R.id.tv_match)
        val tv_cancel = view.findViewById<TextView>(R.id.tv_cancel)
        var progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        tv_name.text = name + "," + age
        tv_match.text = "Match with " + name
        tv_cancel.setOnClickListener {

            alertDialog?.dismiss()
        }
        Picasso.get()
            .load(image) // thumbnail url goes here
            .into(iv_view1, object : Callback {
                override fun onSuccess() {
                    progressBar.visibility = View.GONE
                    Picasso.get()
                        .load(image)
                        .into(iv_view1)
                }

                override fun onError(e: java.lang.Exception?) {

                }

            })
        tv_match.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            message = et_message.text.toString()
            accept_reject()

        }

        alertDialog = dialog.create()
        alertDialog?.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()

    }


    fun alertDialog() {
        val dialog = AlertDialog.Builder(requireActivity())
        val dialogBinding = DataBindingUtil.inflate<DialogNoUserfoundBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_no_userfound,
            null,
            false
        )
        dialogBinding.header = resources.getString(R.string.user_not)
        dialogBinding.buttonMessage = resources.getString(R.string.cancel)
        dialog.setView(dialogBinding.root)
        dialogBinding.clContinue.setOnClickListener {
            alertDialog?.dismiss()
            findNavController().popBackStack()
        }
        alertDialog = dialog.create()
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()

    }

    private fun stopAllPlayers() {
        for (player in playerList.values) {
            player.playWhenReady = false
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
//                    if (playerList.isEmpty() && this::imageMoreAdapter.isInitialized) return@scrollListener
                    when (it) {
                        6 -> {
                            stopAllPlayers()

                            try {
                                if (this::imageMoreAdapter.isInitialized)
                                    imageMoreAdapter.playPlayer(itemPos)
                            } catch (e: Exception) {

                            }
                        }
                        else -> playPlayer(it)
                    }
                })
        }
    }

    private fun playPlayer(position: Int) {
        try {
            if (this::imageMoreAdapter.isInitialized)
                imageMoreAdapter.stopAllPlayers()
        } catch (e: Exception) {

        }
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
    }

    private fun setImage(list: List<String>) {

        for (position in list.indices) {
            if (position >= 6) return
            getImageViewsWithProgress()[position].cardView.visibility = View.VISIBLE
            val image = BaseUtils.getImageUrl(requireActivity(), list[position])
            getImageViewsWithProgress()[position].ivVolume.visibility =
                if (!list[position].contains(Constants.MP4)) View.GONE else View.VISIBLE
            getImageViewsWithProgress()[position].videoView.visibility =
                if (!list[position].contains(Constants.MP4)) View.GONE else View.VISIBLE

            if (!list[position].contains(Constants.MP4)) {

                Glide.with(binding.root).load(image)
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
                Utility.zoomImage(requireActivity(), getImageViewsWithProgress()[position].ivImage)
            } else {
                initializePlayer(
                    Uri.parse(image),
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
    }

    override fun onPause() {
        super.onPause()
        stopPlayer()
    }

    private fun stopPlayer() {
        stopAllPlayers()
        try {
            imageMoreAdapter.stopAllPlayers()
        } catch (e: Exception) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPlayer()
        for (player in playerList.values) {
            player.playWhenReady = false
            player.release()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_left -> {
                if (itemPos != 0) {
                    itemPos -= 1
                    binding.moreView.rvMoreImages.scrollToPosition(itemPos)
                    imageMoreAdapter.playPlayer(itemPos)
                }
            }
            R.id.rl_right -> {
                if (itemPos < (newImageList.size - 1)) {
                    itemPos += 1
                    binding.moreView.rvMoreImages.scrollToPosition(itemPos)
                    imageMoreAdapter.playPlayer(itemPos)
                }
            }
        }
    }

    private fun openReportSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DataBindingUtil.inflate<LayoutBottomSheetUnmatchBinding>(
            LayoutInflater.from(requireContext()), R.layout.layout_bottom_sheet_unmatch, null, false
        )
        dialogBinding.isFromHome = true

        dialogBinding.tvReport.setOnClickListener {
            dialog.dismiss()
            openReportDialog { data, subOption, child, reason ->
                val reportRequest = ReportRequest(
                    user_id,
                    data._id,
                    suboption_id = subOption._id,
                    sub_suboption_id = child._id,
                    other_info = reason
                )
                Log.d("Request", "$reportRequest")
                reportUser(reportRequest)
            }
        }

        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()
    }


    private fun reportUser(request: ReportRequest) {
        if (view == null) return
        lifecycleScope.launch {
            getProfileViewModel.reportUser(request = request).observe(viewLifecycleOwner) {
                when (it) {
                    DataResult.Empty -> {
                    }
                    is DataResult.Failure -> {
                    }
                    DataResult.Loading -> {
                    }
                    is DataResult.Success -> {
                        if (it.statusCode == 200) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

}
