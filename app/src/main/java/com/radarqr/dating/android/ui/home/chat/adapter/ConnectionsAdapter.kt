package com.radarqr.dating.android.ui.home.chat.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.databinding.LayoutConnectionsItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.ui.home.chat.ChatUserFragment.Companion.USER_DATA
import com.radarqr.dating.android.ui.home.quickBlox.model.MatchedData
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible
import java.text.SimpleDateFormat
import java.util.*

class ConnectionsAdapter(
    val list: ArrayList<QBChatDialog?>,
    val quickBloxManager: QuickBloxManager,
    val clickHandler: (QBChatDialog, Int) -> Unit,
    val popUpClickHandler: (QBChatDialog, Int, Int,String) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM = 0
        const val ITEM_LOADING = 1
        const val DATE_FORMAT = "MM/dd/yyyy"
        const val REMOVE_FROM_FAVORITES="Remove from favorites"
    }

    inner class ConnectionViewHolder(val binding: LayoutConnectionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            var isfavourite = false
            val userData = data.customData?.get(USER_DATA) as MatchedData?
            val favourite = data.customData?.get(QuickBloxManager.CUSTOM_KEY_FAVOURITE)

            val favouriteMarkedBy: ArrayList<String>? = try {
                data.customData?.get(QuickBloxManager.CUSTOM_KEY_FAVOURITE_MARKED_BY) as ArrayList<String>?
            } catch (e: ClassCastException) {
                null
            }

            favouriteMarkedBy?.let {
                val drawable: Int
                if (it.contains(quickBloxManager.qbSessionManager.sessionParameters?.userId?.toString()?:"")) {
                    isfavourite = true
                    drawable = R.drawable.ic_favourite
                    binding.ivFavourite.visible(isVisible = true)
                } else {
                    isfavourite = false
                    drawable = R.drawable.ic_non_favourite
                    binding.ivFavourite.visible(isVisible = false)
                }

                binding.ivFavourite.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        drawable
                    )
                )

            } ?: kotlin.run {
                isfavourite = false
                binding.ivFavourite.visible(isVisible = false)
                binding.ivFavourite.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_non_favourite
                    )
                )
            }

            when (data.lastMessageUserId) {
                null -> {
                    binding.tvMoveCount.visible(isVisible = true)
                    binding.tvMoveCount.text = binding.root.context.getString(R.string.newTxt)
                }
                quickBloxManager.getOtherUserId(data)?.toInt() -> {
                    binding.tvMoveCount.visible(isVisible = true)
                    binding.tvMoveCount.text = binding.root.context.getString(R.string.your_move)
                }
                else -> {
                    binding.tvMoveCount.visible(isVisible = false)
                }
            }

            binding.ivUser.loadImage(userData?.profile_pic)
            binding.tvName.text = (userData?.name)

            binding.tvMessage.text = data.lastMessage
            binding.tvMessage.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (data.unreadMessageCount > 0 && data.lastMessageUserId == quickBloxManager.getOtherUserId(
                            data
                        )?.toInt()
                    ) R.color.iconsColorDark else R.color.icons_color
                )
            )

            binding.tvMessage.typeface =
                ResourcesCompat.getFont(
                    binding.root.context,
                    if (data.unreadMessageCount > 0 && data.lastMessageUserId == quickBloxManager.getOtherUserId(
                            data
                        )?.toInt()
                    ) R.font.poppins_bold else R.font.poppins_regular
                )
            val time = if (data.lastMessage == null) {
                val timeFormatToday = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
//                val timeFormatToday = SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)
                timeFormatToday.format(data.createdAt)
            } else getDialogLastMessageTime(data.lastMessageDateSent)
            binding.tvTime.text = time
            binding.root.setOnClickListener {
                clickHandler(data, absoluteAdapterPosition)
            }

            binding.root.setOnLongClickListener {
                data.showPopup(if (isfavourite) REMOVE_FROM_FAVORITES else "Add to favorites")
                true
            }

        }

        private fun QBChatDialog.showPopup(favouriteText: String) {
            val popup = PopupMenu(binding.root.context, binding.tvTime)
            popup.menu.add(0, 0, Menu.NONE, favouriteText)
            popup.menu.add(0, 1, Menu.NONE, "Unmatch")

            popup.setOnMenuItemClickListener {
                popUpClickHandler(this, it.itemId, absoluteAdapterPosition,favouriteText)
                true
            }

            popup.show()
        }

        private fun getDialogLastMessageTime(seconds: Long): String {
            val timeInMillis = seconds * 1000
            val msgTime = Calendar.getInstance()
            msgTime.timeInMillis = timeInMillis

            if (timeInMillis == 0L) {
                return ""
            }

            val now = Calendar.getInstance()
            val timeFormatToday = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
            return timeFormatToday.format(Date(timeInMillis))
            /*val timeFormatToday = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val dateFormatThisYear = SimpleDateFormat("d MMM", Locale.ENGLISH)
            val lastYearFormat = SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)

            return if (now.get(Calendar.DATE) == msgTime.get(Calendar.DATE) && now.get(Calendar.YEAR) == msgTime.get(
                    Calendar.YEAR
                )
            ) {
                timeFormatToday.format(Date(timeInMillis))
            } else if (now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1 && now.get(
                    Calendar.YEAR
                ) == msgTime.get(Calendar.YEAR)
            ) {
                "yesterday"
            } else if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
                dateFormatThisYear.format(Date(timeInMillis))
            } else {
                lastYearFormat.format(Date(timeInMillis))
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> {
                val binding =
                    LayoutConnectionsItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return ConnectionViewHolder(binding)
            }
            else -> {
                val progressBinding =
                    ProgressBarBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ProgressViewHolder(progressBinding)
                return ProgressViewHolder(progressBinding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> (holder as ConnectionViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position]?.dialogId.hashCode().toLong()

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) ITEM_LOADING else ITEM

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    fun addLoadingView() {
        //add loading item
        Handler(Looper.getMainLooper()).post {
            list.add(null)
            notifyItemInserted(list.size - 1)
        }
    }

    fun removeLoadingView() {
        //Remove loading item
        if (list.isNotEmpty() && list[list.size - 1] == null) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
        }
    }
}