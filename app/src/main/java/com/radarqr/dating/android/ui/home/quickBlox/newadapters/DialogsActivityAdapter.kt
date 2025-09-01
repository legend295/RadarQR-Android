package com.radarqr.dating.android.ui.home.quickblox.newadapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.LayoutItemDialogsBinding
import com.radarqr.dating.android.utility.handler.RecyclerViewClickHandler
import com.quickblox.chat.model.QBChatDialog
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val MAX_MESSAGES_TEXT = "99+"
private const val MAX_MESSAGES = 99

class DialogsActivityAdapter(
    var context: Context,
    var quickBloxId: String,
    var clickHandler: RecyclerViewClickHandler<View, Int, QBChatDialog>
) :
    RecyclerView.Adapter<DialogsActivityAdapter.DialogsViewHolder>() {

    private var isSelectMode = false
    private var yourMoveCount = 0

    /*   private var _selectedItems: ArrayList<QBChatDialog> = ArrayList()
       val selectedItems: ArrayList<QBChatDialog>
           get() = _selectedItems
   */
    val imagesList: HashMap<String, String> = HashMap()

    var dialogs: ArrayList<QBChatDialog> = ArrayList()
        set(value) {
            field = value
            add(field)
            sortByDate()
        }

    fun add(dialogs: ArrayList<QBChatDialog>) {
        for (value in dialogs) {
            imagesList[value.dialogId] =
                if (value.customData != null) value.customData.fields["occupant_profile_pic"].toString() else if (value.photo != null) value.photo else ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogsViewHolder {
        val binding: LayoutItemDialogsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_item_dialogs,
            parent,
            false
        )

        return DialogsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogsViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = dialogs.size


    inner class DialogsViewHolder(var binding: LayoutItemDialogsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val dialog = dialogs[adapterPosition]

            val nameWithoutSpaces = if (dialog.customData != null)
                dialog.customData.fields["occupant_name"].toString() else dialog.name

            if (quickBloxId.isEmpty() || quickBloxId == if (dialog.lastMessageUserId == null) "0" else dialog.lastMessageUserId.toString()) {
                binding.textDialogAvatarTitle.visibility = View.GONE
            } else {
                if (dialog.lastMessageUserId == null) {
                    binding.textDialogAvatarTitle.visibility = View.GONE
                } else {
                    binding.textDialogAvatarTitle.visibility = View.VISIBLE
                    yourMoveCount++
                }
            }

            if (imagesList.containsKey(dialog.dialogId))
                Glide.with(context)
                    .load(imagesList[dialog.dialogId])
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            return false
                        }
                    })
                    .into(binding.imageDialogIcon)
            /*if (dialog.customData != null)
                if (dialog.customData.fields["occupant_profile_pic"].toString() != "") {


                }*/

            binding.textDialogName.text = nameWithoutSpaces
            binding.tvNew.visibility = if(prepareTextLastMessage(dialog).isEmpty()) View.VISIBLE else View.GONE
            binding.textDialogLastMessage.text = prepareTextLastMessage(dialog)
            binding.textLastMsgTime.text =
                getDialogLastMessageTime(dialog.lastMessageDateSent)

            val unreadMessagesCount = getUnreadMsgCount(dialog)
            if (isSelectMode) {
                binding.checkboxDialogs.visibility = View.VISIBLE
                binding.textLastMsgTime.visibility = View.INVISIBLE
                binding.textUnreadCounter.visibility = View.INVISIBLE
            } else if (unreadMessagesCount == 0) {
                binding.checkboxDialogs.visibility = View.INVISIBLE
                binding.textLastMsgTime.visibility = View.VISIBLE
                binding.textUnreadCounter.visibility = View.INVISIBLE
                binding.textLastMsgTime.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )
                binding.textDialogLastMessage.setTypeface(null, Typeface.NORMAL)
            } else {
                binding.checkboxDialogs.visibility = View.INVISIBLE
                binding.textLastMsgTime.visibility = View.VISIBLE
                binding.textUnreadCounter.visibility = View.VISIBLE
                val messageCount = if (unreadMessagesCount > MAX_MESSAGES) {
                    MAX_MESSAGES_TEXT
                } else {
                    unreadMessagesCount.toString()
                }
                if (messageCount == "0" || messageCount == "") {
                    binding.textLastMsgTime.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                    binding.textDialogLastMessage.setTypeface(null, Typeface.NORMAL)
                } else {
                    if (quickBloxId == if (dialog.lastMessageUserId == null) "0" else dialog.lastMessageUserId.toString()) {
                        binding.textLastMsgTime.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.black
                            )
                        )
                        binding.textDialogLastMessage.setTypeface(null, Typeface.NORMAL)
                    } else {
                        binding.textLastMsgTime.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.mobile_back
                            )
                        )
                        binding.textDialogLastMessage.setTypeface(null, Typeface.BOLD)
                    }
                }
//            holder.unreadCounterTextView.text = messageCount
            }

            val backgroundColor: Int
            /* if (isItemSelected(adapterPosition)) {
                 binding.checkboxDialogs.isChecked = true
                 backgroundColor = context.resources.getColor(R.color.mobile_back)
             } else {
                 binding.checkboxDialogs.isChecked = false
                 backgroundColor = context.resources.getColor(android.R.color.transparent)
             }*/
//            binding.root.setBackgroundColor(backgroundColor)

            binding.root.setOnClickListener {
                clickHandler.onClick(it, adapterPosition, dialogs[adapterPosition])
            }

            binding.root.setOnLongClickListener {
                clickHandler.onLongClick(it, adapterPosition, dialogs[adapterPosition])
                return@setOnLongClickListener true
            }
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

    /*private fun isItemSelected(position: Int): Boolean {
        return _selectedItems.isNotEmpty() && _selectedItems.contains(dialogs[position])
    }
*/

    fun prepareToSelect() {
        isSelectMode = true
        notifyDataSetChanged()
    }

    fun clearList() {
        dialogs.clear()
        notifyDataSetChanged()
    }

    fun updateObject(position: Int, data: QBChatDialog) {
        dialogs[position] = data
        notifyItemChanged(position)
    }

    fun getYourMoveCount(): Int = yourMoveCount
    fun setYourMoveCount(count: Int) {
        yourMoveCount = count
        notifyDataSetChanged()
    }

    /*  fun clearSelection() {
          isSelectMode = false
          _selectedItems.clear()
          notifyDataSetChanged()
      }*/


    /* fun updateList(newData: List<QBChatDialog>) {
         dialogs = newData
         notifyDataSetChanged()
     }*/

    /* fun selectItem(item: QBChatDialog) {
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
     }*/

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

    @SuppressLint("NotifyDataSetChanged")
    private fun sortByDate() {
        dialogs.sortWith { o1, o2 -> o2?.updatedAt?.compareTo(o1?.updatedAt)!! }
        notifyDataSetChanged()
    }

}