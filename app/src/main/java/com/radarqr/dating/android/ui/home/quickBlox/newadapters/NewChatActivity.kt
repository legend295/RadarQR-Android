//package com.e.radardating.ui.home.quickBlox.newadapters
//
//import android.app.Activity
//import android.app.DownloadManager
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.os.*
//import android.text.Editable
//import android.text.TextUtils
//import android.text.TextWatcher
//import android.util.Log
//import android.view.View
//import android.widget.*
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.e.radardating.R
//import com.e.radardating.databinding.ActivityChatBinding
//import com.e.radardating.ui.home.QuickBlox.ConnectionBaseFragment
//import com.e.radardating.ui.home.quickBlox.listeners.MessageLongClickListener
//import com.e.radardating.ui.home.quickBlox.managers.DialogsManager
//import com.e.radardating.utility.BaseUtils
//import com.e.radardating.utility.PreferencesHelper
//import com.e.radardating.utility.SharedPrefsHelper
//import com.e.radardating.utility.chat.CHAT_HISTORY_ITEMS_PER_PAGE
//import com.e.radardating.utility.chat.ChatHelper
//import com.e.radardating.utility.chat.VerboseQbChatConnectionListener
//import com.quickblox.auth.session.QBSessionManager
//import com.quickblox.chat.QBChatService
//import com.quickblox.chat.QBMessageStatusesManager
//import com.quickblox.chat.QBSystemMessagesManager
//import com.quickblox.chat.exception.QBChatException
//import com.quickblox.chat.listeners.QBChatDialogTypingListener
//import com.quickblox.chat.listeners.QBMessageStatusListener
//import com.quickblox.chat.listeners.QBSystemMessageListener
//import com.quickblox.chat.model.QBAttachment
//import com.quickblox.chat.model.QBChatDialog
//import com.quickblox.chat.model.QBChatMessage
//import com.quickblox.chat.model.QBDialogType
//import com.quickblox.content.model.QBFile
//import com.quickblox.core.QBEntityCallback
//import com.quickblox.core.exception.QBResponseException
//import com.quickblox.core.helper.StringifyArrayList
//import com.quickblox.core.request.QBRequestBuilder
//import com.quickblox.messages.QBPushNotifications
//import com.quickblox.messages.model.QBEnvironment
//import com.quickblox.messages.model.QBEvent
//import com.quickblox.messages.model.QBNotificationType
//import com.quickblox.messages.model.QBPushType
//import com.quickblox.sample.chat.kotlin.utils.qb.*
//import com.quickblox.sample.chat.kotlin.utils.shortToast
//import com.quickblox.users.model.QBUser
//import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.runBlocking
//import org.jivesoftware.smack.ConnectionListener
//import org.jivesoftware.smack.SmackException
//import org.jivesoftware.smack.XMPPException
//import org.jivesoftware.smackx.muc.DiscussionHistory
//import org.koin.android.ext.android.inject
//import java.io.File
//import java.util.*
//import kotlin.collections.ArrayList
//
//
//const val REQUEST_CODE_SELECT_PEOPLE = 752
//private const val REQUEST_CODE_ATTACHMENT = 721
//private const val PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010
//
//const val PROPERTY_FORWARD_USER_NAME = "origin_sender_name"
//
//const val EXTRA_DIALOG_ID = "dialogId"
//const val EXTRA_IS_NEW_DIALOG = "isNewDialog"
//const val IS_IN_BACKGROUND = "is_in_background"
//
//const val ORDER_RULE = "order"
//const val ORDER_VALUE_UPDATED_AT = "desc string updated_at"
//
//const val TYPING_STATUS_DELAY = 2000L
//const val TYPING_STATUS_INACTIVITY_DELAY = 10000L
//private const val SEND_TYPING_STATUS_DELAY: Long = 3000L
//const val MAX_ATTACHMENTS_COUNT = 1
//const val MAX_MESSAGE_SYMBOLS_LENGTH = 1000
//
//class ChatActivity(internal var qbChatDialogNew: QBChatDialog, internal var is_new: Boolean) :
//    ConnectionBaseFragment<ActivityChatBinding>(), QBMessageStatusListener,
//    DialogsManager.ManagingDialogsCallbacks {
//    private val TAG = ChatActivity::class.java.simpleName
//
//    private lateinit var progressBar: ProgressBar
//    private lateinit var messageEditText: EditText
//    private lateinit var attachmentBtnChat: ImageView
//    private lateinit var typingStatus: TextView
//    private var currentUser = QBUser()
//
//    private lateinit var attachmentPreviewContainerLayout: LinearLayout
//    private lateinit var chatMessagesRecyclerView: RecyclerView
//
//    private lateinit var chatAdapter: ChatAdapter
//    private lateinit var chatConnectionListener: ConnectionListener
//
//    //    private lateinit var imageAttachClickListener: ImageAttachClickListener
////    private lateinit var videoAttachClickListener: VideoAttachClickListener
////    private lateinit var fileAttachClickListener: FileAttachClickListener
//    private lateinit var messageLongClickListener: MessageLongClickListenerImpl
//    private lateinit var qbMessageStatusesManager: QBMessageStatusesManager
//    private var chatMessageListener: ChatMessageListener = ChatMessageListener()
//    private var dialogsManager: DialogsManager = DialogsManager()
//    private var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
//    private lateinit var systemMessagesManager: QBSystemMessagesManager
//    private lateinit var messagesList: MutableList<QBChatMessage>
//    private lateinit var qbChatDialog: QBChatDialog
//    private var unShownMessages: ArrayList<QBChatMessage>? = null
//    private var skipPagination = 0
//    private var checkAdapterInit: Boolean = false
//    private val preferencesHelper: PreferencesHelper by inject()
////    private val chatViewModel: ChatViewModel by viewModel()
//
//    companion object {
//        lateinit var qbChatDialog_1: QBChatDialog
//        fun startForResult(activity: Activity, code: Int, dialogId: QBChatDialog) {
//            val intent = Intent(activity, ChatActivity::class.java)
//            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
//            activity.startActivityForResult(intent, code)
//        }
//
//        fun startForResult(
//            activity: Activity,
//            code: Int,
//            dialogId: QBChatDialog,
//            isNewDialog: Boolean
//        ) {
//            val intent = Intent(activity, ChatActivity::class.java)
//            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
//            intent.putExtra(EXTRA_IS_NEW_DIALOG, isNewDialog)
//            activity.startActivityForResult(intent, code)
//        }
//    }
//
//    override fun getLayoutRes(): Int = R.layout.activity_chat
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        binding.chatFragment = this
//        SharedPrefsHelper.delete(IS_IN_BACKGROUND)
//        Log.v(TAG, "onCreate ChatActivity on Thread ID = " + Thread.currentThread().id)
//
//        if (!ChatHelper.isLogged()) {
//            reloginToChat()
//        } else {
////            returnListeners()
//        }
//
//        qbChatDialog = qbChatDialogNew as QBChatDialog
//        qbChatDialog_1 = qbChatDialogNew as QBChatDialog
//        if (ChatHelper.getCurrentUser() != null) {
//            currentUser = ChatHelper.getCurrentUser()!!
//        } else {
//            Log.e(TAG, "Finishing " + TAG + ". Current user is null")
//            requireActivity().finish()
//        }
//        Log.v(TAG, "Deserialized dialog = $qbChatDialog")
//
//        try {
//            qbChatDialog.initForChat(QBChatService.getInstance())
//        } catch (e: IllegalStateException) {
//            Log.d(TAG, "initForChat error. Error message is : " + e.message)
//            Log.e(TAG, "Finishing " + TAG + ". Unable to init chat")
//            requireActivity().finish()
//        }
//        qbChatDialog.addMessageListener(chatMessageListener)
//        qbChatDialog.addIsTypingListener(TypingStatusListener())
////        BaseUtils.showProgressbar(requireContext())
//        initViews()
//        initMessagesRecyclerView()
//        initChatConnectionListener()
//        initChat()
//        returnListeners()
//    }
//
////    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
////        outState.putString(EXTRA_DIALOG_ID, qbChatDialog.dialogId)
////        requireActivity().onSaveInstanceState(outState, outPersistentState)
////    }
////
////    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
////       onRestoreInstanceState(savedInstanceState)
////        try {
////            val dialogId = savedInstanceState.getString(EXTRA_DIALOG_ID)!!
////            qbChatDialog = QbDialogHolder.getChatDialogById(dialogId)!!
////        } catch (e: Exception) {
////            Log.d(TAG, e.message!!)
////        }
////    }
//
//
////    override fun onResumeFinished() {
////        if (ChatHelper.isLogged()) {
////            if (qbChatDialog!=null) {
////                qbChatDialog =  qbChatDialogNew//requireActivity().intent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog
////            }
////            returnToChat()
////        } else {
////            reloginToChat()
////        }
////    }
//
//    private fun reloginToChat() {
//
//        ChatHelper.loginToChat(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<Void> {
//            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
//                returnToChat()
//
//            }
//
//            override fun onError(e: QBResponseException?) {
//
//            }
//        })
//    }
//
//    private fun returnToChat() {
//        qbChatDialog.initForChat(QBChatService.getInstance())
//        if (!qbChatDialog.isJoined) {
//            try {
//                qbChatDialog.join(DiscussionHistory())
//            } catch (e: Exception) {
//                Log.e(TAG, "Join Dialog Exception: " + e.message)
//            }
//        }
//
//        // Loading unread messages received in background
//        if (SharedPrefsHelper.get(IS_IN_BACKGROUND, false)) {
//            progressBar.visibility = View.VISIBLE
//            skipPagination = 0
//            checkAdapterInit = false
//            loadChatHistory()
//        }
//
//        returnListeners()
//    }
//
//    private fun returnListeners() {
//        if (qbChatDialog.isTypingListeners.isEmpty()) {
//            qbChatDialog.addIsTypingListener(TypingStatusListener())
//        }
//
//        dialogsManager.addManagingDialogsCallbackListener(this)
//        try {
//            systemMessagesManager = QBChatService.getInstance().systemMessagesManager
//            systemMessagesManager.addSystemMessageListener(systemMessagesListener)
//            qbMessageStatusesManager = QBChatService.getInstance().messageStatusesManager
//            qbMessageStatusesManager.addMessageStatusListener(this)
//        } catch (e: Exception) {
//            e.message?.let { Log.d(TAG, it) }
//        }
////        chatAdapter.setAttachImageClickListener(imageAttachClickListener)
////        chatAdapter.setAttachVideoClickListener(videoAttachClickListener)
////        chatAdapter.setAttachFileClickListener(fileAttachClickListener)
////        chatAdapter.setMessageLongClickListener(messageLongClickListener)
//        ChatHelper.addConnectionListener(chatConnectionListener)
//    }
//
//    /*   override fun onPause() {
//           super.onPause()
//   //        chatAdapter.removeClickListeners()
//           ChatHelper.removeConnectionListener(chatConnectionListener)
//   //        qbMessageStatusesManager.removeMessageStatusListener(this)
//           SharedPrefsHelper.save(IS_IN_BACKGROUND, true)
//       }
//
//       override fun onDestroy() {
//           super.onDestroy()
//   //        systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
//           qbChatDialog.removeMessageListrener(chatMessageListener)
//           dialogsManager.removeManagingDialogsCallbackListener(this)
//           SharedPrefsHelper.delete(IS_IN_BACKGROUND)
//       }*/
//
////    override fun onBackPressed() {
////        qbChatDialog.removeMessageListrener(chatMessageListener)
////        sendDialogId()
////        super.onBackPressed()
////    }
////    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//////        menuInflater.inflate(R.menu.menu_activity_chat, menu)
//////        val menuItemInfo = menu.findItem(R.id.menu_chat_action_info)
//////        val menuItemLeave = menu.findItem(R.id.menu_chat_action_leave)
//////        val menuItemDelete = menu.findItem(R.id.menu_chat_action_delete)
//////
//////        when (qbChatDialog.type) {
//////            QBDialogType.GROUP -> {
//////                menuItemDelete.isVisible = false
//////            }
//////
//////            QBDialogType.PRIVATE -> {
//////                menuItemInfo.isVisible = false
//////                menuItemLeave.isVisible = false
//////            }
//////
//////            QBDialogType.PUBLIC_GROUP -> {
//////                menuItemInfo.isVisible = false
//////                menuItemLeave.isVisible = false
//////                menuItemDelete.isVisible = false
//////            }
//////
//////            else -> {
//////
//////            }
//////        }
////        return true
////    }
////
////    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//////        when (item.itemId) {
//////            R.id.menu_chat_action_info -> {
//////                updateDialog()
//////                return true
//////            }
//////            R.id.menu_chat_action_leave -> {
//////                openAlertDialogLeaveGroupChat()
//////                return true
//////            }
//////            R.id.menu_chat_action_delete -> {
//////                openAlertDialogDeletePrivateChat()
//////                return true
//////            }
//////            android.R.id.home -> {
//////                onBackPressed()
//////                return true
//////            }
//////            else -> return super.onOptionsItemSelected(item)
//////        return true
//////        }
////        return true
////
////    }
//
//    private fun openAlertDialogLeaveGroupChat() {
////        val alertDialogBuilder = AlertDialog.Builder(this@ChatActivity, R.style.AlertDialogStyle)
////        alertDialogBuilder.setTitle(getString(R.string.dlg_leave_group_dialog))
////        alertDialogBuilder.setMessage(getString(R.string.dlg_leave_group_question))
////        alertDialogBuilder.setCancelable(false)
////        alertDialogBuilder.setPositiveButton(getString(R.string.dlg_leave)) { dialog, which -> leaveGroupChat() }
////        alertDialogBuilder.setNegativeButton(getString(R.string.dlg_cancel)) { dialog, which -> }
////        alertDialogBuilder.create()
////        alertDialogBuilder.show()
//    }
//
//    private fun openAlertDialogDeletePrivateChat() {
////        val alertDialogBuilder = AlertDialog.Builder(this@ChatActivity, R.style.AlertDialogStyle)
////        alertDialogBuilder.setTitle(getString(R.string.dlg_delete_private_dialog))
////        alertDialogBuilder.setMessage(getString(R.string.dlg_delete_private_question))
////        alertDialogBuilder.setCancelable(false)
////        alertDialogBuilder.setPositiveButton(getString(R.string.dlg_delete)) { dialog, which -> deleteChat() }
////        alertDialogBuilder.setNegativeButton(getString(R.string.dlg_cancel)) { dialog, which -> }
////        alertDialogBuilder.create()
////        alertDialogBuilder.show()
//    }
//
//    private fun showPopupMenu(
//        isIncomingMessageClicked: Boolean,
//        view: View,
//        chatMessage: QBChatMessage
//    ) {
////        val popupMenu = PopupMenu(this@ChatActivity, view)
////
////        popupMenu.menuInflater.inflate(R.menu.menu_message_longclick, popupMenu.menu)
////        popupMenu.gravity = Gravity.RIGHT
////
////        if (isIncomingMessageClicked || (qbChatDialog.type != QBDialogType.GROUP)) {
////            popupMenu.menu.removeItem(R.id.menu_message_delivered_to)
////            popupMenu.menu.removeItem(R.id.menu_message_viewed_by)
////            popupMenu.gravity = Gravity.LEFT
////        }
////
////        popupMenu.setOnMenuItemClickListener {
////            when (it.itemId) {
////                R.id.menu_message_forward -> {
////                    startForwardingMessage(chatMessage)
////                }
////                R.id.menu_message_delivered_to -> {
////                    showDeliveredToScreen(chatMessage)
////                }
////                R.id.menu_message_viewed_by -> {
////                    Log.d(TAG, "Viewed by")
////                    showViewedByScreen(chatMessage)
////                }
////            }
////            true
////        }
////        popupMenu.show()
//    }
//
//    private fun showFilePopup(itemViewType: Int?, attachment: QBAttachment?, view: View) {
////        val popupMenu = PopupMenu(this@ChatActivity, view)
////        popupMenu.menuInflater.inflate(R.menu.menu_file_popup, popupMenu.menu)
////
////        if (itemViewType == TYPE_TEXT_RIGHT || itemViewType == TYPE_ATTACH_RIGHT) {
////            popupMenu.gravity = Gravity.RIGHT
////        } else if (itemViewType == TYPE_TEXT_LEFT || itemViewType == TYPE_ATTACH_LEFT) {
////            popupMenu.gravity = Gravity.LEFT
////        }
////
////        popupMenu.setOnMenuItemClickListener {
////            when (it.itemId) {
////                R.id.menu_file_save -> {
////                    saveFileToStorage(attachment)
////                }
////            }
////            true
////        }
////        popupMenu.show()
//    }
//
//    private fun saveFileToStorage(attachment: QBAttachment?) {
//        val file = File(requireActivity().application.filesDir, attachment?.name)
//        val url = QBFile.getPrivateUrlForUID(attachment?.id)
//        val request = DownloadManager.Request(Uri.parse(url))
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.name)
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.allowScanningByMediaScanner()
//        val manager =
//            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        manager.enqueue(request)
//    }
//
//    private fun startForwardingMessage(message: QBChatMessage) {
////        ForwardToActivity.start(this, message)
//    }
//
//    private fun showDeliveredToScreen(message: QBChatMessage) {
////        MessageInfoActivity.start(this, message, MESSAGE_INFO_DELIVERED_TO)
//
//    }
//
//    private fun showViewedByScreen(message: QBChatMessage) {
////        MessageInfoActivity.start(this, message, MESSAGE_INFO_READ_BY)
//    }
//
//    private fun updateDialog() {
////        showProgressDialog(R.string.dlg_updating)
////        Log.d(TAG, "Starting Dialog Update")
////        ChatHelper.getDialogById(qbChatDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
////            override fun onSuccess(updatedChatDialog: QBChatDialog, bundle: Bundle) {
////                Log.d(TAG, "Update Dialog Successful: " + updatedChatDialog.dialogId)
////                qbChatDialog = updatedChatDialog
////
//////                ChatInfoActivity.start(this@ChatActivity, qbChatDialog)
////            }
////
////            override fun onError(e: QBResponseException) {
////                Log.d(TAG, "Dialog Loading Error: " + e.message)
////
////                showErrorSnackbar(R.string.select_users_get_dialog_error, e, null)
////            }
////        })
//    }
//
//    private fun sendDialogId() {
//        val intent = Intent().putExtra(EXTRA_DIALOG_ID, qbChatDialog.dialogId)
//        requireActivity().setResult(Activity.RESULT_OK, intent)
//    }
//
//    private fun leaveGroupChat() {
//        dialogsManager.sendMessageLeftUser(qbChatDialog)
//        dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, qbChatDialog)
//        try {
//            // Its a hack to give the Chat Server more time to process the message and deliver them
//            Thread.sleep(300)
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//
//
//        Log.d(TAG, "Leaving Dialog")
//        ChatHelper.exitFromDialog(qbChatDialog, object : QBEntityCallback<QBChatDialog> {
//            override fun onSuccess(qbDialog: QBChatDialog, bundle: Bundle?) {
//                Log.d(TAG, "Leaving Dialog Successful: " + qbDialog.dialogId)
//
//                QbDialogHolder.deleteDialog(qbDialog)
//                requireActivity().finish()
//            }
//
//            override fun onError(e: QBResponseException) {
//                Log.d(TAG, "Leaving Dialog Error: " + e.message)
//
//            }
//        })
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, "onActivityResult with resultCode: $resultCode requestCode: $requestCode")
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == REQUEST_CODE_SELECT_PEOPLE && data != null) {
//                progressBar.visibility = View.VISIBLE
//                val selectedUsers = data.getSerializableExtra(EXTRA_QB_USERS) as ArrayList<QBUser>
//                val existingOccupants = qbChatDialog.occupants
//                val newUserIds = ArrayList<Int>()
//
//                for (user in selectedUsers) {
//                    if (!existingOccupants.contains(user.id)) {
//                        newUserIds.add(user.id)
//                    }
//                }
//
//                ChatHelper.getDialogById(
//                    qbChatDialog.dialogId,
//                    object : QBEntityCallback<QBChatDialog> {
//                        override fun onSuccess(qbChatDialog: QBChatDialog, p1: Bundle?) {
//                            progressBar.visibility = View.GONE
//                            dialogsManager.sendMessageAddedUsers(qbChatDialog, newUserIds)
//                            dialogsManager.sendSystemMessageAddedUser(
//                                systemMessagesManager,
//                                qbChatDialog,
//                                newUserIds
//                            )
//                            qbChatDialog.let {
//                                this@ChatActivity.qbChatDialog = it
//                            }
//                            updateDialog(selectedUsers)
//                        }
//
//                        override fun onError(e: QBResponseException?) {
//                            progressBar.visibility = View.GONE
//
//                        }
//                    })
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST && grantResults.size > 0 && grantResults[0] != -1) {
//            openImagePicker()
//        }
//    }
////
////    override fun onImagePicked(requestCode: Int, file: File) {
////        when (requestCode) {
////            REQUEST_CODE_ATTACHMENT -> {
////                attachmentPreviewAdapter.add(file)
////            }
////        }
////    }
////
////    override fun onImagePickError(requestCode: Int, e: Exception) {
////        showErrorSnackbar(0, e, null)
////        val rootView = window.decorView.findViewById<View>(android.R.id.content)
////        showSnackbar(rootView, 0, e, R.string.dlg_hide, object : View.OnClickListener {
////            override fun onClick(v: View?) {
////                Snackbar.SnackbarLayout.GONE
////            }
////        })
////    }
////
////    override fun onImagePickClosed(ignored: Int) {
////
////    }
//
//
//    fun showMessage(message: QBChatMessage) {
//        if (isAdapterConnected()) {
//            chatAdapter.addMessage(message)
//            scrollMessageListDown()
//        } else {
//            delayShowMessage(message)
//        }
//    }
//
//    private fun isAdapterConnected(): Boolean {
//        return checkAdapterInit
//    }
//
//    private fun delayShowMessage(message: QBChatMessage) {
//        if (unShownMessages == null) {
//            unShownMessages = ArrayList()
//        }
//        unShownMessages!!.add(message)
//    }
//
//    private fun initViews() {
////        requireActivity().supportActionBar?.title = ""
////        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        binding.ivChatSend.setOnClickListener {
//            try {
//                qbChatDialog.sendStopTypingNotification()
//            } catch (e: XMPPException) {
//                Log.d(TAG, e.message!!)
//            } catch (e: SmackException.NotConnectedException) {
//                Log.d(TAG, e.message!!)
//            }
//
////
////        val totalAttachmentsCount = attachmentPreviewAdapter.count
////        val uploadedAttachments = attachmentPreviewAdapter.uploadedAttachments
////        if (uploadedAttachments.isNotEmpty()) {
////            if (uploadedAttachments.size == totalAttachmentsCount) {
////                for (attachment in uploadedAttachments) {
////                    sendChatMessage(null, attachment)
////                }
////            } else {
////                shortToast(R.string.chat_wait_for_attachments_to_upload)
////            }
////        }
////
//            var text = messageEditText.text.toString().trim { it <= ' ' }
//            if (!TextUtils.isEmpty(text)) {
//                if (text.length > MAX_MESSAGE_SYMBOLS_LENGTH) {
//                    text = text.substring(0, MAX_MESSAGE_SYMBOLS_LENGTH)
//                }
//                sendChatMessage(text, null)
//            }
//        }
//        typingStatus = requireActivity().findViewById(R.id.tv_typing_status)
//
//        messageEditText = requireActivity().findViewById(R.id.et_chat_message)
//        messageEditText.addTextChangedListener(TextInputWatcher())
//
//        progressBar = requireActivity().findViewById(R.id.progress_bar)
//        attachmentPreviewContainerLayout =
//            requireActivity().findViewById(R.id.ll_attachment_preview_container)
//
//        attachmentBtnChat = requireActivity().findViewById(R.id.iv_chat_attachment)
//        attachmentBtnChat.setOnClickListener {
////            if (attachmentPreviewAdapter.count >= MAX_ATTACHMENTS_COUNT) {
////                shortToast(R.string.error_attachment_count)
////            } else {
////                openImagePicker()
////            }
//        }
////
////        attachmentPreviewAdapter = AttachmentPreviewAdapter(this, object : AttachmentPreviewAdapter.AttachmentCountChangedListener {
////            override fun onAttachmentCountChanged(count: Int) {
////                val visiblePreview = when (count) {
////                    0 -> View.GONE
////                    else -> View.VISIBLE
////                }
////                attachmentPreviewContainerLayout.visibility = visiblePreview
////            }
////        }, object : AttachmentPreviewAdapter.AttachmentUploadErrorListener {
////            override fun onAttachmentUploadError(e: QBResponseException) {
////                showErrorSnackbar(0, e, View.OnClickListener { v ->
////                    pickAnImage(this@ChatActivity, REQUEST_CODE_ATTACHMENT)
////                })
////            }
////        })
////        val previewAdapterView = findViewById<AttachmentPreviewAdapterView>(R.id.adapter_attachment_preview)
////        previewAdapterView.setAdapter(attachmentPreviewAdapter)
//    }
//
//    private fun openImagePicker() {
////        val permissionHelper = SystemPermissionHelper(this)
////        if (permissionHelper.isSaveImagePermissionGranted()) {
////            pickAnImage(this, REQUEST_CODE_ATTACHMENT)
////        } else {
////            permissionHelper.requestPermissionsForSaveFileImage()
////        }
//    }
//
//    private fun initMessagesRecyclerView() {
//        chatMessagesRecyclerView = requireActivity().findViewById(R.id.rv_chat_messages)
//
//        val layoutManager = LinearLayoutManager(requireActivity())
//        layoutManager.stackFromEnd = true
//        chatMessagesRecyclerView.layoutManager = layoutManager
//
//        messagesList = ArrayList()
//        chatAdapter = ChatAdapter(requireActivity(), qbChatDialog, messagesList)
//        chatAdapter.setPaginationHistoryListener(PaginationListener())
//        chatMessagesRecyclerView.addItemDecoration(StickyRecyclerHeadersDecoration(chatAdapter))
//
//        chatMessagesRecyclerView.adapter = chatAdapter
////        imageAttachClickListener = ImageAttachClickListener()
////        videoAttachClickListener = VideoAttachClickListener()
////        fileAttachClickListener = FileAttachClickListener()
//        messageLongClickListener = MessageLongClickListenerImpl()
//    }
//
//    private fun sendChatMessage(text: String?, attachment: QBAttachment?) {
//        if (ChatHelper.isLogged()) {
//            val chatMessage = QBChatMessage()
//            attachment?.let {
//                chatMessage.addAttachment(it)
//            } ?: run {
//                chatMessage.body = text
//            }
//
//
//            chatMessage.setSaveToHistory(true)
//            chatMessage.dateSent = System.currentTimeMillis() / 1000
//            chatMessage.isMarkable = true
//
//            if (qbChatDialog.type != QBDialogType.PRIVATE && !qbChatDialog.isJoined) {
//                qbChatDialog.join(DiscussionHistory())
//                shortToast(R.string.chat_still_joining)
//                return
//            }
//            try {
//                Log.d(TAG, "Sending Message with ID: " + chatMessage.id)
//                qbChatDialog.sendMessage(chatMessage)
//                send_Notification()
//                if (qbChatDialog.type == QBDialogType.PRIVATE) {
//                    showMessage(chatMessage)
//                }
//
//                attachment?.let {
////                    attachmentPreviewAdapter.remove(it)
//                } ?: run {
//                    messageEditText.setText("")
//                }
//            } catch (e: SmackException.NotConnectedException) {
//                Log.w(TAG, e)
//                shortToast(R.string.chat_error_send_message)
//            }
//        } else {
//
//            Log.d(TAG, "Relogin to Chat")
//            ChatHelper.loginToChat(currentUser,
//                object : QBEntityCallback<Void> {
//                    override fun onSuccess(p0: Void?, p1: Bundle?) {
//                        Log.d(TAG, "Relogin Successful")
//                        sendChatMessage(text, attachment)
//
//                    }
//
//                    override fun onError(e: QBResponseException) {
//                        Log.d(TAG, "Relogin Error: " + e.message)
//
//                        shortToast(R.string.chat_send_message_error)
//                    }
//                })
//        }
//    }
//
//    private fun initChat() {
//        when (qbChatDialog.type) {
//            QBDialogType.GROUP,
//            QBDialogType.PUBLIC_GROUP -> joinGroupChat()
//            QBDialogType.PRIVATE -> loadDialogUsers()
//            else -> {
//                shortToast(
//                    String.format(
//                        "%s %s",
//                        getString(R.string.chat_unsupported_type),
//                        qbChatDialog.type.name
//                    )
//                )
//                requireActivity().finish()
//            }
//        }
//    }
//
//    private fun joinGroupChat() {
//        progressBar.visibility = View.VISIBLE
//        ChatHelper.join(qbChatDialog, object : QBEntityCallback<Void> {
//            override fun onSuccess(result: Void?, b: Bundle?) {
//                Log.d(TAG, "Joined to Dialog Successful")
//                notifyUsersAboutCreatingDialog()
//
//                loadDialogUsers()
//            }
//
//            override fun onError(e: QBResponseException) {
//                Log.d(TAG, "Joining Dialog Error: " + e.message)
//                progressBar.visibility = View.GONE
//            }
//        })
//    }
//
//
//    private fun notifyUsersAboutCreatingDialog() {
//        if (is_new) {
//            dialogsManager.sendMessageCreatedDialog(qbChatDialog)
//            requireActivity().intent.removeExtra(EXTRA_IS_NEW_DIALOG)
//        }
//    }
//
//    private fun updateDialog(selectedUsers: ArrayList<QBUser>) {
//        ChatHelper.updateDialogUsers(
//            qbChatDialog,
//            selectedUsers,
//            object : QBEntityCallback<QBChatDialog> {
//                override fun onSuccess(dialog: QBChatDialog, args: Bundle?) {
//                    qbChatDialog = dialog
//                    loadDialogUsers()
//                }
//
//                override fun onError(e: QBResponseException) {
//                }
//            })
//    }
//
//    private fun loadDialogUsers() {
//        ChatHelper.getUsersFromDialog(qbChatDialog, object : QBEntityCallback<ArrayList<QBUser>> {
//            override fun onSuccess(users: ArrayList<QBUser>, bundle: Bundle?) {
//                setChatNameToActionBar()
//                loadChatHistory()
//            }
//
//            override fun onError(e: QBResponseException) {
//            }
//        })
//    }
//
//    private fun setChatNameToActionBar() {
//        val chatName = getDialogName(qbChatDialog)
////        supportActionBar?.title = chatName
////        supportActionBar?.setDisplayHomeAsUpEnabled(true)
////        supportActionBar?.setHomeButtonEnabled(true)
//    }
//
//    private fun loadChatHistory() {
//        ChatHelper.loadChatHistory(
//            qbChatDialog,
//            skipPagination,
//            object : QBEntityCallback<ArrayList<QBChatMessage>> {
//                override fun onSuccess(messages: ArrayList<QBChatMessage>, args: Bundle?) {
//                    // The newest messages should be in the end of list,
//                    // so we need to reverse list to show messages in the right order
//                    messages.reverse()
//                    if (checkAdapterInit) {
//                        chatAdapter.addMessages(messages)
//                    } else {
//                        checkAdapterInit = true
//                        chatAdapter.setMessages(messages)
//                        addDelayedMessagesToAdapter()
//                    }
//                    if (skipPagination == 0) {
//                        scrollMessageListDown()
//                    }
////                    readMessages(messages)
//
//                    /*   val id =
//                           if (chatViewModel.quickBloxId == messages[messages.size - 1].senderId.toString()) {
//                               messages[messages.size - 1].senderId
//                           } else messages[messages.size - 1].recipientId
//                       chatAdapter.updateStatusRead(
//                           messages[messages.size - 1].id,
//                           id
//                       )*/
//                    binding.progressBar.visibility = View.GONE
//                    BaseUtils.hideProgressbar()
//                    skipPagination += CHAT_HISTORY_ITEMS_PER_PAGE
//                }
//
//                override fun onError(e: QBResponseException) {
//                    binding.progressBar.visibility = View.GONE
//                    BaseUtils.hideProgressbar()
//                }
//            })
//
//    }
//
//    private fun readMessages(messages: ArrayList<QBChatMessage>) {
//        for (i in messages.indices) {
//            try {
//                qbChatDialog.readMessage(messages[i])
//            } catch (e: XMPPException) {
//                Log.w(TAG, e)
//            } catch (e: SmackException.NotConnectedException) {
//                Log.w(TAG, e)
//            }
//            if (i == 500) {
//                break
//            }
//        }
//    }
//
//    private fun addDelayedMessagesToAdapter() {
//        unShownMessages?.let {
//            if (it.isNotEmpty()) {
//                val chatList = chatAdapter.getMessages()
//                for (message in it) {
//                    if (!chatList.contains(message)) {
//                        chatAdapter.addMessage(message)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun scrollMessageListDown() {
//        chatMessagesRecyclerView.scrollToPosition(messagesList.size - 1)
//    }
//
//    private fun deleteChat() {
//        ChatHelper.deleteDialog(qbChatDialog, object : QBEntityCallback<Void> {
//            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
//                QbDialogHolder.deleteDialog(qbChatDialog)
//                requireActivity().setResult(Activity.RESULT_OK)
//                requireActivity().finish()
//            }
//
//            override fun onError(e: QBResponseException) {
//            }
//        })
//    }
//
//    private fun initChatConnectionListener() {
//        val rootView: View = requireActivity().findViewById(R.id.rv_chat_messages)
//        chatConnectionListener = object : VerboseQbChatConnectionListener(rootView) {
//            override fun reconnectionSuccessful() {
//                super.reconnectionSuccessful()
//                skipPagination = 0
//                if (qbChatDialog.type == QBDialogType.GROUP || qbChatDialog.type == QBDialogType.PUBLIC_GROUP) {
//                    checkAdapterInit = false
//                    // Join active room if we're in Group Chat
//                    requireActivity().runOnUiThread {
//                        joinGroupChat()
//                    }
//                }
//            }
//        }
//    }
//
//    override fun processMessageDelivered(messageID: String, dialogID: String, userID: Int?) {
//        if (qbChatDialog.dialogId == dialogID && userID != null) {
//            chatAdapter.updateStatusDelivered(messageID, userID)
//        }
//    }
//
//    override fun processMessageRead(messageID: String, dialogID: String, userID: Int?) {
//        if (qbChatDialog.dialogId == dialogID && userID != null) {
//            chatAdapter.updateStatusRead(messageID, userID)
//        }
//    }
//
//    override fun onDialogCreated(chatDialog: QBChatDialog) {
//
//    }
//
//    override fun onDialogUpdated(chatDialog: String) {
//
//    }
//
//    override fun onNewDialogLoaded(chatDialog: QBChatDialog) {
//
//    }
//
//    private inner class ChatMessageListener : QbChatDialogMessageListenerImpl() {
//        override fun processMessage(s: String, qbChatMessage: QBChatMessage, integer: Int?) {
//            Log.d(TAG, "Processing Received Message: " + qbChatMessage.body)
//            showMessage(qbChatMessage)
//        }
//    }
//
//    private inner class SystemMessagesListener : QBSystemMessageListener {
//        override fun processMessage(qbChatMessage: QBChatMessage) {
//            Log.d(TAG, "System Message Received: " + qbChatMessage.id)
//            dialogsManager.onSystemMessageReceived(qbChatMessage)
//        }
//
//        override fun processError(e: QBChatException?, qbChatMessage: QBChatMessage?) {
//            Log.d(
//                TAG,
//                "System Messages Error: " + e?.message + "With MessageID: " + qbChatMessage?.id
//            )
//        }
//    }
//
////    private inner class ImageAttachClickListener : AttachClickListener {
////        override fun onAttachmentClicked(itemViewType: Int?, view: View, attachment: QBAttachment) {
////            val url = QBFile.getPrivateUrlForUID(attachment.id)
////            AttachmentImageActivity.start(this@ChatActivity, url)
////        }
////    }
////
////    private inner class VideoAttachClickListener : AttachClickListener {
////        override fun onAttachmentClicked(itemViewType: Int?, view: View, attachment: QBAttachment) {
////            val url = QBFile.getPrivateUrlForUID(attachment.id)
////            AttachmentVideoActivity.start(this@ChatActivity, attachment.name, url)
////        }
////    }
////
////    private inner class FileAttachClickListener : AttachClickListener {
////        override fun onAttachmentClicked(itemViewType: Int?, view: View, attachment: QBAttachment) {
////            showFilePopup(itemViewType, attachment, view)
////        }
////    }
//
//    private inner class MessageLongClickListenerImpl : MessageLongClickListener {
//        override fun onMessageLongClicked(
//            itemViewType: Int?,
//            view: View,
//            chatMessage: QBChatMessage?
//        ) {
////            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
////            vibrator.vibrate(80)
//            if (chatMessage != null) {
//                if (itemViewType == TYPE_TEXT_RIGHT || itemViewType == TYPE_ATTACH_RIGHT) {
//                    Log.d(TAG, "Outgoing message LongClicked")
//                    showPopupMenu(false, view, chatMessage)
//                } else if (itemViewType == TYPE_TEXT_LEFT || itemViewType == TYPE_ATTACH_LEFT) {
//                    Log.d(TAG, "Incoming message LongClicked")
//                    showPopupMenu(true, view, chatMessage)
//                }
//            }
//        }
//    }
//
//    private inner class PaginationListener : PaginationHistoryListener {
//        override fun downloadMore() {
//            Log.w(TAG, "Download More")
//            loadChatHistory()
//        }
//    }
//
//    /* private inner class TypingStatusListener : QBChatDialogTypingListener {
//
//         private var currentTypingUserNames = ArrayList<String>()
//         private val usersTimerMap = HashMap<Int, Timer>()
//
//         override fun processUserIsTyping(dialogID: String?, userID: Int?) {
//             val currentUserID = currentUser.id
//             if (dialogID != null && dialogID == qbChatDialog.dialogId && userID != null && userID != currentUserID) {
//                 Log.d(TAG, "User $userID is typing")
//                 updateTypingInactivityTimer(dialogID, userID)
//                 val user = QbUsersHolder.getUserById(userID)
//                 if (user != null && user.fullName != null) {
//                     Log.d(TAG, "User $userID is in UsersHolder")
//                     for (value in chatViewModel.usersList) {
//                         if (user.id == value.quickblox_user_id.toInt()) {
//                             user.fullName = value.name
//                         }
//                     }
//                     addUserToTypingList(user)
//                 } else {
//                     Log.d(TAG, "User $userID not in UsersHolder")
//                     QBUsers.getUser(userID).performAsync(object : QBEntityCallback<QBUser> {
//                         override fun onSuccess(qbUser: QBUser?, bundle: Bundle?) {
//                             qbUser?.let {
//                                 Log.d(TAG, "User " + qbUser.id + " Loaded from Server")
//                                 for (value in chatViewModel.usersList) {
//                                     if ((it.id ?: 0) == value.quickblox_user_id.toInt()) {
//                                         it.fullName = value.name
//                                     }
//                                 }
//                                 QbUsersHolder.putUser(qbUser)
//                                 addUserToTypingList(qbUser)
//                             }
//                         }
//
//                         override fun onError(e: QBResponseException?) {
//                             Log.d(TAG, "Loading User Error: " + e?.message)
//                         }
//                     })
//                 }
//             }
//         }
//
//         private fun addUserToTypingList(user: QBUser) {
//             val userName = if (TextUtils.isEmpty(user.fullName)) user.login else user.fullName
//             if (!TextUtils.isEmpty(userName) && !currentTypingUserNames.contains(userName) && usersTimerMap.containsKey(
//                     user.id
//                 )
//             ) {
//                 currentTypingUserNames.add(userName)
//             }
//             typingStatus.text = makeStringFromNames()
//             typingStatus.visibility = View.VISIBLE
//         }
//
//         override fun processUserStopTyping(dialogID: String?, userID: Int?) {
//             val currentUserID = currentUser.id
//             if (dialogID != null && dialogID == qbChatDialog.dialogId && userID != null && userID != currentUserID) {
//                 Log.d(TAG, "User $userID stopped typing")
//                 stopInactivityTimer(userID)
//                 val user = QbUsersHolder.getUserById(userID)
//                 if (user != null) {
//                     removeUserFromTypingList(user)
//                 }
//             }
//         }
//
//         private fun removeUserFromTypingList(user: QBUser) {
//             val userName = user.fullName
//             userName?.let {
//                 if (currentTypingUserNames.contains(userName)) {
//                     currentTypingUserNames.remove(userName)
//                 }
//             }
//             typingStatus.text = makeStringFromNames()
//             if (makeStringFromNames().isEmpty()) {
//                 typingStatus.visibility = View.GONE
//             }
//         }
//
//         private fun updateTypingInactivityTimer(dialogID: String, userID: Int) {
//             stopInactivityTimer(userID)
//             val timer = Timer()
//             timer.schedule(object : TimerTask() {
//                 override fun run() {
//                     Log.d(
//                         "Typing Status",
//                         "User with ID $userID Did not refresh typing status. Processing stop typing"
//                     )
//                     requireActivity().runOnUiThread {
//                         processUserStopTyping(dialogID, userID)
//                     }
//                 }
//             }, TYPING_STATUS_INACTIVITY_DELAY)
//             usersTimerMap.put(userID, timer)
//         }
//
//         private fun stopInactivityTimer(userID: Int?) {
//             if (usersTimerMap.get(userID) != null) {
//                 try {
//                     usersTimerMap.get(userID)!!.cancel()
//                 } catch (ignored: NullPointerException) {
//
//                 } finally {
//                     usersTimerMap.remove(userID)
//                 }
//             }
//         }
//
//         private fun makeStringFromNames(): String {
//             var result = ""
//             val usersCount = currentTypingUserNames.size
//             if (usersCount == 1) {
//                 val firstUser = currentTypingUserNames.get(0)
//
//                 if (firstUser.length <= 20) {
//                     result = firstUser + " " + "is typing…"
//                 } else {
//                     result = firstUser.subSequence(0, 19).toString() +
//                             getString(R.string.typing_ellipsis) +
//                             " " + getString(R.string.typing_postfix_singular)
//                 }
//             } else if (usersCount == 2) {
//                 var firstUser = currentTypingUserNames.get(0)
//                 var secondUser = currentTypingUserNames.get(1)
//
//                 if ((firstUser + secondUser).length > 20) {
//                     if (firstUser.length >= 10) {
//                         firstUser = firstUser.subSequence(0, 9)
//                             .toString() + getString(R.string.typing_ellipsis)
//                     }
//
//                     if (secondUser.length >= 10) {
//                         secondUser = secondUser.subSequence(0, 9)
//                             .toString() + getString(R.string.typing_ellipsis)
//                     }
//                 }
//                 result = firstUser + " and " + secondUser + " " + "are typing…"
//
//             } else if (usersCount > 2) {
//                 var firstUser = currentTypingUserNames.get(0)
//                 var secondUser = currentTypingUserNames.get(1)
//                 val thirdUser = currentTypingUserNames.get(2)
//
//                 if ((firstUser + secondUser + thirdUser).length <= 20) {
//                     result =
//                         firstUser + ", " + secondUser + " and " + thirdUser + " " + "are typing…"
//                 } else if ((firstUser + secondUser).length <= 20) {
//                     result =
//                         firstUser + ", " + secondUser + " and " + (currentTypingUserNames.size - 2).toString() + " more " + getString(
//                             R.string.typing_postfix_plural
//                         )
//                 } else {
//                     if (firstUser.length >= 10) {
//                         firstUser = firstUser.subSequence(0, 9)
//                             .toString() + getString(R.string.typing_ellipsis)
//                     }
//                     if (secondUser.length >= 10) {
//                         secondUser = secondUser.subSequence(0, 9)
//                             .toString() + getString(R.string.typing_ellipsis)
//                     }
//                     result = firstUser + ", " + secondUser +
//                             " and " + (currentTypingUserNames.size - 2).toString() + " more " + "are typing…"
//                 }
//             }
//             return result
//         }
//     }*/
//
//    private inner class TypingStatusListener : QBChatDialogTypingListener {
//
//        override fun processUserIsTyping(dialogID: String?, userID: Int?) {
//            val id: String
//            runBlocking(Dispatchers.IO) {
//                id =
//                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
//                        .first()!!
//
//            }
//
//            val user = if (id == qbChatDialog.occupants[0].toString()) {
//                QbUsersHolder.getUserById(qbChatDialog.occupants[1])
//            } else QbUsersHolder.getUserById(qbChatDialog.occupants[0])
//
//            try {
//                for (value in chatViewModel.usersList) {
//                    if (user?.id == value.quickblox_user_id.toInt()) {
//                        user.fullName = value.name
//                    }
//                }
//            } catch (e: Exception) {
//
//            }
//
//            typingStatus.text = if (user?.fullName?.length!! <= 20) {
//                user.fullName + " " + "is typing…"
//            } else {
//                user.fullName?.subSequence(0, 19).toString() +
//                        getString(R.string.typing_ellipsis) +
//                        " " + getString(R.string.typing_postfix_singular)
//            }
//            typingStatus.visibility = View.VISIBLE
//        }
//
//        override fun processUserStopTyping(dialogID: String?, userID: Int?) {
//            typingStatus.visibility = View.GONE
//        }
//
//    }
//
//    private inner class TextInputWatcher : TextWatcher {
//
//        private var timer = Timer()
//        private var lastSendTime: Long = 0L
//
//        override fun beforeTextChanged(
//            charSequence: CharSequence?,
//            start: Int,
//            count: Int,
//            after: Int
//        ) {
//
//        }
//
//        override fun onTextChanged(
//            charSequence: CharSequence?,
//            start: Int,
//            before: Int,
//            count: Int
//        ) {
//            if (SystemClock.uptimeMillis() - lastSendTime > SEND_TYPING_STATUS_DELAY) {
//                lastSendTime = SystemClock.uptimeMillis()
//                try {
//                    qbChatDialog.sendIsTypingNotification()
//                } catch (e: XMPPException) {
//                    Log.d(TAG, e.message!!)
//                } catch (e: SmackException.NotConnectedException) {
//                    Log.d(TAG, e.message!!)
//                }
//
//            }
//        }
//
//        override fun afterTextChanged(s: Editable?) {
//            timer.cancel()
//            timer = Timer()
//            timer.schedule(object : TimerTask() {
//                override fun run() {
//                    try {
//                        qbChatDialog.sendStopTypingNotification()
//                    } catch (e: XMPPException) {
//                        Log.d(TAG, e.message!!)
//                    } catch (e: SmackException.NotConnectedException) {
//                        Log.d(TAG, e.message!!)
//                    }
//                }
//            }, TYPING_STATUS_DELAY)
//        }
//    }
//
//    fun send_Notification() {
//        val userIDs = StringifyArrayList<Int>()
//
//        val id: String
//        runBlocking(Dispatchers.IO) {
//            id =
//                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
//                    .first()!!
//
//        }
//        Log.e("TAG", "Self user id $id")
//        /*  if (qbChatDialog.occupants[0] == id.toInt()) {
//              userIDs.add(qbChatDialog.occupants[1])
//          } else userIDs.add(qbChatDialog.occupants[0])*/
//
//        userIDs.add(QBSessionManager.getInstance().sessionParameters.userId)
//
//
////        if (qbChatDialog.occupants[0] == id.toInt()) {
////            userIDs.add(qbChatDialog.occupants[1])
////        } else userIDs.add(qbChatDialog.occupants[0])
//
//        val event = QBEvent()
//        event.userIds = userIDs
//        event.environment = QBEnvironment.DEVELOPMENT
//        event.notificationType = QBNotificationType.PUSH
//        event.pushType = QBPushType.GCM
//
//        val customData = HashMap<String, Any>()
//        customData["data.message"] = "Hello QuickBlox"
//        customData["data.type"] = "First QuickBlox Push"
//
//        event.setMessage(customData)
//
//        QBPushNotifications.createEvent(event).performAsync(object : QBEntityCallback<QBEvent> {
//            override fun onSuccess(qbEvent: QBEvent?, bundle: Bundle?) {
//                // Event Created
//                Log.e("TAG", "onSuccess")
//            }
//
//            override fun onError(e: QBResponseException?) {
//                Log.e("TAG", "onError")
//            }
//        })
//    }
//
//
//}