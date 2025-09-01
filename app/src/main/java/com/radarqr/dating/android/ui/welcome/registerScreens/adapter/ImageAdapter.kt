package com.radarqr.dating.android.ui.welcome.registerScreens.adapter

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class ImageAdapter(
    var listItem: ArrayList<String>,
    val requireActivity: FragmentActivity, var tag: String,
    var progress_tag: String,
    internal var callback: ImageListener
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    var size: Int = 6
        set(value) {
            field = value
            refresh()
        }


    fun refresh() {
        notifyDataSetChanged()
    }


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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val listItem: View = layoutInflater.inflate(
            R.layout.layout_profile_image,
            parent, false
        )
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*holder.itemView.iv_view1.setOnClickListener {
            callback.Item_Click(position)
        }
        holder.itemView.iv_close1.setOnClickListener {
            holder.itemView.progress_bar1.visibility = View.VISIBLE
            listItem.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, listItem.size)
            callback.remove_image(position)
        }

        try {

            Glide.with(holder.itemView.iv_view1).load(listItem[position]).into(holder.itemView.iv_view1)
            var type = *//*getMimeType(requireActivity, listItem.get(position))*//*""
            if (type.equals("jpg") || type.equals("jpeg") || type.equals("png") || type.equals("webp") || type.equals(
                    "mp4"
                )
            ) {
                if (position == 0) {
                    holder.itemView.iv_close1.visibility = View.GONE
                    holder.itemView.iv_add1.visibility = View.GONE
                } else {
                    holder.itemView.iv_close1.visibility = View.VISIBLE
                    holder.itemView.iv_add1.visibility = View.GONE
                }


            } else {
                holder.itemView.iv_close1.visibility = View.GONE
                holder.itemView.iv_add1.visibility = View.VISIBLE
            }


            if (progress_tag.equals("1")) {
                progress_tag = ""
                holder.itemView.iv_view1.isEnabled = false
                holder.itemView.progress_bar1.visibility = View.VISIBLE
            } else {

                holder.itemView.iv_view1.isEnabled = false
                holder.itemView.progress_bar1.visibility = View.GONE
                holder.itemView.iv_view1.isEnabled = true

            }
        } catch (e: IndexOutOfBoundsException) {

        }*/
    }


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun getItemCount(): Int = if (listItem.size <= 15) size else listItem.size

   /* fun add(uri: Uri) {
        list.add(uri)
        notifyItemInserted(list.size - 1)
    }*/

    fun replace(position: Int) {

    }

    /*fun remove(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }*/

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    interface ImageListener {
        fun Item_Click(position: Int)
        fun remove_image(position: Int)
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String?

        //Check uri format to avoid null
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
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