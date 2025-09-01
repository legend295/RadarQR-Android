package com.radarqr.dating.android.ui.home.likes.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.ui.home.likes.model.UserLikes
import com.radarqr.dating.android.utility.S3Utils


class LikesAdapter(
    private val ListItem: MutableList<UserLikes>,
    val requireActivity: FragmentActivity,
    internal var callback: ItemclickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val LOADING = 0
    private val ITEM = 1
    private var isLoadingAdded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var viewHolder: RecyclerView.ViewHolder? = null

        when (viewType) {
            ITEM -> {
                val listItem: View = layoutInflater.inflate(
                    R.layout.layout_item_like,
                    parent, false
                )


                viewHolder = ViewHolder(listItem)
            }
            LOADING -> {
                val viewLoading: View = layoutInflater.inflate(
                    R.layout.item_progress,
                    parent,
                    false
                )
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!


    }

    override fun onBindViewHolder(holder1: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> {
                val holder: ViewHolder = holder1 as ViewHolder
                if (ListItem.get(position).sender_id.equals("")) {
                    holder.iv_outline!!.visibility = View.VISIBLE
                    holder.cl_top!!.visibility = View.GONE
                } else {
                    holder.iv_outline!!.visibility = View.GONE
                    holder.cl_top!!.visibility = View.VISIBLE

                    holder.tv_name!!.text =
                        ListItem.get(position).user_detail!!.name.toString() + "," + ListItem.get(
                            position
                        ).user_detail!!.age.toString()
                    setImage(
                        holder.iv_view1!!,
                        ListItem.get(position).user_detail!!.profile_pic,
                        position
                    )

                }
                holder.itemView.setOnClickListener {
                    callback.Item_Click(
                        ListItem.get(position).sender_id!!,
                        ListItem[position].category ?: ""
                    )
                }
            }
            LOADING -> {
                val loadingViewHolder = holder1 as LoadingViewHolder
                loadingViewHolder.progressBar.visibility = View.VISIBLE
            }
        }

    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return ListItem.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == ListItem.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        notifyDataSetChanged()
        /*add(UserLikes()!!)*/
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        notifyDataSetChanged()

       /* val position: Int = ListItem.size - 1
        val result = getItem(position)
        if (result != null) {
            ListItem.removeAt(position)
            notifyItemRemoved(position)
        }*/
    }

    fun add(movie: UserLikes?) {
        ListItem.add(movie!!)
        notifyItemInserted(ListItem.size - 1)
    }

    fun addAll(moveResults: List<UserLikes?>) {
        for (result in moveResults) {
            add(result)
        }
    }

    fun getItem(position: Int): UserLikes? {
        return ListItem.get(position)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public var tv_name: TextView? = null
        public var iv_outline: ImageView? = null
        public var cl_top: ConstraintLayout? = null
        public var iv_view1: ImageView? = null

        init {
            cl_top = itemView.findViewById<View>(R.id.cl_top) as ConstraintLayout
            tv_name = itemView.findViewById<View>(R.id.tv_name) as TextView
            iv_view1 = itemView.findViewById<View>(R.id.iv_view1) as ImageView
            iv_outline = itemView.findViewById<View>(R.id.iv_outline) as ImageView
        }
    }


    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public var progressBar: ProgressBar

        init {
            progressBar = itemView.findViewById<View>(R.id.progress_bar1) as ProgressBar
        }
    }

    fun setImage(iv_view: ImageView, image_list: String, pos: Int) {
        var urlFromS3 = S3Utils.generatesShareUrl(
            requireActivity, image_list
        )

        var Image = urlFromS3.replace(" ", "%20")

        Glide.with(requireActivity).load(Image).into(iv_view)
    }

    interface ItemclickListener {
        fun Item_Click(user_id: String, category: String)

    }
}