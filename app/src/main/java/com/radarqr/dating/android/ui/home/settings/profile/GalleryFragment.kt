package com.radarqr.dating.android.ui.home.settings.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.*
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentGalleryBinding
import com.radarqr.dating.android.databinding.LayoutGalleryItemBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.utility.Utility.getImageUrl
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.serializable

class GalleryFragment : BaseFragment<FragmentGalleryBinding>() {

    private var profileData: ProfileData? = null
    private var player: SimpleExoPlayer? = null
    private var adapter: GalleryAdapter? = null
    private var imagesList = ArrayList<String>()
    var layoutBinding: LayoutGalleryItemBinding? = null
    private var imageSelectedPosition = 0

    override fun getLayoutRes(): Int = R.layout.fragment_gallery

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            imageSelectedPosition = getInt(Constants.POSITION, 0)
            profileData = serializable(Constants.EXTRA_DATA) as ProfileData?
            profileData?.apply {
                imagesList = images ?: ArrayList()
                /*imagesList.clear()
                if (imageDataMap.isNotEmpty()) {
                    imageDataMap.forEach {
                        imagesList.add(it.value.url)
                    }
                } else {
                    images?.forEach {
                        imagesList.add(requireContext().getImageUrl(it))
                    }
                }*/
            }
        }

        binding.ivBack.setOnClickListener {
            this.view?.findNavController()?.popBackStack()
        }



        adapter = GalleryAdapter(imagesList, requireActivity()) { data, layoutBinding ->
            val url = if (RaddarApp.imagesMap.containsKey(data)) RaddarApp.imagesMap[data]
            else requireContext().getImageUrl(data)

            this.layoutBinding = layoutBinding
            layoutBinding.ivItem.visible(isVisible = false)
            layoutBinding.videoView.visible(isVisible = true)
            layoutBinding.ivPlay.visible(isVisible = false)
            this@GalleryFragment.layoutBinding?.progressBar?.visible(isVisible = true)

            player = SimpleExoPlayer.Builder(requireContext()).build()
            player?.apply {
                repeatMode = Player.REPEAT_MODE_OFF
                layoutBinding.videoView.player = player
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                setMediaItem(mediaItem)
                prepare()
                play()
                playWhenReady = true
            }

            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_BUFFERING -> {
                            this@GalleryFragment.layoutBinding?.progressBar?.visible(isVisible = true)
                        }
                        ExoPlayer.STATE_READY -> {
                            this@GalleryFragment.layoutBinding?.progressBar?.visible(isVisible = false)
                        }
                        ExoPlayer.STATE_ENDED -> {
                            player?.apply {
                                if (playWhenReady) {
                                    this@GalleryFragment.layoutBinding?.ivItem?.visible(isVisible = true)
                                    this@GalleryFragment.layoutBinding?.videoView?.visible(isVisible = false)
                                    this@GalleryFragment.layoutBinding?.ivPlay?.visible(isVisible = true)
                                    this@GalleryFragment.layoutBinding?.progressBar?.visible(
                                        isVisible = false
                                    )
                                    playWhenReady = false
                                    release()
                                }
                            }
                        }
                        else -> {

                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Log.d("VIDEO_PLAY_ERROR", " onPlayerError $error")
                }

                override fun onPlayerErrorChanged(error: PlaybackException?) {
                    super.onPlayerErrorChanged(error)
                    Log.d("VIDEO_PLAY_ERROR", " onPlayerErrorChanged $error")
                }
            })
        }
        binding.viewPagerImages.adapter = adapter
        binding.viewPagerImages.setCurrentItem(imageSelectedPosition, false)

        binding.viewPagerImages.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                player?.apply {
                    if (playWhenReady) {
                        layoutBinding?.ivItem?.visible(isVisible = true)
                        layoutBinding?.videoView?.visible(isVisible = false)
                        layoutBinding?.ivPlay?.visible(isVisible = true)
                        this@GalleryFragment.layoutBinding?.progressBar?.visible(isVisible = false)
                        playWhenReady = false
                        release()
                    }
                }
            }
        })

    }


    override fun onPause() {
        super.onPause()
        player?.apply {
            if (playWhenReady) {
                layoutBinding?.ivItem?.visible(isVisible = true)
                layoutBinding?.videoView?.visible(isVisible = false)
                layoutBinding?.ivPlay?.visible(isVisible = true)
                playWhenReady = false
                release()
            }
        }
    }
}