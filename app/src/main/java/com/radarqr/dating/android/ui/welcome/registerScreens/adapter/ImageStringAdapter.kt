package com.radarqr.dating.android.ui.welcome.registerScreens.adapter

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutProfileImageBinding
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.Utility.loadImage
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class ImageStringAdapter(
    var listItem: ArrayList<String>,
    val requireActivity: FragmentActivity, var tag: String,
    var progress_tag: String,
    internal var callback: ImageListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    lateinit var binding: LayoutProfileImageBinding

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_EMPTY = 1
    }

    var isDeleteInProgress = false
    /* set(value) {
         field = value
         refresh()
     }*/

    var size: Int = 6
        set(value) {
            field = value
            refresh()
        }

    fun refresh() {
        try {
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {

        }

    }

    private fun add(value: String) {
        listItem.add(value)
        notifyItemInserted(listItem.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSize() {
        size = if (listItem.size <= 6) {
            6
        } else {
            when (listItem.size % 3) {
                0 -> {
                    listItem.size
                }
                1 -> {
                    listItem.size + 2
                }

                2 -> {
                    listItem.size + 1
                }
                else -> {
                    listItem.size
                }
            }
        }
        refresh()
    }

    fun clear() {
        if (listItem.isNotEmpty()) {
            listItem.clear()
            refresh()
        }
    }

    fun remove(position: Int) {
        listItem.remove(listItem[position])
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, listItem.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_profile_image,
            parent,
            false
        )
        return if (viewType == VIEW_TYPE_ITEM) {
            ViewHolder(binding)
        } else {
            ViewHolderEmpty(binding)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            if (listItem.isNotEmpty())
                (holder as ImageStringAdapter.ViewHolder).bind(listItem[position])
        } else {
            (holder as ImageStringAdapter.ViewHolderEmpty).bind()
        }

    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int): Int {
        return if (position >= listItem.size) {
            VIEW_TYPE_EMPTY
        } else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int = size

    inner class ViewHolder(val binding: LayoutProfileImageBinding) :

        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {
            binding.ivClose1.visibility = View.VISIBLE
            binding.ivTransparent.visibility = View.VISIBLE
            binding.ivUser.visibility = View.GONE
            binding.ivVideo.visibility =
                if (listItem[absoluteAdapterPosition].contains(Constants.MP4) || listItem[absoluteAdapterPosition].contains(
                        Constants.THUMB
                    )
                ) View.VISIBLE else View.GONE

            binding.ivAdd1.visibility = View.GONE
            binding.ivView1.loadImage(value)
            /* binding.ivClose1.visibility =
                 if (absoluteAdapterPosition == 0) View.GONE else View.VISIBLE*/
            /* if (progress_tag == "1") {
                 progress_tag = ""
                 binding.progressBar1.visibility = View.VISIBLE
             } else {
                 binding.progressBar1.visibility = View.GONE
             }*/
            binding.root.setOnClickListener {
                callback.itemClick(listItem[absoluteAdapterPosition], absoluteAdapterPosition)
            }
            binding.ivClose1.setOnClickListener {
                if (BaseUtils.isInternetAvailable()) {
                    if (isDeleteInProgress) return@setOnClickListener
                    if (absoluteAdapterPosition == 0) {
                        isDeleteInProgress = true
                        binding.progressBar1.visibility = View.VISIBLE
                        callback.itemClick(
                            listItem[absoluteAdapterPosition],
                            absoluteAdapterPosition
                        )
                    } else
                        callback.removeImage(
                            listItem[absoluteAdapterPosition],
                            absoluteAdapterPosition,
                            binding.progressBar1
                        ) {
                            binding.progressBar1.visibility = View.GONE
                        }
                }
            }

        }
    }

    inner class ViewHolderEmpty(val binding: LayoutProfileImageBinding) :

        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.progressBar.visibility = View.GONE
            binding.progressBar1.visibility = View.GONE
            binding.root.setOnClickListener {
                callback.emptyClick(absoluteAdapterPosition)
            }
        }
    }


    interface ImageListener {
        fun emptyClick(position: Int)
        fun itemClick(url: String, pos: Int)
        fun removeImage(url: String, position: Int, view: View, isRemoved: () -> Unit)
    }

    fun getMimeType(context: Context, uri: Uri): String? {

        //Check uri format to avoid null
        val extension: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
        return extension
    }

}