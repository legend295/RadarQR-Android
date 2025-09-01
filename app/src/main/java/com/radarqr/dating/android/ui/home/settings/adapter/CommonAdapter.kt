package com.radarqr.dating.android.ui.home.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.ui.home.settings.model.CommonModel


class CommonAdapter(
    private val ListItem: ArrayList<CommonModel>,
    internal var multi_value: Int,
    internal var value: String,
    val requireActivity: FragmentActivity,
    internal var callback: ItemCallback
) :
    RecyclerView.Adapter<CommonAdapter.ViewHolder>() {
    var selcted_position = 0
    var first = 0
    var click = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val listItem: View = layoutInflater.inflate(
            R.layout.layout_common_item,
            parent, false
        )
        return ViewHolder(listItem)


    }

    interface ItemCallback {
        fun onItemClick(name: String, type: String, sent_value: String)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CommonModel = ListItem[position]

        /*holder.itemView.tv_option3.text = item.name
        if (!item.sent_value.equals("")) {
            if (value.equals(item.sent_value, ignoreCase = true)) {
                holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                holder.itemView.tv_option3.setTextColor(
                    ContextCompat.getColor(
                        requireActivity,
                        R.color.white
                    )
                )
            } else {
                if (first != 0) {

                    if (selcted_position == position) {
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.white
                            )
                        )
                    } else {
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.rect_white_age)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.black
                            )
                        )
                    }

                }
            }
        } else {
            if (multi_value == 1) {
                if (click == 1) {
                    if (selcted_position != holder.adapterPosition) {
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.rect_white_age)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.black
                            )
                        )
                    } else {
                        if (item.isSelected) {
                            holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                            holder.itemView.tv_option3.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity,
                                    R.color.white
                                )
                            )
                        } else {
                            holder.itemView.tv_option3.setBackgroundResource(R.drawable.rect_white_age)
                            holder.itemView.tv_option3.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity,
                                    R.color.black
                                )
                            )
                        }
                    }
                } else {
                    if (item.isSelected) {
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.white
                            )
                        )
                    } else {
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.rect_white_age)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.black
                            )
                        )
                    }
                }
            } else {
                if (value.equals(item.name,ignoreCase = true)) {
                    holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                    holder.itemView.tv_option3.setTextColor(
                        ContextCompat.getColor(
                            requireActivity,
                            R.color.white
                        )
                    )
                } else {
                    if (first != 0) {
                        if (selcted_position == position) {
                            holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                            holder.itemView.tv_option3.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity,
                                    R.color.white
                                )
                            )
                        } else {
                            holder.itemView.tv_option3.setBackgroundResource(R.drawable.rect_white_age)
                            holder.itemView.tv_option3.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity,
                                    R.color.black
                                )
                            )
                        }
                    }
                }
            }
        }
        holder.itemView.setOnClickListener {
            first = 1
            if (multi_value == 0) {
                value = ""
                selcted_position = holder.adapterPosition

                callback.onItemClick(item.name, item.type, item.sent_value!!)
                notifyDataSetChanged()
            } else {
                if (item.name.equals("Open to all")) {
                    click = 1
                    selcted_position = holder.adapterPosition
                    for (i in 0 until ListItem.size) {
                        ListItem[i].isSelected = false
                    }

                    item.isSelected = true
                    callback.onItemClick(item.name, item.type, item.sent_value!!)
                    notifyDataSetChanged()
                } else {
                    value = ""

                    selcted_position = holder.adapterPosition
                    if (item.isSelected == false) {
                        item.isSelected = true
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.green_fill_rect)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.white
                            )
                        )
                    } else {
                        item.isSelected = false
                        holder.itemView.tv_option3.setBackgroundResource(R.drawable.rect_white_age)
                        holder.itemView.tv_option3.setTextColor(
                            ContextCompat.getColor(
                                requireActivity,
                                R.color.black
                            )
                        )
                    }
                    if (ListItem[ListItem.size - 1].isSelected) {
                        ListItem[ListItem.size - 1].isSelected = false
                        notifyDataSetChanged()
                    }
                }
            }
            callback.onItemClick(item.name, item.type, item.sent_value!!)
        }*/
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun getItemCount(): Int = ListItem.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}