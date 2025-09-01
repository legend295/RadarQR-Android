package com.radarqr.dating.android.ui.home.settings.adapter


import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutImageVideoViewBinding
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.Utility
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import java.util.*
import kotlin.collections.ArrayList


class ImageMoreAdapter(
    var list: ArrayList<String>,
    val requireActivity: Activity

) :
    RecyclerView.Adapter<ImageMoreAdapter.ViewHolder>() {
    private val playerList: TreeMap<Int, SimpleExoPlayer> = TreeMap()
    var player: SimpleExoPlayer? = null
    lateinit var errorException: (PlaybackException?) -> Unit
    var currentPosition: Int = 0
    lateinit var binding: LayoutImageVideoViewBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_image_video_view,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun getItemCount(): Int = list.size

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
//        playPlayer(holder.absoluteAdapterPosition)
        /* holder.binding.videoView.player?.let {
             it.playWhenReady = true
         }*/
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        /* holder.binding.videoView.player?.let {
             it.playWhenReady = false
         }*/
//        playPlayer(holder.absoluteAdapterPosition)
//        playPlayer(-1)
    }


    fun playPlayer(position: Int) {
        for (pos in list.indices) {
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

    fun stopAllPlayers() {
        BaseUtils.stopAllPlayers(playerList)
    }

    inner class ViewHolder(val binding: LayoutImageVideoViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(value: String) {
            binding.ivVolume.setOnClickListener {
                if (playerList[absoluteAdapterPosition]?.volume == 0f) {
                    playerList[absoluteAdapterPosition]?.volume = 100f
                    binding.ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[absoluteAdapterPosition]?.volume = 0f
                    binding.ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
            binding.cardView.visibility = View.VISIBLE
            binding.progressBarVideo.visibility =
                if (!value.contains(Constants.MP4)) View.GONE else View.VISIBLE

            if (value.contains(Constants.MP4)) {
                initializePlayer(
                    Uri.parse(value),
                    binding.videoView
                )

                playerList[absoluteAdapterPosition] = player!!
                playerList[absoluteAdapterPosition]?.addListener(
                    BaseUtils.playerListener(
                        absoluteAdapterPosition,
                        binding.progressBarVideo
                    ) { e -> errorException(e) }
                )

                binding.ivVolume.visibility = View.VISIBLE
                binding.ivImage.visibility = View.GONE
                binding.videoView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.ivVolume.visibility = View.GONE
                binding.ivImage.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
                Glide.with(binding.root).load(value).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.visibility = View.GONE
                        return false
                    }
                }).into(binding.ivImage)
            }

            Utility.zoomImage(requireActivity, binding.ivImage)
        }

        private fun initializePlayer(uri: Uri, videoView: PlayerView) {
            player = SimpleExoPlayer.Builder(requireActivity)
                .build()
                .also { exoPlayer ->
//                isMute = true
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                    videoView.player = exoPlayer
                    val mediaItem = MediaItem.fromUri(uri)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            player!!.volume = 0f

            player!!.playWhenReady = true

        }
    }
}