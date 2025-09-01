package com.radarqr.dating.android.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.LayoutReportItemBinding
import android.view.animation.AnimationUtils
import com.radarqr.dating.android.data.model.report.Data
import com.radarqr.dating.android.data.model.report.SubOption
import com.radarqr.dating.android.data.model.report.SubSuboption


class ReportAdapter<T>(var context: Context, val reportType: ReportType) :
    RecyclerView.Adapter<ReportAdapter<T>.ViewHolder>() {

    lateinit var clickHandler: (Int, ReportType, ArrayList<T>) -> Unit

    private val list: ArrayList<T> = ArrayList()

    inner class ViewHolder(var binding: LayoutReportItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvReport.setOnClickListener {
                clickHandler.invoke(adapterPosition, reportType, list)
//                setFadeAnimation(binding.root)
            }
            when (reportType) {
                ReportType.MENU -> {
                    val data = list[adapterPosition] as Data
                    binding.tvReport.text = data.option
                }

                ReportType.SUB_MENU -> {
                    val data = list[adapterPosition] as SubOption
                    binding.tvReport.text = data.value
                }

                ReportType.CHILD -> {
                    val data = list[adapterPosition] as SubSuboption
                    binding.tvReport.text = data.value
                }
                ReportType.EDIT -> {
                }
            }

        }
    }

    private fun setFadeAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
        view.startAnimation(animation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: LayoutReportItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_report_item,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()

    }

    override fun getItemCount(): Int {
        Log.d("LIST_SIZE", "${list.size}")
        return list.size
    }

    private fun add(data: T) {
        list.add(data)
        notifyItemInserted(list.size - 1)
    }

    fun addAll(list: ArrayList<T>) {
        for (value in list) {
            add(value)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear(isCleared: (Boolean) -> Unit) {
        if (list.isNotEmpty()) {
            list.clear()
            notifyDataSetChanged()
        }
        isCleared(true)
    }
}