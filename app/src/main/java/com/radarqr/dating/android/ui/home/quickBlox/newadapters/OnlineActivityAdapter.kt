package com.radarqr.dating.android.ui.home.quickblox.newadapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.LayoutItemOnlineBinding
import com.quickblox.chat.model.QBChatDialog
import com.radarqr.dating.android.utility.S3Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OnlineActivityAdapter(var context: Context, var quickBloxId: String) :
    RecyclerView.Adapter<OnlineActivityAdapter.OnlineViewHolder>() {

    private var isSelectMode = false
    private var _selectedItems: ArrayList<QBChatDialog> = ArrayList()
    val selectedItems: ArrayList<QBChatDialog>
        get() = _selectedItems

    val imagesList: ArrayList<String> = ArrayList()


    var dialogs: ArrayList<QBChatDialog> = ArrayList()
        set(value) {
            field = value
            sortByDate()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineViewHolder {
        val binding: LayoutItemOnlineBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_item_online,
            parent,
            false
        )
        return OnlineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnlineViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = dialogs.size

    inner class OnlineViewHolder(var binding: LayoutItemOnlineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val dialog = dialogs[adapterPosition]

            val nameWithoutSpaces =
                if (dialog.customData != null) dialog.customData.fields["occupant_name"].toString() else ""/*dialog.name.replace(" ", "")*/


            if (dialog.customData != null)
                if (dialog.customData.fields["occupant_profile_pic"].toString().equals("")) {

                } else {
                    val urlFromS3 = S3Utils.generatesThumbShareUrl(
                        context, dialog.customData.fields["occupant_profile_pic"].toString() ?: ""
                    )

                    val image = urlFromS3.replace(" ", "%20")


                        Glide.with(context).load(image).into(binding.imageDialogIcon)
                   /* try {
                        if (imagesList[position] != dialog.customData.fields["occupant_profile_pic"].toString()) {
                            imagesList[position] = image
                            Glide.with(context).load(image).into(holder.dialogImageView)

                        }
                    } catch (e: IndexOutOfBoundsException) {
                        imagesList.add(dialog.customData.fields["occupant_profile_pic"].toString())
                    }*/

//            Glide.with(context).load(Image).into(holder.dialogImageView)
                }
//        holder.dialogImageView.setImageDrawable(getColorCircleDrawable(position))
           /* holder.nameTextView.text = nameWithoutSpaces *//*getDialogName(dialog)*//*
            holder.lastMessageTextView.text = prepareTextLastMessage(dialog)
            holder.lastMessageTimeTextView.text = getDialogLastMessageTime(dialog.lastMessageDateSent)

            if (quick_blox_id.equals(dialog.userId)) {
                holder.dialogAvatarTitle.visibility = View.GONE
            } else {
                holder.dialogAvatarTitle.visibility = View.VISIBLE
            }

            val unreadMessagesCount = getUnreadMsgCount(dialog)
            if (isSelectMode) {
                holder.checkboxDialog.visibility = View.VISIBLE
                holder.lastMessageTimeTextView.visibility = View.INVISIBLE
                holder.unreadCounterTextView.visibility = View.INVISIBLE
            } else if (unreadMessagesCount == 0) {
                holder.checkboxDialog.visibility = View.INVISIBLE
                holder.lastMessageTimeTextView.visibility = View.VISIBLE
                holder.unreadCounterTextView.visibility = View.INVISIBLE
            } else {
                holder.checkboxDialog.visibility = View.INVISIBLE
                holder.lastMessageTimeTextView.visibility = View.VISIBLE
                holder.unreadCounterTextView.visibility = View.VISIBLE
                val messageCount = if (unreadMessagesCount > com.e.radardating.ui.home.quickBlox.adapter.MAX_MESSAGES) {
                    com.e.radardating.ui.home.quickBlox.adapter.MAX_MESSAGES_TEXT
                } else {
                    unreadMessagesCount.toString()
                }
//            holder.unreadCounterTextView.text = messageCount
                if (messageCount == "") {
                    holder.lastMessageTimeTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                } else {
                    holder.lastMessageTimeTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.mobile_back
                        )
                    )
                }
            }


            val backgroundColor: Int
            if (isItemSelected(position)) {
                holder.checkboxDialog.isChecked = true
                backgroundColor = context.resources.getColor(R.color.mobile_back)
            } else {
                holder.checkboxDialog.isChecked = false
                backgroundColor = context.resources.getColor(android.R.color.transparent)
            }
            holder.rootLayout.setBackgroundColor(backgroundColor)*/
        }
    }

    private fun prepareTextLastMessage(chatDialog: QBChatDialog): String {
        var lastMessage = ""
        if (isLastMessageAttachment(chatDialog)) {
            lastMessage = "attachment"
        } else {
            chatDialog.lastMessage?.let {
                lastMessage = it
            }
        }
        return lastMessage
    }

    private fun isLastMessageAttachment(dialog: QBChatDialog): Boolean {
        val lastMessage = dialog.lastMessage
        val lastMessageSenderId = dialog.lastMessageUserId
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null
    }

    private fun getUnreadMsgCount(chatDialog: QBChatDialog): Int {
        val unreadMessageCount = chatDialog.unreadMessageCount
        return unreadMessageCount ?: 0
    }

    private fun isItemSelected(position: Int): Boolean {
        return _selectedItems.isNotEmpty() && _selectedItems.contains(dialogs[position])
    }


    fun prepareToSelect() {
        isSelectMode = true
        notifyDataSetChanged()
    }

    fun clearList() {
        (dialogs as ArrayList).clear()
        notifyDataSetChanged()
    }

    fun clearSelection() {
        isSelectMode = false
        _selectedItems.clear()
        notifyDataSetChanged()
    }

    /* fun updateList(newData: List<QBChatDialog>) {
         dialogs = newData
         notifyDataSetChanged()
     }*/

    fun selectItem(item: QBChatDialog) {
        if (_selectedItems.contains(item)) {
            return
        }
        _selectedItems.add(item)
        notifyDataSetChanged()
    }

    fun toggleSelection(item: QBChatDialog) {
        if (_selectedItems.contains(item)) {
            _selectedItems.remove(item)
        } else {
            _selectedItems.add(item)
        }
        notifyDataSetChanged()
    }

    private fun getDialogLastMessageTime(seconds: Long): String {
        val timeInMillis = seconds * 1000
        val msgTime = Calendar.getInstance()
        msgTime.timeInMillis = timeInMillis

        if (timeInMillis == 0L) {
            return ""
        }

        val now = Calendar.getInstance()
        val timeFormatToday = SimpleDateFormat("HH:mm", Locale.ENGLISH)
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
        }
    }


    fun sortByDate() {
        dialogs.sortWith { o1, o2 -> o2?.updatedAt?.compareTo(o1?.updatedAt)!! }
        notifyDataSetChanged()
    }

}