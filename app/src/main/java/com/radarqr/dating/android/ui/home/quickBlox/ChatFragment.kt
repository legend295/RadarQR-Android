package com.radarqr.dating.android.ui.home.quickBlox

import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBMessageStatusesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBChatDialogTypingListener
import com.quickblox.chat.listeners.QBMessageStatusListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.*
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.messages.QBPushNotifications
import com.quickblox.messages.model.QBEnvironment
import com.quickblox.messages.model.QBEvent
import com.quickblox.messages.model.QBNotificationType
import com.quickblox.messages.model.QBPushType
import com.quickblox.sample.chat.kotlin.utils.qb.PaginationHistoryListener
import com.quickblox.sample.chat.kotlin.utils.qb.QbUsersHolder
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.FROM_HOME
import com.radarqr.dating.android.databinding.ActivityChatBinding
import com.radarqr.dating.android.databinding.LayoutBottomSheetUnmatchBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.chat.ChatUserFragment
import com.radarqr.dating.android.ui.home.chat.ChatUserFragment.Companion.USER_DATA
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.quickBlox.listeners.MessageLongClickListener
import com.radarqr.dating.android.ui.home.quickBlox.managers.DialogsManager
import com.radarqr.dating.android.ui.home.quickBlox.model.MatchedData
import com.radarqr.dating.android.ui.home.settings.adapter.ImageMoreAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.MatchDataRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.ReportRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.UnMatchRequest
import com.radarqr.dating.android.utility.*
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.chat.CHAT_HISTORY_ITEMS_PER_PAGE
import com.radarqr.dating.android.utility.chat.ChatHelper
import com.radarqr.dating.android.utility.chat.VerboseQbChatConnectionListener
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.environment.Environment
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smackx.muc.DiscussionHistory
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class ChatFragment :
    ConnectionBaseFragment<ActivityChatBinding>(), QBMessageStatusListener,
    DialogsManager.ManagingDialogsCallbacks, View.OnClickListener, ViewClickHandler {
    private val TAG = ChatFragment::class.java.simpleName
    lateinit var qbChatDialogNew: QBChatDialog
    var isType = true
    private var player: SimpleExoPlayer? = null
    val chatViewModel: ChatViewModel by viewModel()

    //view profile
    var itemPos = 0
    var listItem: ArrayList<String> = ArrayList()
    var newImageList: ArrayList<String> = ArrayList()
    lateinit var imageMoreAdapter: ImageMoreAdapter
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    var tag = 0

    var name = ""
    var dialog: BottomSheetDialog? = null
    var isDialogVisible = false

    //
    private lateinit var progressBar: ProgressBar
    private lateinit var messageEditText: EditText
    private lateinit var attachmentBtnChat: ImageView
    private lateinit var typingStatus: TextView
//    private var currentUser = QBUser()

    private lateinit var attachmentPreviewContainerLayout: LinearLayout
    private lateinit var chatMessagesRecyclerView: RecyclerView

    private var chatAdapter: ChatAdapter? = null
    private lateinit var chatConnectionListener: ConnectionListener

    //    private lateinit var imageAttachClickListener: ImageAttachClickListener
//    private lateinit var videoAttachClickListener: VideoAttachClickListener
//    private lateinit var fileAttachClickListener: FileAttachClickListener
    private lateinit var messageLongClickListener: MessageLongClickListenerImpl
    private lateinit var qbMessageStatusesManager: QBMessageStatusesManager

    //    private var chatMessageListener: ChatMessageListener = ChatMessageListener()
    private var dialogsManager: DialogsManager = DialogsManager()
    private var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
    private lateinit var systemMessagesManager: QBSystemMessagesManager
    private var messagesList: MutableList<QBChatMessage> = ArrayList()
    private var qbChatDialog: QBChatDialog? = null
    private var unShownMessages: ArrayList<QBChatMessage>? = null
    private var skipPagination = 0
    private var checkAdapterInit: Boolean = false
    private val preferencesHelper: PreferencesHelper by inject()
//    private val quickBloxManager: QuickBloxManager by inject()

    private var clickedPosition = -1
    private var type = ""
    private var from = ""

    //    private val chatViewModel: ChatViewModel by viewModel()
    private val playerList: TreeMap<Int, SimpleExoPlayer> = TreeMap()

    companion object {
        const val REQUEST_CODE_SELECT_PEOPLE = 752
        private const val REQUEST_CODE_ATTACHMENT = 721
        private const val PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010

        const val PROPERTY_FORWARD_USER_NAME = "origin_sender_name"

        const val EXTRA_DIALOG_ID = "dialogId"

        const val IS_IN_BACKGROUND = "is_in_background"


        const val TYPING_STATUS_DELAY = 2000L

        private const val SEND_TYPING_STATUS_DELAY: Long = 3000L

        const val MAX_MESSAGE_SYMBOLS_LENGTH = 1000
        const val MARK_UN_FAVOURITE = "Mark UnFavorite"
    }

    override fun getLayoutRes(): Int = R.layout.activity_chat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewHandler = this
        HomeActivity.activeFragment.value = this
//        skipPagination = 0
        RaddarApp.dialogId = ""
        activity?.let {
            if (it is HomeActivity) {
                it.removePadding()
                it.setHomeToolbarVisibility(isVisible = false)
                it.binding.bottomNav.bottomNav.visible(isVisible = false)
            }
        }

        if (arguments != null)
            arguments?.apply {
                clickedPosition = getInt(Constants.POSITION, -1)
                type = getString(Constants.TYPE, "")
                from = getString(Constants.FROM, "")
                if (type == FROM_HOME) {
                    val dialogId = getString(Constants.DIALOG_ID)
                    binding.tvName.text = getString(Constants.NAME) ?: ""
                    binding.imageDialogIcon.loadImage(getString(Constants.PROFILE_PIC) ?: "")
                    binding.progressBar.visible(isVisible = true)
                    dialogId?.let {
                        ChatUserFragment().signInToQB {
                            if (it)
                                QuickBloxManager.getDialogById(dialogId) { qbData ->
                                    qbData?.let {
                                        binding.progressBar.visible(isVisible = true)
                                        getUserMatches(
                                            MatchDataRequest(ArrayList<String>().apply {
                                                QuickBloxManager.getOtherUserId(qbData)
                                                    ?.let { it1 -> add(it1) }
                                            }),
                                            qbData
                                        )
                                    } ?: kotlin.run {
                                        binding.progressBar.visible(isVisible = false)
                                        requireContext().showToast("Chat data not found.")
                                        this@ChatFragment.view?.findNavController()?.popBackStack()
                                    }

                                }
                        }
                    } ?: kotlin.run {
                        requireContext().showToast("There is some error with id.")
                        this@ChatFragment.view?.findNavController()?.popBackStack()
                    }
                } else {
                    ChatUserFragment().signInToQB {
                        if (it)
                            init(if (qbChatDialog == null) getSerializable(EXTRA_DIALOG_ID)!! as QBChatDialog else qbChatDialog)
                    }
                }
            }
        else {
            ChatUserFragment().signInToQB {
                if (it)
                    init(qbChatDialog)
            }
        }
    }


    private fun getUserMatches(request: MatchDataRequest, dialog: QBChatDialog) {
        if (view != null && isAdded)
            lifecycleScope.launch {
                chatViewModel.getUserMatches(request)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                /* val data = it.data.data.replaceUserMatchesImageWithUrl(
                                     RaddarApp.getInstance().applicationContext,
                                     LinkedHashMap<String, MatchedData>()
                                 )*/

                                /*Update custom data for matched data used to show user data on chat screen*/
                                dialog.customData?.apply {
                                    fields[USER_DATA] =
                                        it.data.data[0]
                                } ?: kotlin.run {
                                    val customData = QBDialogCustomData().apply {
                                        put(
                                            USER_DATA,
                                            it.data.data[0]
                                        )
                                    }
                                    dialog.customData = customData
                                }
                                chatViewModel.qbObject = dialog
                                init(dialog)

                            }

                            is DataResult.Failure -> {
                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/match-data",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/match-data Api Error"))
                            }

                            is DataResult.Empty -> {
                            }
                        }
                    }

            }
    }

    private fun init(qb: QBChatDialog?) {
        qbChatDialog = qb
        if (!QuickBloxManager.qbChatService.isLoggedIn) {
            QuickBloxManager.connectToChat {
                loadChatHistory()
            }
        } else loadChatHistory()

        RaddarApp.dialogId = qbChatDialog?.dialogId ?: ""
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        SharedPrefsHelper.delete(IS_IN_BACKGROUND)
        Log.v(TAG, "onCreate ChatFragment on Thread ID = " + Thread.currentThread().id)


        binding.layoutProfile.moreView.rlRight.setOnClickListener(this)
        binding.layoutProfile.moreView.rlLeft.setOnClickListener(this)

        getQuickBloxData(qb)

        binding.ivMore.setOnClickListener {
            if (view != null && isAdded && isVisible)
                openBottomSheet()
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }



        if (qb != null)
            qbChatDialogNew = qb

        Log.v(TAG, "Deserialized dialog = $qbChatDialog")

        try {
            qbChatDialog?.initForChat(
                QuickBloxManager.qbChatService
            )
        } catch (e: IllegalStateException) {
            Log.d(TAG, "initForChat error. Error message is : " + e.message)
            Log.e(TAG, "Finishing $TAG. Unable to init chat")
            requireActivity().finish()
        }
        qbChatDialog?.addMessageListener(messageListener)
//        qbChatDialog?.addIsTypingListener(TypingStatusListener())


        initViews()
        initMessagesRecyclerView()
        initChatConnectionListener()
        returnListeners()
    }

    override fun onStop() {
        super.onStop()
        RaddarApp.dialogId = ""
        qbChatDialog?.removeMessageListrener(messageListener)
        qbChatDialog?.removeIsTypingListener(TypingStatusListener())
        try {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
            qbMessageStatusesManager.removeMessageStatusListener(this)
            dialogsManager.removeManagingDialogsCallbackListener(this)
        } catch (e: Exception) {

        }

        qbChatDialog = null
        chatViewModel.qbObject = QBChatDialog()
    }

    private val messageListener = object : QBChatDialogMessageListener {
        override fun processMessage(p0: String?, p1: QBChatMessage?, p2: Int?) {
            p1?.let {
                Log.d(TAG, "Processing Received Message: " + p1.body)
                showMessage(p1)
                if (type.isEmpty())
                    updateMessage(p1, p2)
            }
        }

        override fun processError(
            p0: String?,
            p1: QBChatException?,
            p2: QBChatMessage?,
            p3: Int?
        ) {
            Log.d(TAG, "Processing Error Message: " + p2?.body)
            if (!isDialogVisible) {
                isDialogVisible = true
                showAlertDialog()
            }
        }
    }

    private fun getQuickBloxData(qb: QBChatDialog?) {
        dialog = BottomSheetDialog(requireActivity())
        val data = qb?.customData?.get(USER_DATA) as MatchedData?

        if (type != FROM_HOME || binding.tvName.text.isEmpty()) {
            binding.tvName.text = data?.name ?: ""
            binding.imageDialogIcon.loadImage(data?.profile_pic)
        }

        qb?.let {
            if (qb.lastMessage == null || qb.lastMessage.isEmpty()) {
                binding.tvNoChat.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.tvNoChat.visibility = View.GONE
                binding.progressBar.visible(chatAdapter?.getMessages().isNullOrEmpty())
            }
        }

    }


    private fun returnListeners() {
        if (qbChatDialog?.isTypingListeners?.isEmpty() == true) {
            qbChatDialog?.addIsTypingListener(TypingStatusListener())
        }

        dialogsManager.addManagingDialogsCallbackListener(this)
        try {
            systemMessagesManager = QBChatService.getInstance().systemMessagesManager
            systemMessagesManager.addSystemMessageListener(systemMessagesListener)
            qbMessageStatusesManager = QBChatService.getInstance().messageStatusesManager
            qbMessageStatusesManager.addMessageStatusListener(this)
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
        }
    }

    fun showMessage(message: QBChatMessage) {
        if (isAdapterConnected()) {
            chatAdapter?.addMessage(message)
            scrollMessageListDown()
        } else {
            delayShowMessage(message)
        }
    }

    private fun isAdapterConnected(): Boolean {
        return checkAdapterInit
    }

    private fun delayShowMessage(message: QBChatMessage) {
        if (unShownMessages == null) {
            unShownMessages = ArrayList()
        }
        unShownMessages!!.add(message)
    }

    private fun initViews() {
        binding.ivChatSend.setOnClickListener {
            try {
                qbChatDialog?.sendStopTypingNotification()
            } catch (e: XMPPException) {
                Log.d(TAG, e.message!!)
            } catch (e: SmackException.NotConnectedException) {
                Log.d(TAG, e.message!!)
            }

            var text = messageEditText.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(text)) {
                if (text.length > MAX_MESSAGE_SYMBOLS_LENGTH) {
                    text = text.substring(0, MAX_MESSAGE_SYMBOLS_LENGTH)
                }
                binding.ivChatSend.isEnabled = false
                sendChatMessage(text, null)
            }
        }
        typingStatus = requireActivity().findViewById(R.id.tv_typing_status)

        messageEditText = requireActivity().findViewById(R.id.et_chat_message)
        messageEditText.addTextChangedListener(TextInputWatcher())

        progressBar = requireActivity().findViewById(R.id.progress_bar)
        attachmentPreviewContainerLayout =
            requireActivity().findViewById(R.id.ll_attachment_preview_container)

    }


    private fun initMessagesRecyclerView() {
        chatMessagesRecyclerView = requireActivity().findViewById(R.id.rv_chat_messages)

        val layoutManager = LinearLayoutManager(requireActivity())
        layoutManager.stackFromEnd = true
        chatMessagesRecyclerView.layoutManager = layoutManager

//        messagesList = ArrayList()
        chatAdapter =
            qbChatDialog?.let {
                ChatAdapter(
                    requireActivity(),
                    it,
                    messagesList,
                    QuickBloxManager,
                    preferencesHelper
                )
            }
        chatAdapter?.setPaginationHistoryListener(PaginationListener())
        chatMessagesRecyclerView.addItemDecoration(StickyRecyclerHeadersDecoration(chatAdapter))

        chatMessagesRecyclerView.adapter = chatAdapter
//        imageAttachClickListener = ImageAttachClickListener()
//        videoAttachClickListener = VideoAttachClickListener()
//        fileAttachClickListener = FileAttachClickListener()
        messageLongClickListener = MessageLongClickListenerImpl()
    }

    private fun sendChatMessage(text: String?, attachment: QBAttachment?) {
        if (!BaseUtils.isInternetAvailable()) {
            binding.ivChatSend.isEnabled = true
            CommonCode.setToast(requireContext(), resources.getString(R.string.no_internet_msg))
            return
        }
        if (QuickBloxManager.qbChatService.isLoggedIn) {
            val chatMessage = QBChatMessage()
            attachment?.let {
                chatMessage.addAttachment(it)
            } ?: run {
                chatMessage.body = text
            }


            chatMessage.setSaveToHistory(true)
            chatMessage.dateSent = System.currentTimeMillis() / 1000
            chatMessage.isMarkable = true
            chatMessage.dialogId = qbChatDialog?.dialogId

            if (qbChatDialog?.type != QBDialogType.PRIVATE && qbChatDialog?.isJoined == false) {
                qbChatDialog?.join(DiscussionHistory())
                shortToast(R.string.chat_still_joining)
                return
            }
            try {
                Log.d(TAG, "Sending Message with ID: " + chatMessage.id)
                qbChatDialog?.sendMessage(chatMessage, object : QBEntityCallback<Void> {
                    override fun onSuccess(p0: Void?, p1: Bundle?) {
                        binding.ivChatSend.isEnabled = true
                        binding.tvNoChat.visibility = View.GONE
                        sendNotification(chatMessage.body)
                        if (qbChatDialog?.type == QBDialogType.PRIVATE) {
                            showMessage(chatMessage)
                        }
                        messageEditText.setText("")
                        if (type.isEmpty())
                            updateMessage(chatMessage, chatMessage.senderId)
                    }

                    override fun onError(p0: QBResponseException?) {
                        binding.ivChatSend.isEnabled = true
                        context?.showToast("Unable to send message \n${p0?.message ?: ""}")
                    }
                })


            } catch (e: SmackException.NotConnectedException) {
                Log.w(TAG, e)
                binding.ivChatSend.isEnabled = true
                shortToast(R.string.chat_error_send_message)
            }
        } else {
            binding.ivChatSend.isEnabled = true
            binding.progressBarReLogin.visibility = View.VISIBLE
            Log.d(TAG, "Re-login to Chat")
            QuickBloxManager.connectToChat {
                if (it) {
                    binding.progressBarReLogin.visibility = View.GONE
                    sendChatMessage(text, attachment)
                } else {
                    binding.progressBarReLogin.visibility = View.GONE
                    shortToast(R.string.chat_send_message_error)
                }
            }
        }
    }


    private fun loadChatHistory() {
        qbChatDialog?.let {
            ChatHelper.loadChatHistory(
                it,
                skipPagination,
                object : QBEntityCallback<ArrayList<QBChatMessage>> {
                    override fun onSuccess(messages: ArrayList<QBChatMessage>, args: Bundle?) {
                        // The newest messages should be in the end of list,
                        // so we need to reverse list to show messages in the right order
                        messages.reverse()
                        if (skipPagination == 0)
                            messagesList.clear()
                        messagesList.addAll(messages)
//                            chatAdapter?.addMessages(messages)
                        chatAdapter?.notifyItemRangeInserted(0, messages.size)
                        if (!checkAdapterInit) {
                            checkAdapterInit = true
                            addDelayedMessagesToAdapter()
                            /*  if (skipPagination == 0)
                                  messagesList.clear()
                              messagesList.addAll(messages)
  //                            chatAdapter?.addMessages(messages)
                              chatAdapter?.notifyItemRangeInserted(0, messages.size)*/
                        } else {
//                            checkAdapterInit = true
//                            chatAdapter?.setMessages(messages)
                            /* if (skipPagination == 0)
                                 messagesList.clear()
                             messagesList.addAll(messages)
                             chatAdapter?.notifyItemRangeInserted(0, messages.size)*/
//                            addDelayedMessagesToAdapter()
                        }
                        if (skipPagination == 0) {
                            scrollMessageListDown()
                        }
                        //                    readMessages(messages)

                        /*   val id =
                                   if (chatViewModel.quickBloxId == messages[messages.size - 1].senderId.toString()) {
                                       messages[messages.size - 1].senderId
                                   } else messages[messages.size - 1].recipientId
                               chatAdapter.updateStatusRead(
                                   messages[messages.size - 1].id,
                                   id
                               )*/
                        binding.progressBar.visibility = View.GONE
                        binding.tvNoChat.visible(messagesList.isEmpty())
                        BaseUtils.hideProgressbar()
                        skipPagination += CHAT_HISTORY_ITEMS_PER_PAGE

                        updateUnReadCount()
                    }

                    override fun onError(e: QBResponseException) {
                        binding.tvNoChat.visible(isVisible = true)
                        binding.progressBar.visibility = View.GONE
                        BaseUtils.hideProgressbar()
                    }
                })
        }

    }

    private fun updateUnReadCount() {
        qbChatDialog?.apply {
            val id = QuickBloxManager.getOtherUserId(this)
            if (chatViewModel.allUserChatDialogsMap.containsKey(id)) {
                chatViewModel.allUserChatDialogsMap[id]?.unreadMessageCount = 0
            }

            if (chatViewModel.realLifeUserChatDialogsMap.containsKey(id)) {
                chatViewModel.realLifeUserChatDialogsMap[id]?.unreadMessageCount = 0
            }

            if (chatViewModel.favouritesUserChatDialogsMap.containsKey(id)) {
                chatViewModel.favouritesUserChatDialogsMap[id]?.unreadMessageCount = 0
            }
        }
    }

    private fun readMessages(messages: ArrayList<QBChatMessage>) {
        for (i in messages.indices) {
            try {
                qbChatDialog?.readMessage(messages[i])
            } catch (e: XMPPException) {
                Log.w(TAG, e)
            } catch (e: SmackException.NotConnectedException) {
                Log.w(TAG, e)
            }
            if (i == 500) {
                break
            }
        }
    }

    private fun addDelayedMessagesToAdapter() {
        unShownMessages?.let {
            if (it.isNotEmpty()) {
                val chatList = chatAdapter?.getMessages()
                for (message in it) {
                    if (chatList?.contains(message) == false) {
                        chatAdapter?.addMessage(message)
                    }
                }
            }
        }
    }

    private fun scrollMessageListDown() {
        chatMessagesRecyclerView.scrollToPosition(messagesList.size - 1)
    }

    /* private fun deleteChat() {
         ChatHelper.deleteDialog(qbChatDialog, object : QBEntityCallback<Void> {
             override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
 //                QbDialogHolder.deleteDialog(qbChatDialog)
 //                requireActivity().setResult(Activity.RESULT_OK)
 //                requireActivity().finish()
                 BaseUtils.hideProgressbar()
                 dialog?.dismiss()
                 findNavController().popBackStack()
             }

             override fun onError(e: QBResponseException) {
             }
         })
     }*/


    private fun initChatConnectionListener() {
        val rootView: View = requireActivity().findViewById(R.id.rv_chat_messages)
        chatConnectionListener = object : VerboseQbChatConnectionListener(rootView) {
            override fun reconnectionSuccessful() {
                super.reconnectionSuccessful()
                skipPagination = 0
                if (qbChatDialog?.type == QBDialogType.GROUP || qbChatDialog?.type == QBDialogType.PUBLIC_GROUP) {
                    checkAdapterInit = false
                    // Join active room if we're in Group Chat
                    requireActivity().runOnUiThread {
//                        joinGroupChat()
                    }
                }
            }
        }
    }

    override fun processMessageDelivered(messageID: String, dialogID: String, userID: Int?) {
        if (qbChatDialog?.dialogId == dialogID && userID != null) {
            chatAdapter?.updateStatusDelivered(messageID, userID)
        }
    }

    override fun processMessageRead(messageID: String, dialogID: String, userID: Int?) {
        if (qbChatDialog?.dialogId == dialogID && userID != null) {
            chatAdapter?.updateStatusRead(messageID, userID)
        }
    }

    override fun onDialogCreated(chatDialog: QBChatDialog) {

    }

    override fun onDialogUpdated(chatDialog: String) {

    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog) {

    }


    /*private inner class ChatMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(s: String, qbChatMessage: QBChatMessage, integer: Int?) {

        }

        override fun processError(
            s: String,
            e: QBChatException,
            qbChatMessage: QBChatMessage,
            integer: Int?
        ) {
            super.processError(s, e, qbChatMessage, integer)

        }
    }*/

    private fun updateMessage(qbChatMessage: QBChatMessage, integer: Int?) {
        if (view != null && isAdded && isVisible)
            qbChatDialog?.apply {
                if (from.isEmpty() || from == FROM_HOME) return
                val id = QuickBloxManager.getOtherUserId(this)?.toInt()
                chatViewModel.updateDialog(qbChatMessage, id) {}
                /*if (chatViewModel.allUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.allUserChatDialogsMap[id]?.apply {
                        lastMessage = qbChatMessage.body
                        lastMessageDateSent = qbChatMessage.dateSent
                        updatedAt = Date()
                        lastMessageUserId = integer
                    }
                    if (clickedPosition != -1 && from == ChatUserFragment.ChatType.ALL.value()) {
                        chatViewModel.allUserChatDialogs[clickedPosition] = dialog
                    }
                }

                if (chatViewModel.realLifeUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.realLifeUserChatDialogsMap[id]?.apply {
                        lastMessage = qbChatMessage.body
                        lastMessageDateSent = qbChatMessage.dateSent
                        updatedAt = Date()
                        lastMessageUserId = integer
                    }
                    if (clickedPosition != -1 && from == ChatUserFragment.ChatType.REAL_LIFE.value()) {
                        chatViewModel.realLifeUserChatDialogs[clickedPosition] = dialog
                    }
                }
                if (chatViewModel.favouritesUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.favouritesUserChatDialogsMap[id]?.apply {
                        lastMessage = qbChatMessage.body
                        lastMessageDateSent = qbChatMessage.dateSent
                        updatedAt = Date()
                        lastMessageUserId = integer
                    }
                    if (clickedPosition != -1 && from == ChatUserFragment.ChatType.FAVOURITES.value()) {
                        chatViewModel.favouriteUserChatDialogs[clickedPosition] = dialog
                    }
                }*/
            }
    }

    private fun showAlertDialog() {
        try {
            removeDialogFromMaps()
            showCustomAlert(Constants.UN_MATCH_MESSAGE, "Ok", object : DialogClickHandler<Any> {
                override fun onClick(value: Any) {
                    this@ChatFragment.view?.findNavController()?.popBackStack()
                }
            })
        } catch (e: Exception) {
            this@ChatFragment.view?.findNavController()?.popBackStack()
        }
    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            Log.d(TAG, "System Message Received: " + qbChatMessage.id)
            dialogsManager.onSystemMessageReceived(qbChatMessage)
        }

        override fun processError(e: QBChatException?, qbChatMessage: QBChatMessage?) {
            Log.d(
                TAG,
                "System Messages Error: " + e?.message + "With MessageID: " + qbChatMessage?.id
            )
        }
    }

//    private inner class ImageAttachClickListener : AttachClickListener {
//        override fun onAttachmentClicked(itemViewType: Int?, view: View, attachment: QBAttachment) {
//            val url = QBFile.getPrivateUrlForUID(attachment.id)
//            AttachmentImageActivity.start(this@ChatFragment, url)
//        }
//    }
//
//    private inner class VideoAttachClickListener : AttachClickListener {
//        override fun onAttachmentClicked(itemViewType: Int?, view: View, attachment: QBAttachment) {
//            val url = QBFile.getPrivateUrlForUID(attachment.id)
//            AttachmentVideoActivity.start(this@ChatFragment, attachment.name, url)
//        }
//    }
//
//    private inner class FileAttachClickListener : AttachClickListener {
//        override fun onAttachmentClicked(itemViewType: Int?, view: View, attachment: QBAttachment) {
//            showFilePopup(itemViewType, attachment, view)
//        }
//    }

    private inner class MessageLongClickListenerImpl : MessageLongClickListener {
        override fun onMessageLongClicked(
            itemViewType: Int?,
            view: View,
            chatMessage: QBChatMessage?
        ) {
//            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//            vibrator.vibrate(80)
            if (chatMessage != null) {
                if (itemViewType == TYPE_TEXT_RIGHT || itemViewType == TYPE_ATTACH_RIGHT) {
                    Log.d(TAG, "Outgoing message LongClicked")
//                    showPopupMenu(false, view, chatMessage)
                } else if (itemViewType == TYPE_TEXT_LEFT || itemViewType == TYPE_ATTACH_LEFT) {
                    Log.d(TAG, "Incoming message LongClicked")
//                    showPopupMenu(true, view, chatMessage)
                }
            }
        }
    }

    private inner class PaginationListener : PaginationHistoryListener {
        override fun downloadMore() {
            Log.w(TAG, "Download More")
            loadChatHistory()
        }
    }

    private inner class TypingStatusListener : QBChatDialogTypingListener {

        override fun processUserIsTyping(dialogID: String?, userID: Int?) {
            val id: String
            runBlocking(Dispatchers.IO) {
                id =
                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                        .first()!!

            }

            val user = if (id == (qbChatDialog?.occupants?.get(0)?.toString() ?: "")) {
                QbUsersHolder.getUserById(qbChatDialog?.occupants?.get(1) ?: 0)
            } else QbUsersHolder.getUserById(qbChatDialog?.occupants?.get(0) ?: 0)

            user?.let {
                try {
                    for (value in chatViewModel.allUserMatchesList) {
                        if (user.id == value.value.quickblox_user_id.toInt()) {
                            user.fullName = value.value.name
                        }
                    }
                } catch (e: Exception) {

                }

                typingStatus.text = if ((user.fullName?.length ?: 0) <= 20) {
                    user.fullName + " " + "is typing…"
                } else {
                    user.fullName?.subSequence(0, 19).toString() +
                            getString(R.string.typing_ellipsis) +
                            " " + getString(R.string.typing_postfix_singular)
                }
                if (isType) {
                    typingStatus.visibility = View.VISIBLE
                }
            }
        }

        override fun processUserStopTyping(dialogID: String?, userID: Int?) {
            typingStatus.visibility = View.GONE
        }

    }

    private inner class TextInputWatcher : TextWatcher {

        private var timer = Timer()
        private var lastSendTime: Long = 0L

        override fun beforeTextChanged(
            charSequence: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {

        }

        override fun onTextChanged(
            charSequence: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            if (SystemClock.uptimeMillis() - lastSendTime > SEND_TYPING_STATUS_DELAY) {
                lastSendTime = SystemClock.uptimeMillis()
                try {
                    qbChatDialog?.sendIsTypingNotification()
                } catch (e: XMPPException) {
                    Log.d(TAG, e.message!!)
                } catch (e: SmackException.NotConnectedException) {
                    Log.d(TAG, e.message!!)
                }

            }
        }

        override fun afterTextChanged(s: Editable?) {
            timer.cancel()
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        qbChatDialog?.sendStopTypingNotification()
                    } catch (e: XMPPException) {
                        Log.d(TAG, e.message!!)
                    } catch (e: SmackException.NotConnectedException) {
                        Log.d(TAG, e.message!!)
                    }
                }
            }, TYPING_STATUS_DELAY)
        }
    }

    private fun sendNotification(message: String) {
        val userIDs = StringifyArrayList<Int>()

        val id: String
        var name: String
        var profilePic: String
        runBlocking(Dispatchers.IO) {
            id =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                    .first() ?: "0"

            name =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_FIRSTNAME)
                    .first()
                    ?: "RadarQR"
            profilePic =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_IMAGE).first()
                    ?: "RadarQR"

        }
        if (id.isEmpty()) {
            Log.e(ChatFragment::class.java.simpleName, "Notifications-- quickblox id empty")
            return
        }
        if ((qbChatDialog?.occupants?.get(0) ?: 0) == id.toInt()) {
            userIDs.add(qbChatDialog?.occupants?.get(1) ?: 0)
        } else userIDs.add(qbChatDialog?.occupants?.get(0) ?: 0)


        val event = QBEvent()
        event.userIds = userIDs
        event.environment = when (RaddarApp.getEnvironment()) {
            Environment.PRODUCTION, Environment.RELEASE, Environment.STAGING -> QBEnvironment.PRODUCTION
            else -> QBEnvironment.DEVELOPMENT
        }
        event.notificationType = QBNotificationType.PUSH
//        event.pushType = QBPushType.GCM
        val json = JSONObject()
        try {
            json.put("message", "$name:\n$message")
            json.put("type", Constants.CHAT_MESSAGE)
            json.put(Constants.DIALOG_ID, qbChatDialogNew.dialogId)
            json.put(Constants.NAME, name)
            json.put(Constants.PROFILE_PIC, profilePic)
            json.put(Constants.IOS_APNS_KEY, Constants.IOS_APNS_VALUE)
            json.put(Constants.IOS_SOUND_KEY, Constants.IOS_SOUND_VALUE)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        event.message = json.toString()
        Log.e(ChatFragment::class.simpleName, "notification event ids = ${event.userIds}")
        Log.e(ChatFragment::class.simpleName, "notification event sent = $event")
        Log.e(ChatFragment::class.simpleName, "notification Data sent = $json")
        QBPushNotifications.createEvents(event)
            .performAsync(object : QBEntityCallback<List<QBEvent>> {
                override fun onSuccess(p0: List<QBEvent>?, p1: Bundle?) {
                    Log.e(ChatFragment::class.simpleName, "push notification sent : ${p0?.size}")
                }

                override fun onError(p0: QBResponseException?) {
                    Log.e(
                        ChatFragment::class.simpleName,
                        "push notification onError ${p0?.localizedMessage}"
                    )
                }
            })
    }


    fun openBottomSheet() {
        val layoutBinding = LayoutBottomSheetUnmatchBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        )
        qbChatDialog?.apply {
            val list: ArrayList<String>? = try {
                customData?.get(QuickBloxManager.CUSTOM_KEY_FAVOURITE_MARKED_BY) as ArrayList<String>?
            } catch (e: ClassCastException) {
                null
            }

            layoutBinding.tvFavourite.text =
                if (list.isNullOrEmpty()) "Mark Favorite"
                else if (list.contains(QuickBloxManager.qbSessionManager.sessionParameters.userId.toString())) MARK_UN_FAVOURITE
                else "Mark Favorites"
        }

        fun enableDisableButtons(isEnabled: Boolean) {
            layoutBinding.tvFavourite.isEnabled = isEnabled
            layoutBinding.tvUmatch.isEnabled = isEnabled
            layoutBinding.tvReport.isEnabled = isEnabled
            layoutBinding.tvCancel.isEnabled = isEnabled
        }
        layoutBinding.tvFavourite.setOnClickListener {
            if (!BaseUtils.isInternetAvailable()) return@setOnClickListener
            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS && layoutBinding.tvFavourite.text != MARK_UN_FAVOURITE) {
                showSubscriptionSheet(SubscriptionPopUpType.MARK_FAVORITE, popBackStack = false) {
                    dialog?.dismiss()
                }
            } else {
                qbChatDialog?.let { it1 ->
                    enableDisableButtons(isEnabled = false)
                    disableEnableChatFields(isEnabled = false)
                    layoutBinding.progressBar.visible(true)
                    QuickBloxManager.markFavUnFav(it1) { isSuccess, data ->
                        if (isSuccess) {
                            dialog?.dismiss()
                            if (type.isEmpty())
                                data?.updateCustomData()
                        } else requireContext().showToast("Unable to mark favourite")
                        layoutBinding.progressBar.visible(false)
                        enableDisableButtons(isEnabled = true)
                        disableEnableChatFields(isEnabled = true)
                    }
                }
            }
        }

        layoutBinding.tvReport.setOnClickListener {
            openReportDialog { data, subOption, child, reason ->
                if (BaseUtils.isInternetAvailable()) {
                    getUnMatchRequest()?.apply {
                        val reportRequest = ReportRequest(
                            this.receiver_id,
                            data._id,
                            suboption_id = subOption._id,
                            sub_suboption_id = child._id,
                            other_info = reason
                        )
                        disableEnableChatFields(isEnabled = false)
                        reportUser(reportRequest)
                    }
                }
            }
        }

        layoutBinding.tvUmatch.setOnClickListener {
            if (!BaseUtils.isInternetAvailable()) return@setOnClickListener
            BaseUtils.showProgressbar(requireContext())
            getUnMatchRequest().apply {
                this?.let {
                    disableEnableChatFields(isEnabled = false)
                    unMatch(this)
                } ?: BaseUtils.hideProgressbar()
            }

        }

        layoutBinding.tvCancel.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.setCancelable(false)

        dialog?.setContentView(layoutBinding.root)

        dialog?.show()

    }

    private fun QBChatDialog.updateCustomData() {
        if (view != null && isAdded && isVisible) {
            qbChatDialog = this
            qbChatDialog?.apply {
                if (from.isEmpty() || from == FROM_HOME) return
                val id = QuickBloxManager.getOtherUserId(this) ?: ""
                if (chatViewModel.allUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.allUserChatDialogsMap[id]?.apply {
                        customData = this@updateCustomData.customData
                    }
                    if (clickedPosition != -1 && from == ChatUserFragment.ChatType.ALL.value()) {
                        chatViewModel.allUserChatDialogs[clickedPosition] = dialog
                    }
                }

                if (chatViewModel.realLifeUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.realLifeUserChatDialogsMap[id]?.apply {
                        customData = this@updateCustomData.customData
                    }
                    if (clickedPosition != -1 && from == ChatUserFragment.ChatType.REAL_LIFE.value()) {
                        chatViewModel.realLifeUserChatDialogs[clickedPosition] = dialog
                    }
                }
                /*val list: ArrayList<String>? = try {
                    this@updateCustomData.customData.get(QuickBloxManager.CUSTOM_KEY_FAVOURITE_MARKED_BY) as ArrayList<String>?
                } catch (e: ClassCastException) {
                    null
                }
                if (list != null && list.isNotEmpty() && list.contains(QuickBloxManager.qbSessionManager.sessionParameters.userId.toString())) {
                    chatViewModel.favouritesUserChatDialogsMap[id] =
                        chatViewModel.allUserChatDialogsMap[id]
                    chatViewModel.favouriteUserChatDialogs.clear()
                    chatViewModel.favouriteUserChatDialogs.addAll(chatViewModel.favouritesUserChatDialogsMap.values)
                } else {
                    if (chatViewModel.favouritesUserChatDialogsMap.containsKey(id)) {
                        chatViewModel.favouritesUserChatDialogsMap.remove(id)
                    }
                    chatViewModel.favouriteUserChatDialogs.clear()
                    chatViewModel.favouriteUserChatDialogs.addAll(chatViewModel.favouritesUserChatDialogsMap.values)
                }*/
            }
        }
    }

    private fun getUnMatchRequest(): UnMatchRequest? {
        /*for (id in chatViewModel.allUserMatchesList) {
            val userListId = QuickBloxManager.getOtherUserId(chatViewModel.qbObject)
            if (id.value.quickblox_user_id == userListId) {
                return UnMatchRequest(id.value._id)
            }
        }*/
        return qbChatDialog?.let {
            val matchData = it.customData?.get("user_data")
            val id = matchData?.let { it1 -> (it1 as MatchedData)._id }
            id?.let { it1 ->
                UnMatchRequest(it1)
            } ?: kotlin.run { null }
        }
            ?: kotlin.run { null }
    }

    private fun reportUser(request: ReportRequest) {
        if (view == null) return
        lifecycleScope.launch {
            getProfileViewModel.reportUser(request = request).observe(viewLifecycleOwner) {
                when (it) {
                    DataResult.Empty -> {
                    }

                    is DataResult.Failure -> {
                        disableEnableChatFields(isEnabled = true)
                        reportApiError(
                            Exception().stackTrace[0].lineNumber,
                            it.statusCode ?: 0,
                            "user/report",
                            requireActivity().componentName.className,
                            it.message ?: ""
                        )

                        FirebaseCrashlytics.getInstance()
                            .recordException(Exception("user/report Api Error"))
                    }

                    DataResult.Loading -> {
                    }

                    is DataResult.Success -> {
                        if (it.statusCode == 200) {
                            unMatch(UnMatchRequest(request.user_id))
                        }
                    }
                }
            }
        }
    }


    private fun unMatch(request: UnMatchRequest) {
//        val list = ArrayList<QBChatDialog>()
//        list.add(chatViewModel.qbObject)
        if (BaseUtils.isInternetAvailable())
            if (view != null && isAdded && isVisible)
                lifecycleScope.launch {
                    chatViewModel.unMatch(request).observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {
                            }

                            is DataResult.Success -> {
                                qbChatDialog?.apply {
                                    QuickBloxManager.unMatch(this) { isSuccess, data ->
//                                        BaseUtils.hideProgressbar()
                                        dialog?.dismiss()
                                        disableEnableChatFields(isEnabled = true)
                                        blockUser()
                                    }
                                }
                            }

                            is DataResult.Failure -> {
                                BaseUtils.hideProgressbar()
                                dialog?.dismiss()
                                disableEnableChatFields(isEnabled = true)
                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/un-match",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/un-match Api Error"))
                            }

                            is DataResult.Empty -> {
                                dialog?.dismiss()

                            }

                        }

                    }

                }
    }

    private fun blockUser() {
        val privacyListsManager = QBChatService.getInstance().privacyListsManager
        val id = qbChatDialog?.let { QuickBloxManager.getOtherUserId(it) }
        val item = QBPrivacyListItem()
        item.isAllow = false
        item.type = QBPrivacyListItem.Type.USER_ID
        item.valueForType = id
        item.isMutualBlock = true

        var lists: List<QBPrivacyList> = ArrayList()
        try {
            lists = privacyListsManager.privacyLists
        } catch (e: SmackException.NotConnectedException) {

        } catch (e: SmackException.NoResponseException) {

        } catch (e: XMPPException) {

        }

        val privacyList: QBPrivacyList = if (lists.isEmpty()) {
            QBPrivacyList(arrayOf(item).toMutableList(), "public")
        } else {
            lists[lists.size - 1].items.add(item)
            lists[0]
        }

        try {
            try {
                privacyListsManager.createPrivacyList(privacyList)
            } catch (e: java.lang.Exception) {
            }
            privacyListsManager.applyPrivacyList("public")
        } catch (e: SmackException.NotConnectedException) {

        } catch (e: SmackException.NoResponseException) {

        } catch (e: XMPPException) {

        }

        removeDialogFromMaps()

        qbChatDialog?.let {
            QuickBloxManager.deleteDialog(it) {
                BaseUtils.hideProgressbar()
                this.view?.findNavController()?.popBackStack()
            }
        } ?: kotlin.run {
            BaseUtils.hideProgressbar()
            this.view?.findNavController()?.popBackStack()
        }
    }

    private fun removeDialogFromMaps() {
        qbChatDialog?.apply {
            val id = QuickBloxManager.getOtherUserId(this)
            chatViewModel.allUserChatDialogsMap.remove(id)
            chatViewModel.realLifeUserChatDialogsMap.remove(id)
            chatViewModel.favouritesUserChatDialogsMap.remove(id)
        }
    }


    override fun onPause() {
        super.onPause()
//        stopPlayer()
    }


    override fun onResume() {
        super.onResume()
    }

    private fun disableEnableChatFields(isEnabled: Boolean) {
        binding.ivBack.isEnabled = isEnabled
        binding.etChatMessage.isEnabled = isEnabled
        binding.ivMore.isEnabled = isEnabled
        binding.ivChatSend.isEnabled = isEnabled
        binding.progressBarMedium.visibility = if (isEnabled) View.GONE else View.VISIBLE
        binding.ivChatSend.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.image_dialog_icon, R.id.tv_name -> {
                val data = qbChatDialog?.customData?.get(USER_DATA) as MatchedData?
                data?.let {
                    val bundle = Bundle().apply {
                        putString(Constants.USER_ID, data._id)
                        putInt(Constants.FROM, ProfileFragment.FROM_CHAT)
                    }
                    binding.root.findNavController().navigate(R.id.profileFragment, bundle)
                }
            }

            R.id.rl_left -> {
                if (itemPos != 0) {
                    itemPos -= 1
                    binding.layoutProfile.moreView.rvMoreImages.scrollToPosition(itemPos)
                    imageMoreAdapter.playPlayer(itemPos)
                }
            }

            R.id.rl_right -> {
                if (itemPos < (newImageList.size - 1)) {
                    itemPos += 1
                    binding.layoutProfile.moreView.rvMoreImages.scrollToPosition(itemPos)
                    imageMoreAdapter.playPlayer(itemPos)
                }
            }
        }

    }

}
/*

 private inner class TypingStatusListener : QBChatDialogTypingListener {
     private var currentTypingUserNames = ArrayList<String>()
     private val usersTimerMap = HashMap<Int, Timer>()
     override fun processUserIsTyping(dialogID: String?, userID: Int?) {
         val currentUserID = currentUser.id
         if (dialogID != null && dialogID == qbChatDialog.dialogId && userID != null && userID != currentUserID) {
             Log.d(TAG, "User $userID is typing")
             updateTypingInactivityTimer(dialogID, userID)
             val user = QbUsersHolder.getUserById(userID)
             if (user != null && user.fullName != null) {
                 Log.d(TAG, "User $userID is in UsersHolder")
                 for (value in chatViewModel.usersList) {
                     if (user.id == value.quickblox_user_id.toInt()) {
                         user.fullName = value.name
                     }
                 }
                 addUserToTypingList(user)
             } else {
                 Log.d(TAG, "User $userID not in UsersHolder")
                 QBUsers.getUser(userID).performAsync(object : QBEntityCallback<QBUser> {
                     override fun onSuccess(qbUser: QBUser?, bundle: Bundle?) {
                         qbUser?.let {
                             Log.d(TAG, "User " + qbUser.id + " Loaded from Server")
                             for (value in chatViewModel.usersList) {
                                 if ((it.id ?: 0) == value.quickblox_user_id.toInt()) {
                                     it.fullName = value.name
                                 }
                             }
                             QbUsersHolder.putUser(qbUser)
                             addUserToTypingList(qbUser)
                         }
                     }
                     override fun onError(e: QBResponseException?) {
                         Log.d(TAG, "Loading User Error: " + e?.message)
                     }
                 })
             }
         }
     }
     private fun addUserToTypingList(user: QBUser) {
         val userName = if (TextUtils.isEmpty(user.fullName)) user.login else user.fullName
         if (!TextUtils.isEmpty(userName) && !currentTypingUserNames.contains(userName) && usersTimerMap.containsKey(
                 user.id
             )
         ) {
             currentTypingUserNames.add(userName)
         }
         typingStatus.text = makeStringFromNames()
         typingStatus.visibility = View.VISIBLE
     }
     override fun processUserStopTyping(dialogID: String?, userID: Int?) {
         val currentUserID = currentUser.id
         if (dialogID != null && dialogID == qbChatDialog.dialogId && userID != null && userID != currentUserID) {
             Log.d(TAG, "User $userID stopped typing")
             stopInactivityTimer(userID)
             val user = QbUsersHolder.getUserById(userID)
             if (user != null) {
                 removeUserFromTypingList(user)
             }
         }
     }
     private fun removeUserFromTypingList(user: QBUser) {
         val userName = user.fullName
         userName?.let {
             if (currentTypingUserNames.contains(userName)) {
                 currentTypingUserNames.remove(userName)
             }
         }
         typingStatus.text = makeStringFromNames()
         if (makeStringFromNames().isEmpty()) {
             typingStatus.visibility = View.GONE
         }
     }
     private fun updateTypingInactivityTimer(dialogID: String, userID: Int) {
         stopInactivityTimer(userID)
         val timer = Timer()
         timer.schedule(object : TimerTask() {
             override fun run() {
                 Log.d(
                     "Typing Status",
                     "User with ID $userID Did not refresh typing status. Processing stop typing"
                 )
                 requireActivity().runOnUiThread {
                     processUserStopTyping(dialogID, userID)
                 }
             }
         }, TYPING_STATUS_INACTIVITY_DELAY)
         usersTimerMap.put(userID, timer)
     }
     private fun stopInactivityTimer(userID: Int?) {
         if (usersTimerMap.get(userID) != null) {
             try {
                 usersTimerMap.get(userID)!!.cancel()
             } catch (ignored: NullPointerException) {
             } finally {
                 usersTimerMap.remove(userID)
             }
         }
     }
     private fun makeStringFromNames(): String {
         var result = ""
         val usersCount = currentTypingUserNames.size
         if (usersCount == 1) {
             val firstUser = currentTypingUserNames.get(0)
             if (firstUser.length <= 20) {
                 result = firstUser + " " + "is typing…"
             } else {
                 result = firstUser.subSequence(0, 19).toString() +
                         getString(R.string.typing_ellipsis) +
                         " " + getString(R.string.typing_postfix_singular)
             }
         } else if (usersCount == 2) {
             var firstUser = currentTypingUserNames.get(0)
             var secondUser = currentTypingUserNames.get(1)
             if ((firstUser + secondUser).length > 20) {
                 if (firstUser.length >= 10) {
                     firstUser = firstUser.subSequence(0, 9)
                         .toString() + getString(R.string.typing_ellipsis)
                 }
                 if (secondUser.length >= 10) {
                     secondUser = secondUser.subSequence(0, 9)
                         .toString() + getString(R.string.typing_ellipsis)
                 }
             }
             result = firstUser + " and " + secondUser + " " + "are typing…"
         } else if (usersCount > 2) {
             var firstUser = currentTypingUserNames.get(0)
             var secondUser = currentTypingUserNames.get(1)
             val thirdUser = currentTypingUserNames.get(2)
             if ((firstUser + secondUser + thirdUser).length <= 20) {
                 result =
                     firstUser + ", " + secondUser + " and " + thirdUser + " " + "are typing…"
             } else if ((firstUser + secondUser).length <= 20) {
                 result =
                     firstUser + ", " + secondUser + " and " + (currentTypingUserNames.size - 2).toString() + " more " + getString(
                         R.string.typing_postfix_plural
                     )
             } else {
                 if (firstUser.length >= 10) {
                     firstUser = firstUser.subSequence(0, 9)
                         .toString() + getString(R.string.typing_ellipsis)
                 }
                 if (secondUser.length >= 10) {
                     secondUser = secondUser.subSequence(0, 9)
                         .toString() + getString(R.string.typing_ellipsis)
                 }
                 result = firstUser + ", " + secondUser +
                         " and " + (currentTypingUserNames.size - 2).toString() + " more " + "are typing…"
             }
         }
         return result
     }
 }

private fun stopAllPlayers() {
        for (player in playerList.values) {
            player.playWhenReady = false
        }
    }

    private fun checkVisiblePortion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.layoutProfile.scScroll.setOnScrollChangeListener(
                BaseUtils.scrollListener(
                    scrollView = binding.layoutProfile.scScroll,
                    list = getImageViewsWithProgress(),
                    recyclerTopView = binding.layoutProfile.moreView.viewTop,
                    recyclerBottomView = binding.layoutProfile.moreView.viewBottom
                ) {
//                    if (playerList.isEmpty() && this::imageMoreAdapter.isInitialized) return@scrollListener
                    when (it) {
                        6 -> {
                            stopAllPlayers()

                            try {
                                if (this::imageMoreAdapter.isInitialized)
                                    imageMoreAdapter.playPlayer(itemPos)
                            } catch (e: Exception) {

                            }
                        }
                        else -> playPlayer(it)
                    }
                })
        }
    }

    private fun playPlayer(position: Int) {
        try {
            if (this::imageMoreAdapter.isInitialized)
                imageMoreAdapter.playPlayer(itemPos)
        } catch (e: Exception) {

        }
        if (playerList.isNotEmpty()) {
            for (pos in 0..6) {
                if (pos == position) {
                    if (playerList.containsKey(pos)) {
                        playerList[pos]?.playWhenReady = true
                    }
                } else {
                    if (playerList.containsKey(pos)) {
                        playerList[pos]?.playWhenReady = false
                    }
                }
            }
        }
    }

    private fun setImage(list: List<String>) {
        for (position in list.indices) {
            if (position >= 6) return
            getImageViewsWithProgress()[position].cardView.visibility = View.VISIBLE
            getImageViewsWithProgress()[position].ivVolume.visibility =
                if (!list[position].contains(Constants.MP4)) View.GONE else View.VISIBLE
            getImageViewsWithProgress()[position].videoView.visibility =
                if (!list[position].contains(Constants.MP4)) View.GONE else View.VISIBLE
            getImageViewsWithProgress()[position].progressBarVideo.visibility =
                if (!list[position].contains(Constants.MP4)) View.GONE else View.VISIBLE

            val image = BaseUtils.getImageUrl(requireActivity(), list[position])
            if (!list[position].contains(Constants.MP4)) {

                Glide.with(binding.root).load(image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            getImageViewsWithProgress()[position].progressBar.visibility =
                                View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            getImageViewsWithProgress()[position].progressBar.visibility =
                                View.GONE
                            return false
                        }
                    }).into(getImageViewsWithProgress()[position].ivImage)
                Utility.zoomImage(
                    requireActivity(),
                    getImageViewsWithProgress()[position].ivImage
                )
            } else {

                initializePlayer(
                    Uri.parse(image),
                    getImageViewsWithProgress()[position].videoView,
                    position
                )

                getImageViewsWithProgress()[position].progressBar.visibility = View.GONE
            }
        }
    }

    private fun getImageViewsWithProgress(): ArrayList<LayoutImageVideoViewBinding> {
        val list = ArrayList<LayoutImageVideoViewBinding>()
        list.add(binding.layoutProfile.firstView)
        list.add(binding.layoutProfile.secondView)
        list.add(binding.layoutProfile.thirdView)
        list.add(binding.layoutProfile.forthView)
        list.add(binding.layoutProfile.fifthView)
        list.add(binding.layoutProfile.sixthView)
        BaseUtils.handleClick(list, playerList)
        return list
    }

    private fun initializePlayer(uri: Uri, videoView: PlayerView, position: Int) {
        player = SimpleExoPlayer.Builder(requireActivity())
            .build()
            .also { exoPlayer ->
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        player!!.volume = 0f

        if (position != 0)
            player!!.playWhenReady = false

        playerList[position] = player!!
        playerList[position]?.addListener(
            BaseUtils.playerListener(
                position,
                getImageViewsWithProgress()[position].progressBarVideo
            ) {
                handleVideoError()
            }
        )
    }

    private fun handleVideoError() {
//        getProfile()
    }

    private fun stopPlayer() {
        stopAllPlayers()
        try {
            if (this::imageMoreAdapter.isInitialized)
                imageMoreAdapter.stopAllPlayers()
        } catch (e: Exception) {

        }
    }

     private fun showPopupMenu(
        isIncomingMessageClicked: Boolean,
        view: View,
        chatMessage: QBChatMessage
    ) {
//        val popupMenu = PopupMenu(this@ChatFragment, view)
//
//        popupMenu.menuInflater.inflate(R.menu.menu_message_longclick, popupMenu.menu)
//        popupMenu.gravity = Gravity.RIGHT
//
//        if (isIncomingMessageClicked || (qbChatDialog.type != QBDialogType.GROUP)) {
//            popupMenu.menu.removeItem(R.id.menu_message_delivered_to)
//            popupMenu.menu.removeItem(R.id.menu_message_viewed_by)
//            popupMenu.gravity = Gravity.LEFT
//        }
//
//        popupMenu.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.menu_message_forward -> {
//                    startForwardingMessage(chatMessage)
//                }
//                R.id.menu_message_delivered_to -> {
//                    showDeliveredToScreen(chatMessage)
//                }
//                R.id.menu_message_viewed_by -> {
//                    Log.d(TAG, "Viewed by")
//                    showViewedByScreen(chatMessage)
//                }
//            }
//            true
//        }
//        popupMenu.show()
    }

    private fun showFilePopup(itemViewType: Int?, attachment: QBAttachment?, view: View) {
//        val popupMenu = PopupMenu(this@ChatFragment, view)
//        popupMenu.menuInflater.inflate(R.menu.menu_file_popup, popupMenu.menu)
//
//        if (itemViewType == TYPE_TEXT_RIGHT || itemViewType == TYPE_ATTACH_RIGHT) {
//            popupMenu.gravity = Gravity.RIGHT
//        } else if (itemViewType == TYPE_TEXT_LEFT || itemViewType == TYPE_ATTACH_LEFT) {
//            popupMenu.gravity = Gravity.LEFT
//        }
//
//        popupMenu.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.menu_file_save -> {
//                    saveFileToStorage(attachment)
//                }
//            }
//            true
//        }
//        popupMenu.show()
    }

    private fun saveFileToStorage(attachment: QBAttachment?) {
        val file = File(requireActivity().application.filesDir, attachment?.name)
        val url = QBFile.getPrivateUrlForUID(attachment?.id)
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.name)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.allowScanningByMediaScanner()
        val manager =
            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun startForwardingMessage(message: QBChatMessage) {
//        ForwardToActivity.start(this, message)
    }

    private fun showDeliveredToScreen(message: QBChatMessage) {
//        MessageInfoActivity.start(this, message, MESSAGE_INFO_DELIVERED_TO)

    }

    private fun showViewedByScreen(message: QBChatMessage) {
//        MessageInfoActivity.start(this, message, MESSAGE_INFO_READ_BY)
    }

    private fun updateDialog() {
//        showProgressDialog(R.string.dlg_updating)
//        Log.d(TAG, "Starting Dialog Update")
//        ChatHelper.getDialogById(qbChatDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
//            override fun onSuccess(updatedChatDialog: QBChatDialog, bundle: Bundle) {
//                Log.d(TAG, "Update Dialog Successful: " + updatedChatDialog.dialogId)
//                qbChatDialog = updatedChatDialog
//
////                ChatInfoActivity.start(this@ChatFragment, qbChatDialog)
//            }
//
//            override fun onError(e: QBResponseException) {
//                Log.d(TAG, "Dialog Loading Error: " + e.message)
//
//                showErrorSnackbar(R.string.select_users_get_dialog_error, e, null)
//            }
//        })
    }

    */
/*private fun sendDialogId() {
        val intent = Intent().putExtra(EXTRA_DIALOG_ID, qbChatDialog.dialogId)
        requireActivity().setResult(Activity.RESULT_OK, intent)
    }

      private fun leaveGroupChat() {
          dialogsManager.sendMessageLeftUser(qbChatDialog)
          dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, qbChatDialog)
          try {
              // Its a hack to give the Chat Server more time to process the message and deliver them
              Thread.sleep(300)
          } catch (e: InterruptedException) {
              e.printStackTrace()
          }


          Log.d(TAG, "Leaving Dialog")
          ChatHelper.exitFromDialog(qbChatDialog, object : QBEntityCallback<QBChatDialog> {
              override fun onSuccess(qbDialog: QBChatDialog, bundle: Bundle?) {
                  Log.d(TAG, "Leaving Dialog Successful: " + qbDialog.dialogId)

                  QbDialogHolder.deleteDialog(qbDialog)
                  BaseUtils.hideProgressbar()
                  dialog?.dismiss()
                  findNavController().popBackStack()
              }

              override fun onError(e: QBResponseException) {
                  Log.d(TAG, "Leaving Dialog Error: " + e.message)

              }
          })
      }
      */


/*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult with resultCode: $resultCode requestCode: $requestCode")
        if (resultCode == Activity.RESULT_OK) {
          /*  if (requestCode == REQUEST_CODE_SELECT_PEOPLE && data != null) {
                progressBar.visibility = View.VISIBLE
                val selectedUsers =
                    data.getSerializableExtra(EXTRA_QB_USERS) as ArrayList<QBUser>
                val existingOccupants = qbChatDialog?.occupants
                val newUserIds = ArrayList<Int>()

                for (user in selectedUsers) {
                    if (!existingOccupants?.contains(user.id)!!) {
                        newUserIds.add(user.id)
                    }
                }

                ChatHelper.getDialogById(
                    qbChatDialog?.dialogId.toString(),
                    object : QBEntityCallback<QBChatDialog> {
                        override fun onSuccess(qbChatDialog: QBChatDialog, p1: Bundle?) {
                            progressBar.visibility = View.GONE
                            dialogsManager.sendMessageAddedUsers(qbChatDialog, newUserIds)
                            dialogsManager.sendSystemMessageAddedUser(
                                systemMessagesManager,
                                qbChatDialog,
                                newUserIds
                            )
                            qbChatDialog.let {
                                this@ChatFragment.qbChatDialog = it
                            }
//                            updateDialog(selectedUsers)
                        }

                        override fun onError(e: QBResponseException?) {
                            progressBar.visibility = View.GONE

                        }
                    })
            }*/
        }
    }*/
