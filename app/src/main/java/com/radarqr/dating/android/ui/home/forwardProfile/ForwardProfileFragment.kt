package com.radarqr.dating.android.ui.home.forwardProfile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.LayoutForwordProfileBinding
import com.radarqr.dating.android.ui.home.main.recommended.RecommendedFragment.Companion.nestedScrollView
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.isPackageInstalled
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.radarqr.dating.android.utility.S3Utils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class ForwardProfileFragment : BaseFragment<LayoutForwordProfileBinding>() {
    var user_id = ""
    var user_id_array: ArrayList<String> = ArrayList()
    var Image_list: List<String> = ArrayList()
    var name = ""
    var myUserId = ""
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.forwardProfileFragment = this
        showToolbarLayout(false)
        showNavigation(false)
        val data: Bundle? = arguments

        runBlocking {
            myUserId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""

        }

        user_id = data?.getString("user_id")!!
        getProfile()

        binding.rlEmailMsg.setOnClickListener {

            if (isPackageInstalled("com.google.android.gm", requireActivity().packageManager)) {
                val intent2 = Intent()
                intent2.action = Intent.ACTION_SEND
                intent2.type = "message/rfc822"
                intent2.`package` = "com.google.android.gm"
                intent2.putExtra(Intent.EXTRA_SUBJECT, "Check Out $name On RadarQR")
                intent2.putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out $name on RadarQR. Click on the link to view the profile ${
                        RaddarApp.getEnvironment().getShareUrl()
                    }$user_id"
                )
                shareBitmap(intent2)
                requireActivity().startActivity(Intent.createChooser(intent2, "Send mail"))
            } else CommonCode.setToast(
                requireContext(),
                "You don't have any application to use this feature."
            )
        }

        binding.rlTextMsg.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.data = Uri.parse("sms:")
//            smsIntent.type = "vnd.android-dir/mms-sms"
            smsIntent.putExtra(
                "sms_body",
                "Check out $name on RadarQR. Click on the link to view the profile ${
                    RaddarApp.getEnvironment().getShareUrl()
                }$user_id"
            )
            startActivity(smsIntent)
        }

        binding.activityToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(
            v.measuredWidth,
            v.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        v.draw(c)
        return b
    }

    private fun shareBitmap(intent: Intent) {
        if (nestedScrollView == null) {
            return
        }
        val bitmap = loadBitmapFromView(nestedScrollView!!)
        val cachePath = File(requireActivity().externalCacheDir, "shared_images/");
        cachePath.mkdirs()

        //create png file
        val file = File(cachePath, "${name}_${System.currentTimeMillis()}.png");
        val fileOutputStream: FileOutputStream?
        try {
            fileOutputStream = FileOutputStream(file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap?.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 20, fileOutputStream)
            } else {
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, fileOutputStream)
            }
            fileOutputStream.flush()
            fileOutputStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace();
        } catch (e: IOException) {
            e.printStackTrace();
        }

        val uri = FileProvider.getUriForFile(
            requireActivity(),
            requireActivity().applicationContext.packageName + ".provider",
            file
        )
        intent.putExtra(Intent.EXTRA_STREAM, uri)
    }


    override fun getLayoutRes(): Int = R.layout.layout_forword_profile

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
                            if (it.data.data.name != null || !it.data.data.name.equals("")) {
                                if (it.data.data.age != null || !it.data.data.age.equals("")) {
                                    name = it.data.data.name ?: ""
                                    binding.tvName.text =
                                        it.data.data.name.toString() + " , " + it.data.data.age.toString()
                                }
                            }
                            it.data.data.images?.apply {
                                if (size != 0) {
                                    setImage(
                                        binding.ivImage1,
                                        image_list = this,
                                        0,
                                        binding.progressBar1
                                    )
                                }
                            }


                        }
                        is DataResult.Failure -> {
                            binding.clMain.visibility = View.GONE

                        }

                        else -> {}
                    }
                }

        }
    }

    fun setImage(iv_view: ImageView, image_list: List<String>, pos: Int, progressBar: ProgressBar) {
        var urlFromS3 = S3Utils.generatesShareUrl(
            requireActivity(), image_list.get(
                pos
            ) ?: ""
        )


        var Image = urlFromS3.replace(" ", "%20")

        Picasso.get()
            .load(Image) // thumbnail url goes here
            .into(iv_view, object : Callback {
                override fun onSuccess() {
                    progressBar.visibility = View.GONE
                    Picasso.get()
                        .load(Image)
                        .into(iv_view)
                }

                override fun onError(e: java.lang.Exception?) {

                }

            })
    }

    private fun loadImage(url: String) {
        val image = BaseUtils.getImageUrl(requireContext(), url)
        Glide.with(binding.root).load(image).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                binding.progressBar1.visibility = View.GONE
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                binding.progressBar1.visibility = View.GONE
                return true
            }
        }).into(binding.ivImage1)
    }

}
