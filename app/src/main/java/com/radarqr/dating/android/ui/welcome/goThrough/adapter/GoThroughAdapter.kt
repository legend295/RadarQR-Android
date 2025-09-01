package com.radarqr.dating.android.ui.welcome.goThrough.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.LayoutWelcomeItemBinding
import com.radarqr.dating.android.ui.welcome.goThrough.GoThroughModel


class GoThroughAdapter(

    private val screen_list: ArrayList<GoThroughModel>//SettingModel.DataBean.TutorialItem>,
) : RecyclerView.Adapter<GoThroughAdapter.View_holder>() {

    private lateinit var mContext: Context


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GoThroughAdapter.View_holder {
        val binding: LayoutWelcomeItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_welcome_item,
            parent,
            false
        )


        mContext = parent.context

        return View_holder(binding.root)
    }

    override fun getItemCount(): Int {
        return screen_list.count()
    }

    override fun onBindViewHolder(holder: GoThroughAdapter.View_holder, position: Int) {

        holder.ivInstruction?.let {
            Glide.with(mContext).load(screen_list[position].image).into(it)
        }


        holder.tvTitle?.text = screen_list[position].title
        holder.tvBody?.text = screen_list[position].description
    }


    interface InstructionCallback {
        fun onNextButton(position: Int)
    }

    inner class View_holder(root: View) : RecyclerView.ViewHolder(root) {

        var ivInstruction: ImageView? = root.findViewById(R.id.iv_image)
        var tvTitle: TextView? = root.findViewById(R.id.tv_title)
        var tvBody: TextView? = root.findViewById(R.id.tv_sub_title)
    }

}