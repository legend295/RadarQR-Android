package com.radarqr.dating.android.ui.home.main.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.profile.BasicInfoData
import com.radarqr.dating.android.databinding.LayoutHomeBinding
import com.radarqr.dating.android.databinding.LayoutImageVideoViewBinding
import com.radarqr.dating.android.ui.home.settings.adapter.HobbyAdapter
import com.radarqr.dating.android.ui.home.settings.adapter.ImageMoreAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.addOnPageChangedListener
import com.radarqr.dating.android.utility.S3Utils
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.handler.RecyclerViewClickHandler
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import java.util.*


class HomeAdapter(
    private val userList: List<ProfileData>,
    internal var requireActivity: Activity,
    val clickHandler: RecyclerViewClickHandler<View, Int, ProfileData>
) :
    RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {
    private var player: SimpleExoPlayer? = null
    var newImageList: ArrayList<String> = ArrayList()
    lateinit var imageMoreAdapter: ImageMoreAdapter
    lateinit var hobbyAdapter: HobbyAdapter
    private lateinit var hobbyWorkCommonAdapter: BasicCommonAdapter
    var layoutInflater: LayoutInflater? = null
    lateinit var basicAdapter: BasicCommonAdapter
    var binding: LayoutHomeBinding? = null
    var itemPos = 0
    val playerList: TreeMap<Int, SimpleExoPlayer> = TreeMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.HomeViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(requireActivity)
        }
        binding =
            DataBindingUtil.inflate(layoutInflater!!, R.layout.layout_home, parent, false)
        return HomeViewHolder(binding!!)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HomeAdapter.HomeViewHolder, position: Int) {
        /*if (userList.isNotEmpty()) {
            val item = userList[position]

            holder.binding.tvName.text = item.name + "," + BaseUtils.convertAge(item)
            var hobbyItem: List<HobbiesData> = ArrayList()
            val workItem: ArrayList<BasicInfoData> = ArrayList()
            var imageList: List<String> = ArrayList()
            val listItem: ArrayList<BasicInfoData> = ArrayList()
            item.about_me.let {
                holder.binding.tvAboutDescription.text = it
            }
            if (holder.binding.tvAboutDescription.text.equals("")) {
                holder.binding.tvAbout.visibility = View.GONE
            } else {
                holder.binding.tvAbout.visibility = View.VISIBLE
            }


            BaseUtils.getListItem(item) { list ->
                listItem.addAll(list)
            }

            holder.binding.ivOptions.setOnClickListener {
                clickHandler.onClick(it, position, item)
            }
            item.hobbies_interest?.apply {
                if (isNotEmpty()) {
                    holder.binding.rvHobby.visibility = View.VISIBLE
                    holder.binding.tvHobby.visibility = View.VISIBLE
                    hobbyItem = this
                } else {
                    holder.binding.rvHobby.visibility = View.GONE
                    holder.binding.tvHobby.visibility = View.GONE
                }
            }

            val chipsLayoutManager =
                ChipsLayoutManager.newBuilder(requireActivity)
                    .setChildGravity(Gravity.TOP)
                    .setScrollingEnabled(true)
                    .setGravityResolver { Gravity.CENTER }
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()

            holder.binding.rvHobby.layoutManager = chipsLayoutManager


            hobbyAdapter = HobbyAdapter(
                hobbyItem,
                requireActivity, "1"
            )
            holder.itemView.rv_hobby?.apply {
                itemAnimator = DefaultItemAnimator()
                adapter = hobbyAdapter
                hobbyAdapter.notifyDataSetChanged()
            }
            BaseUtils.getWorkItem(item) { list ->
                workItem.addAll(list)
            }

            holder.itemView.rv_work.visibility = if (workItem.isEmpty()) View.GONE else View.VISIBLE
            holder.itemView.tv_work.visibility = if (workItem.isEmpty()) View.GONE else View.VISIBLE

            hobbyWorkCommonAdapter = BasicCommonAdapter(
                workItem,
                requireActivity
            )

            holder.itemView.rv_work.apply {
                layoutManager = LinearLayoutManager(requireActivity)
                itemAnimator = DefaultItemAnimator()
                adapter = hobbyWorkCommonAdapter
                adapter?.notifyDataSetChanged()
            }
            val chipsLayoutManager2 =
                ChipsLayoutManager.newBuilder(requireActivity)
                    .setChildGravity(Gravity.TOP)
                    .setScrollingEnabled(true)
                    .setGravityResolver { Gravity.CENTER }
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()

            holder.itemView.rv_basic.visibility =
                if (listItem.size == 0) View.GONE else View.VISIBLE
            holder.itemView.tv_basic.visibility =
                if (listItem.size == 0) View.GONE else View.VISIBLE

            holder.itemView.rv_basic.layoutManager = chipsLayoutManager2
            basicAdapter = BasicCommonAdapter(
                listItem,
                requireActivity
            )

            holder.itemView.rv_basic.apply {

                setItemViewCacheSize(listItem.size)
                setHasFixedSize(true)

                itemAnimator = DefaultItemAnimator()
                adapter = basicAdapter
                adapter?.notifyDataSetChanged()
            }
            imageList = item.images ?: ArrayList()

            for (player in playerList.values) {
                player.playWhenReady = false
                player.release()
            }
            playerList.clear()

            setImage(imageList)
            if (imageList.size > 6) {
                binding?.let { binding ->
                    binding.moreView.clMoreImage.visibility = View.VISIBLE
                    binding.moreView.tvMorePhoto.visibility = View.VISIBLE
                    for (i in imageList.indices) {
                        if (i > 5) {
                            newImageList.add(getImageUrl(imageList[i]))
                        }
                    }
                    imageMoreAdapter = ImageMoreAdapter(
                        newImageList,
                        requireActivity
                    )
                    binding.moreView.rvMoreImages.addOnPageChangedListener { visiblePosition ->
                        itemPos = visiblePosition
                        imageMoreAdapter.playPlayer(itemPos)
                    }

                    binding.moreView.rlLeft.setOnClickListener {
                        if (itemPos != 0) {
                            itemPos -= 1
                            binding.moreView.rvMoreImages.scrollToPosition(itemPos)
                            imageMoreAdapter.playPlayer(itemPos)
                        }
                    }
                    binding.moreView.rlRight.setOnClickListener {
                        if (itemPos < (newImageList.size - 1)) {
                            itemPos += 1
                            binding.moreView.rvMoreImages.scrollToPosition(itemPos)
                            imageMoreAdapter.playPlayer(itemPos)
                        }
                    }

                    binding.moreView.rvMoreImages.apply {
                        layoutManager = LinearLayoutManager(
                            requireActivity, LinearLayoutManager.HORIZONTAL, false
                        )
                        itemAnimator = DefaultItemAnimator()
                        adapter = imageMoreAdapter
                    }
                }

            }
        }
*/

    }

    fun isImageAdapterInitialized(): Boolean = this::imageMoreAdapter.isInitialized

    private fun getImageUrl(imageName: String): String {
        val urlFromS3 = S3Utils.generatesShareUrl(
            requireActivity, imageName
        )

        return urlFromS3.replace(" ", "%20")
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int = 1//user_list.size


    inner class HomeViewHolder(var binding: LayoutHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {

        }

    }

    fun stopAllPlayers() {
        /* try {
             imageMoreAdapter.player?.let {
                 it.playWhenReady = false
             }
         } catch (e: Exception) {

         }*/
        for (player in playerList.values) {
            player.playWhenReady = false
        }
    }

    fun releasePlayer() {
        for (player in playerList.values) {
            player.playWhenReady = false
            player.release()
        }
    }

    fun playPlayer(position: Int) {
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
        try {
            for (position in list.indices) {
                if (position >= 6) return
                getImageViewsWithProgress()[position].cardView.visibility = View.VISIBLE

                val urlFromS3 = S3Utils.generatesShareUrl(
                    requireActivity, list[position]
                )
                val image = getImageUrl(list[position])
                if (!list[position].contains(Constants.MP4)) {
                    getImageViewsWithProgress()[position].videoView.visibility = View.GONE
                    getImageViewsWithProgress()[position].ivVolume.visibility = View.GONE
                    binding?.let {
                        Glide.with(it.root).load(image)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    getImageViewsWithProgress()[position].progressBar.visibility =
                                        View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    getImageViewsWithProgress()[position].progressBar.visibility =
                                        View.GONE
                                    return false
                                }
                            }).into(getImageViewsWithProgress()[position].ivImage)
                    }
                    Utility.zoomImage(
                        requireActivity,
                        getImageViewsWithProgress()[position].ivImage
                    )
                } else {
                    initializePlayer(
                        Uri.parse(image),
                        getImageViewsWithProgress()[position].videoView,
                        position
                    )
                    getImageViewsWithProgress()[position].ivVolume.visibility = View.VISIBLE
                    getImageViewsWithProgress()[position].progressBar.visibility = View.GONE
                }
            }
        } catch (e: Exception) {

        }
    }

    fun getImageViewsWithProgress(): ArrayList<LayoutImageVideoViewBinding> {
        val list = ArrayList<LayoutImageVideoViewBinding>()
        binding?.let { binding ->
            list.add(binding.firstView)
            list.add(binding.secondView)
            list.add(binding.thirdView)
            list.add(binding.forthView)
            list.add(binding.fifthView)
            list.add(binding.sixthView)
        }
        BaseUtils.handleClick(list, playerList)
        return list
    }

    private fun initializePlayer(uri: Uri, videoView: PlayerView, position: Int) {
        player = SimpleExoPlayer.Builder(requireActivity)
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


}