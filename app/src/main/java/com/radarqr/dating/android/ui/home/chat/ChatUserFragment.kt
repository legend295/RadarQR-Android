package com.radarqr.dating.android.ui.home.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quickblox.chat.QBChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBPrivacyListListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBPrivacyList
import com.quickblox.chat.model.QBPrivacyListItem
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.base.HomeBaseFragment
import com.radarqr.dating.android.databinding.FragmentChatUserBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.hotspots.helpers.showUnMatchConfirmation
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.chat.adapter.ConnectionsAdapter
import com.radarqr.dating.android.ui.home.chat.container.AllConnectionsFragment
import com.radarqr.dating.android.ui.home.chat.container.FavouritesConnectionFragment
import com.radarqr.dating.android.ui.home.chat.container.RealLifeConnectionsFragment
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.quickBlox.managers.DialogsManager
import com.radarqr.dating.android.ui.home.quickBlox.model.MatchedData
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.ReportRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.UnMatchRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.chat.ChatHelper
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatUserFragment : HomeBaseFragment<FragmentChatUserBinding>(),
    ViewClickHandler {
    private val TAG = ChatUserFragment::class.java.simpleName
    private var fragment: Fragment? = null

    val chatViewModel: ChatViewModel by viewModel()

    //    private var allConnectionFragment = AllConnectionsFragment(this)
//    private var realLifeConnectionsFragment = RealLifeConnectionsFragment(this)
//    private var favouritesConnectionFragment = FavouritesConnectionFragment(this)
    private var chatType = ChatType.ALL
    private var dialog: BottomSheetDialog? = null

    private val dialogManager = DialogsManager()

    private val preferencesHelper: PreferencesHelper by inject()

    private val getProfileViewModel: GetProfileViewModel by viewModel()


    companion object {
        const val USER_DATA = "user_data"
    }

    override fun getLayoutRes(): Int = R.layout.fragment_chat_user

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        binding.clickHandler = this
        handleView(chatType)
    }

    override fun onStop() {
        super.onStop()
        removeListeners()
        (activity as HomeActivity?)?.progressBarVisible(isVisible = false)
    }

    override fun onPause() {
        super.onPause()
        (activity as HomeActivity?)?.progressBarVisible(isVisible = false)
    }

    override fun onResume() {
        super.onResume()
//        (activity as HomeActivity?)?.progressBarVisible(isVisible = (QuickBloxManager.qbSessionManager.activeSession == null || !QuickBloxManager.qbChatService.isLoggedIn))
        signInToQB { }
    }

    private fun init() {
        implementInterfaces()
    }

    private fun implementInterfaces() {
        chatViewModel.handler = object : ChatViewModel.Handler {
            override fun signInToQB(isSuccess: (Boolean) -> Unit) {
                this@ChatUserFragment.signInToQB(isSuccess = isSuccess)
            }

            override fun markFavourite(
                qbData: QBChatDialog,
                clickedPosition: Int,
                from: String,
                text: String,
                callBack: () -> Unit
            ) {
                this@ChatUserFragment.markFavourite(qbData, clickedPosition, from, text, callBack)
            }

            override fun unMatchChat(qbChatDialog: QBChatDialog, callBack: () -> Unit) {
                showUnMatchConfirmation {
                    this@ChatUserFragment.unMatchChat(qbChatDialog, callBack)
                }
            }
        }
    }

    fun signInToQB(isError: Boolean = false, isSuccess: (Boolean) -> Unit) {
        if (BaseUtils.isInternetAvailable()) {
            if (HomeActivity.userMobileNumber.isEmpty()) return
            if (QuickBloxManager.qbSessionManager.activeSession == null || QuickBloxManager.qbSessionManager.sessionParameters?.userLogin != HomeActivity.userMobileNumber || isError) {
                QuickBloxManager.signIn(HomeActivity.userMobileNumber) { user, exception ->
                    (activity as HomeActivity?)?.progressBarVisible(isVisible = user == null)
                    user?.let {
                        connectToChat(isSuccess)
                    } ?: kotlin.run {
                        isSuccess(false)
                        if (exception != null && exception.httpStatusCode == 401 && exception.localizedMessage == "Unauthorized") {
                            QuickBloxManager.qbSessionManager.deleteSessionParameters()
                            QuickBloxManager.qbSessionManager.deleteActiveSession()
                            return@signIn
                        }
                        QuickBloxManager.signOut {
                            signInToQB(isError = true, isSuccess)
                        }
                    }
                }
            } else connectToChat(isSuccess)
        }
    }

    private fun connectToChat(isSuccess: (Boolean) -> Unit) {
        QuickBloxManager.connectToChat {
            if (HomeActivity.currentDestination?.id == R.id.dialogs_fragment)
                (activity as HomeActivity?)?.progressBarVisible(isVisible = !it)
            if (it) {
                (activity as HomeActivity?)?.setUnReadMessageCount()
                registerListeners()
                isSuccess(true)
            } else {
                signInToQB(true, isSuccess)
            }
        }


    }

    private fun registerListeners() {
        QuickBloxManager.qbChatService.incomingMessagesManager?.addDialogMessageListener(
            qbChatDialogMessageListener
        )
        QuickBloxManager.qbChatService.privacyListsManager?.addPrivacyListListener(
            qbPrivacyListListener
        )
        dialogManager.addManagingDialogsCallbackListener(managingDialogListener)
    }

    private fun removeListeners() {
        QuickBloxManager.qbChatService.incomingMessagesManager?.removeDialogMessageListrener(
            qbChatDialogMessageListener
        )
        QuickBloxManager.qbChatService.privacyListsManager?.removePrivacyListListener(
            qbPrivacyListListener
        )
        dialogManager.removeManagingDialogsCallbackListener(managingDialogListener)
    }

    fun markFavourite(
        qbData: QBChatDialog,
        clickedPosition: Int,
        from: String,
        text: String,
        callBack: () -> Unit
    ) {
        if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS && text != ConnectionsAdapter.REMOVE_FROM_FAVORITES) {
            showSubscriptionSheet(SubscriptionPopUpType.MARK_FAVORITE, popBackStack = false) {

            }
        } else {
            BaseUtils.showProgressbar(requireContext())
            QuickBloxManager.markFavUnFav(qbData) { isSuccess, data ->
                if (isSuccess) {
                    data?.updateCustomData(clickedPosition, from, callBack)
                } else requireContext().showToast("Unable to mark favourite")
                BaseUtils.hideProgressbar()
            }
        }
    }

    private fun QBChatDialog.updateCustomData(
        clickedPosition: Int,
        from: String,
        callBack: () -> Unit
    ) {
        if (view != null && isAdded) {
            apply {
                val id = QuickBloxManager.getOtherUserId(this) ?: ""
                if (chatViewModel.allUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.allUserChatDialogsMap[id]?.apply {
                        customData = this@updateCustomData.customData
                    }
                    if (clickedPosition != -1 && from == ChatType.ALL.value()) {
                        chatViewModel.allUserChatDialogs[clickedPosition] = dialog
                    }
                }

                if (chatViewModel.realLifeUserChatDialogsMap.containsKey(id)) {
                    val dialog = chatViewModel.realLifeUserChatDialogsMap[id]?.apply {
                        customData = this@updateCustomData.customData
                    }
                    if (clickedPosition != -1 && from == ChatType.REAL_LIFE.value()) {
                        chatViewModel.realLifeUserChatDialogs[clickedPosition] = dialog
                    }
                }
                callBack()
            }
        }
    }

    fun unMatchChat(qbChatDialog: QBChatDialog, callBack: () -> Unit) {
        if (view != null && isAdded) {
            BaseUtils.showProgressbar(requireContext())
            chatViewModel.getUnMatchRequest(qbChatDialog)?.apply {
                unMatch(this, qbChatDialog, callBack)
            }
        }
    }

    fun openBottomSheet(position: Int, chatDialog: QBChatDialog) {
        dialog = BottomSheetDialog(requireActivity())

        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_unmatch, null)

        val tvUnMatch = view.findViewById<TextView>(R.id.tv_umatch)
        val tvReport = view.findViewById<TextView>(R.id.tv_report)
        val tvCancel = view.findViewById<TextView>(R.id.tv_cancel)

        tvReport.setOnClickListener {
            dialog?.dismiss()
            openReportDialog { data, subOption, child, reason ->
                val userData = chatDialog.customData[USER_DATA] as MatchedData?
                userData ?: return@openReportDialog
                val reportRequest = ReportRequest(
                    userData._id,
                    data._id,
                    suboption_id = subOption._id,
                    sub_suboption_id = child._id,
                    other_info = reason
                )
                Log.d("Request", "$reportRequest")
                /* reportUser(reportRequest) {
                     unMatch(chatDialog)
                 }*/
            }
        }

        tvUnMatch.setOnClickListener {
            BaseUtils.showProgressbar(requireContext())
            unMatch(chatDialog)
        }

        tvCancel.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.setCancelable(true)

        dialog?.setContentView(view)

        dialog?.show()

    }

    private fun unMatch(chatDialog: QBChatDialog) {
        val data = chatDialog.customData[USER_DATA] as MatchedData?
        data ?: return
        unMatch(UnMatchRequest(data._id), chatDialog, data.quickblox_user_id)
    }

    private fun reportUser(request: ReportRequest, qbChatDialog: QBChatDialog) {
        if (view == null) return
        lifecycleScope.launch {
            getProfileViewModel.reportUser(request = request).observe(viewLifecycleOwner) {
                when (it) {
                    DataResult.Empty -> {
                    }

                    is DataResult.Failure -> {
                    }

                    DataResult.Loading -> {
                    }

                    is DataResult.Success -> {
                        if (it.statusCode == 200) {
//                            unMatch(UnMatchRequest(request.user_id))
                        }
                    }
                }
            }
        }
    }

    private fun unMatch(request: UnMatchRequest, qbChatDialog: QBChatDialog, callBack: () -> Unit) {
        if (BaseUtils.isInternetAvailable())
            if (view != null && isAdded && isVisible)
                lifecycleScope.launch {
                    chatViewModel.unMatch(request).observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {
                            }

                            is DataResult.Success -> {
                                qbChatDialog.apply {
                                    QuickBloxManager.unMatch(this) { isSuccess, data ->
                                        dialog?.dismiss()
                                        blockUser(qbChatDialog, callBack)
                                    }
                                }
                            }

                            is DataResult.Failure -> {
                                BaseUtils.hideProgressbar()
                                dialog?.dismiss()
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

    private fun unMatch(request: UnMatchRequest, chatDialog: QBChatDialog, id: String) {
        val list = ArrayList<QBChatDialog>()
        list.add(chatDialog)
        lifecycleScope.launch {
            chatViewModel.unMatch(request)
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            dialog?.dismiss()
                            /*try {
                                if (chatViewModel.imageUrlHasMap.containsKey(id)) {
                                    chatViewModel.imageUrlHasMap.remove(id)
                                }
                                if (chatViewModel.imageUrlHasMap.isEmpty()) {
                                    runBlocking {
                                        preferencesHelper.removeChatUserImage()
                                    }
                                } else {
                                    runBlocking {
                                        preferencesHelper.saveChatUserImage(
//                                    chatViewModel.imageHasMap,
                                            chatViewModel.imageUrlHasMap
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                            }*/
//                            blockUser()
//                            getUserChatList()
                        }

                        is DataResult.Failure -> {
                            dialog?.dismiss()
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

    private fun blockUser(qbChatDialog: QBChatDialog, callBack: () -> Unit) {
        val privacyListsManager = QBChatService.getInstance().privacyListsManager
        val id = qbChatDialog.let { QuickBloxManager.getOtherUserId(it) }
        val item = QBPrivacyListItem()
        item.isAllow = false
        item.type = QBPrivacyListItem.Type.USER_ID
        item.valueForType = id
        item.isMutualBlock = true

        var lists: List<QBPrivacyList> = java.util.ArrayList()
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

        chatViewModel.removeDialogFromMaps(qbChatDialog)

        qbChatDialog.let {
            QuickBloxManager.deleteDialog(it) {
                BaseUtils.hideProgressbar()
                callBack()
            }
        }
    }


    private fun deletePrivateDialogs(privateDialogsToDelete: List<QBChatDialog>, id: String) {
        ChatHelper.deletePrivateDialogs(
            privateDialogsToDelete,
            object : QBEntityCallback<ArrayList<String>> {
                override fun onSuccess(dialogsIds: ArrayList<String>, bundle: Bundle?) {
                    dialog?.dismiss()
                    /*try {
                        if (chatViewModel.imageUrlHasMap.containsKey(id)) {
                            chatViewModel.imageUrlHasMap.remove(id)
                        }
                        if (chatViewModel.imageUrlHasMap.isEmpty()) {
                            runBlocking {
                                preferencesHelper.removeChatUserImage()
                            }
                        } else {
                            runBlocking {
                                preferencesHelper.saveChatUserImage(
//                                    chatViewModel.imageHasMap,
                                    chatViewModel.imageUrlHasMap
                                )
                            }
                        }
                    } catch (e: Exception) {
                    }*/
//                    getUserChatList()
                }

                override fun onError(e: QBResponseException) {
                    dialog?.dismiss()
                    BaseUtils.hideProgressbar()
//                    getUserChatList()

                }
            })
    }

    private fun handleView(type: ChatType) {
        this.chatType = type
        binding.type = type

        Handler(Looper.getMainLooper()).postDelayed({
            loadFragment()
        }, 100)
    }

    private fun loadFragment() {
        if (view != null && isAdded) {
            fragment = when (chatType) {
                ChatType.ALL -> AllConnectionsFragment()
                ChatType.REAL_LIFE -> RealLifeConnectionsFragment()
                ChatType.FAVOURITES -> FavouritesConnectionFragment()
            }
            fragment?.let {
                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.container, it)
                transaction.commit()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvAllConnections -> {
                if (chatType != ChatType.ALL) {
                    chatType = ChatType.ALL
                    handleView(chatType)
                }
            }

            R.id.tvRealLife -> {
                if (chatType != ChatType.REAL_LIFE) {
                    chatType = ChatType.REAL_LIFE
                    handleView(chatType)
                }
            }

            R.id.tvFavourites -> {
                if (chatType != ChatType.FAVOURITES) {
                    chatType = ChatType.FAVOURITES
                    handleView(chatType)
                }
            }
        }
    }

    private val qbChatDialogMessageListener = object : QBChatDialogMessageListener {
        override fun processMessage(p0: String?, p1: QBChatMessage?, p2: Int?) {
            Log.d(TAG, "processMessage $p0, $p1, $p2")
            if (this@ChatUserFragment.view != null) chatViewModel.dataListener?.incomingMessageListener(
                p0,
                p1,
                p2
            )
        }

        override fun processError(p0: String?, p1: QBChatException?, p2: QBChatMessage?, p3: Int?) {
            Log.e(TAG, "processError")
        }
    }

    private val qbPrivacyListListener = object : QBPrivacyListListener {
        override fun setPrivacyList(p0: String?, p1: MutableList<QBPrivacyListItem>?) {

        }

        override fun updatedPrivacyList(p0: String?) {

        }
    }

    private val managingDialogListener = object : DialogsManager.ManagingDialogsCallbacks {
        override fun onDialogCreated(chatDialog: QBChatDialog) {
            Log.d("TAG", "onDialogCreated")
            if (this@ChatUserFragment.view != null) chatViewModel.dataListener?.onDialogCreated()
        }

        override fun onDialogUpdated(chatDialog: String) {
            Log.d("TAG", "onDialogUpdated")
            if (this@ChatUserFragment.view != null) chatViewModel.dataListener?.onDialogCreated()
        }

        override fun onNewDialogLoaded(chatDialog: QBChatDialog) {
            Log.d("TAG", "onNewDialogLoaded")
        }
    }


    enum class ChatType : ChatViewModel.ChatTypeInterface {
        ALL {
            override fun value(): String = "all"
        },
        REAL_LIFE {
            override fun value(): String = "real_life"
        },
        FAVOURITES {
            override fun value(): String = "favourites"
        }
    }


}

/*
*
    private lateinit var allDialogsAdapter: DialogsActivityAdapter
    private lateinit var onlineDialogsAdapter: DialogsActivityAdapter
    private lateinit var inPersonDialogsAdapter: DialogsActivityAdapter

    var fragment: Fragment? = null
    var allConnectionFragment = AllConnectionsFragment()
    var realLifeConnectionsFragment = RealLifeConnectionsFragment()
    var favouritesConnectionFragment = FavouritesConnectionFragment()
    var chatType = ChatType.ALL

    var fragList: ArrayList<String> = ArrayList()
    var mobile = ""
    var quick_blox_id = ""
    var dialog: BottomSheetDialog? = null
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    lateinit var thread: Thread

    var chatDataHandler: ChatUpdateHandler? = null

    private val tabIcons = intArrayOf(
        R.drawable.ic_fill_color,
        R.drawable.ic_view
    )


    var name = ""
    private val preferencesHelper: PreferencesHelper by inject()
    private val getProfileViewModel: GetProfileViewModel by viewModel()

    private var isProcessingResultInProgress: Boolean = false
    private lateinit var pushBroadcastReceiver: BroadcastReceiver
    private lateinit var chatConnectionListener: ConnectionListener
    private var allDialogsMessagesListener: QBChatDialogMessageListener =
        AllDialogsMessageListener()
    private var systemMessagesListener: SystemMessagesListener =
        SystemMessagesListener()
    private lateinit var systemMessagesManager: QBSystemMessagesManager
    private lateinit var incomingMessagesManager: QBIncomingMessagesManager
    private var dialogsManager: DialogsManager = DialogsManager()
    private lateinit var currentUser: QBUser
    private var currentActionMode: ActionMode? = null
    private var hasMoreDialogs = true
    private val joinerTasksSet = HashSet<DialogJoinerAsyncTask>()

    companion object {
        private const val REQUEST_SELECT_PEOPLE = 174
        private const val REQUEST_DIALOG_ID_FOR_UPDATE = 165
        private const val PLAY_SERVICES_REQUEST_CODE = 9000

        var newTab: TabLayout? = null

    }


    override fun getLayoutRes(): Int = R.layout.fragment_chat_user

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clickHandler = this
        runBlocking {

            mobile =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_MOBILE).first()
                    ?: ""
            quick_blox_id =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                    .first()
                    ?: ""
            chatViewModel.areImagesStored =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_ARE_IMAGES_STORED)
                    .first() ?: ""
        }

        /* if (!BaseUtils.isInternetAvailable()) {
             binding.llChatEmpty.visibility = View.VISIBLE
             binding.tvViewProfile.text = resources.getString(R.string.no_internet_msg)
             return
         }*/

//        newTab = binding.tabLayout
        chatViewModel.mobile = mobile
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        chatViewModel.areBadgesApplied = false
        if (ChatHelper.getCurrentUser() != null) {
            currentUser = ChatHelper.getCurrentUser()!!
        }


        showToolbarLayout(false)
        setObserver()
        showNavigation(true)
        initializeAdapters()
        initializeTab()

        clickHandler()


        init()


        initConnectionListener()
        checkPlayServicesAvailable()
        registerQbChatListeners()

        binding.tvNoInternet.setOnClickListener {
            initializeLogin()
        }

        initializeLogin()
    }

    private fun initializeLogin() {
        if (chatViewModel.isDataAvailable) {
            binding.progressBar.visibility = View.GONE
            setBadge(true)
            getUserChatList()
        } else {
            binding.progressBar.visibility = View.VISIBLE
            if (quick_blox_id == "" || !SharedPrefsHelper.hasQbUser()) {
                login()
            } else {
                reLogin()
            }
        }
    }

    private fun init() {
        /*binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (chatViewModel.areWeSearching)
                    filterArrayList(newText = newText ?: "")
                return true
            }
        })*/
    }

    private fun clickHandler() {
        /*binding.tvCancel.setOnClickListener {
            binding.llSearch.visibility = View.GONE
            binding.searchView.setQuery("", true)
            chatViewModel.areWeSearching = false
        }

        binding.ivSearch.setOnClickListener {
            binding.llSearch.visibility = View.VISIBLE
            chatViewModel.areWeSearching = true
        }
*/
        binding.tvViewProfile.setOnClickListener {
            findNavController().navigate(R.id.home_fragment)
        }
    }

    private fun filterArrayList(newText: String) {
        if (newText == "") {
            chatViewModel.didWeSearchedSomething = false
            chatViewModel.filteredChatDialogs = chatViewModel.userChatDialogs
            notifyAdapters()
        } else {
            chatViewModel.didWeSearchedSomething = true
            val testList: ArrayList<QBChatDialog> = ArrayList()
            for (filteredValue in chatViewModel.userChatDialogs) {
                if (filteredValue.customData != null) {
                    if (filteredValue.customData.fields["occupant_name"].toString().trim()
                            .contains(newText.trim(), ignoreCase = true)
                    ) {
                        testList.add(filteredValue)
                    }
                } else {
                    if (filteredValue.name.toString().trim()
                            .contains(newText.trim(), ignoreCase = true)
                    ) {
                        testList.add(filteredValue)
                    }
                }
            }
            chatViewModel.filteredChatDialogs = testList
            notifyAdapters()
        }
    }

    private fun initializeTab() {
        /* binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
             override fun onTabSelected(tab: TabLayout.Tab?) {
                 handleRecyclerVisibility(pos = tab?.position ?: 0)
                 chatViewModel.tabSelectedPosition = tab?.position ?: 0
                 when (tab?.position) {
                     0 -> {
                         if (chatViewModel.isDataAvailable)
                             binding.llChatEmpty.visibility =
                                 if (BaseUtils.isInternetAvailable() && chatViewModel.getDialogsList().size == 0) View.VISIBLE else View.GONE
                         binding.tvNoInternet.visibility =
                             if (!BaseUtils.isInternetAvailable() && chatViewModel.getDialogsList().size == 0) View.VISIBLE else View.GONE
                     }
                     1 -> {
                         if (chatViewModel.isDataAvailable)
                             binding.llChatEmpty.visibility =
                                 if (BaseUtils.isInternetAvailable() && chatViewModel.getOnlineDialogsList().size == 0) View.VISIBLE else View.GONE
                         binding.tvNoInternet.visibility =
                             if (!BaseUtils.isInternetAvailable() && chatViewModel.getOnlineDialogsList().size == 0) View.VISIBLE else View.GONE
                     }
                     2 -> {
                         if (chatViewModel.isDataAvailable)
                             binding.llChatEmpty.visibility =
                                 if (BaseUtils.isInternetAvailable() && chatViewModel.getInPersonDialogsList().size == 0) View.VISIBLE else View.GONE
                         binding.tvNoInternet.visibility =
                             if (!BaseUtils.isInternetAvailable() && chatViewModel.getInPersonDialogsList().size == 0) View.VISIBLE else View.GONE
                     }
                 }
             }

             override fun onTabUnselected(tab: TabLayout.Tab?) {
             }

             override fun onTabReselected(tab: TabLayout.Tab?) {
             }
         })

         binding.tabLayout.selectTab(binding.tabLayout.getTabAt(chatViewModel.tabSelectedPosition))

 */
    }

    private fun handleRecyclerVisibility(pos: Int) {
        /*  binding.rvAllDialogs.visibility = if (pos == 0) View.VISIBLE else View.GONE
          binding.rvOnlineDialogs.visibility = if (pos == 1) View.VISIBLE else View.GONE
          binding.rvInPersonDialogs.visibility = if (pos == 2) View.VISIBLE else View.GONE*/
    }

    private fun initConnectionListener() {
        val rootView = binding.root
        chatConnectionListener = object : VerboseQbChatConnectionListener(rootView) {
            override fun reconnectionSuccessful() {
                super.reconnectionSuccessful()
                if (chatViewModel.isDataAvailable) {
                    getUserChatList()
                }
            }


        }
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(requireActivity())
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(
                    requireActivity(),
                    resultCode,
                    PLAY_SERVICES_REQUEST_CODE
                )!!.show()
            } else {
                Log.e("TAG", "This device is not supported.")
                shortToast("This device is not supported")
                requireActivity().finish()
            }
        }
    }

    private fun registerQbChatListeners() {

        ChatHelper.addConnectionListener(chatConnectionListener)
        try {
            systemMessagesManager = QBChatService.getInstance().systemMessagesManager
            incomingMessagesManager = QBChatService.getInstance().incomingMessagesManager
            if (incomingMessagesManager == null) {
                reLogin()
                return
            }
            systemMessagesManager.addSystemMessageListener(systemMessagesListener)
            incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener)
            dialogsManager.addManagingDialogsCallbackListener(this)

            pushBroadcastReceiver = PushBroadcastReceiver()
            LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                pushBroadcastReceiver,
                IntentFilter(ACTION_NEW_FCM_EVENT)
            )
        } catch (e: Exception) {
            Log.d("TAG", "Can not get SystemMessagesManager. Need relogin. " + e.message)
            reLogin()
            return
        }


    }

    override fun onStop() {
        super.onStop()
        cancelTasks()

        unregisterQbChatListeners()
    }

    private fun cancelTasks() {
        joinerTasksSet.iterator().forEach {
            it.cancel(true)
        }
    }

    private fun unregisterQbChatListeners() {
        try {
            incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener)
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener)
            dialogsManager.removeManagingDialogsCallbackListener(this)
        } catch (e: Exception) {

        }
    }


    private fun initializeAdapters() {
        allDialogsAdapter =
            DialogsActivityAdapter(requireContext(), quick_blox_id, recyclerClickHandler)
        onlineDialogsAdapter =
            DialogsActivityAdapter(requireContext(), quick_blox_id, recyclerClickHandler)
        inPersonDialogsAdapter =
            DialogsActivityAdapter(requireContext(), quick_blox_id, recyclerClickHandler)

//        binding.rvAllDialogs.adapter = allDialogsAdapter
//        binding.rvOnlineDialogs.adapter = onlineDialogsAdapter
//        binding.rvInPersonDialogs.adapter = inPersonDialogsAdapter
        chatViewModel.filteredChatDialogs = chatViewModel.userChatDialogs
        notifyAdapters()
    }

    private val recyclerClickHandler = object : RecyclerViewClickHandler<View, Int, QBChatDialog> {
        override fun onClick(k: View, l: Int, m: QBChatDialog) {
            chatViewModel.userSelectedPosition = l
            chatViewModel.qbObject = m
            when {
                ChatHelper.isLogged() -> {
                    /*if (chatViewModel.areWeSearching)
                        binding.searchView.setQuery("", false)*/
                    hideKeyboard(binding.root)
                    val data = Bundle()
                    data.putSerializable(EXTRA_DIALOG_ID, m)
                    try {
                        findNavController().navigate(R.id.action_connection_to_chat_frag, data)
                    } catch (e: IllegalArgumentException) {

                    }
                }
                else -> {
                    if (BaseUtils.isInternetAvailable()) {
//                        binding.progressBarHeader.visibility = View.VISIBLE
                        ChatHelper.loginToChat(currentUser,
                            object : QBEntityCallback<Void> {
                                override fun onSuccess(p0: Void?, p1: Bundle?) {
                                    val data = Bundle()
                                    data.putSerializable(EXTRA_DIALOG_ID, m)
//                                    binding.progressBarHeader.visibility = View.GONE
                                    findNavController().navigate(
                                        R.id.action_connection_to_chat_frag,
                                        data
                                    )
                                }

                                override fun onError(e: QBResponseException?) {
//                                    binding.progressBarHeader.visibility = View.GONE
                                    shortToast(R.string.login_chat_login_error)
                                }
                            })
                    } else shortToast(resources.getString(R.string.no_internet_msg))
                }
            }
        }

        override fun onLongClick(k: View, l: Int, m: QBChatDialog) {
            chatViewModel.userSelectedPosition = l
            chatViewModel.qbObject = m
            openBottomSheet(l, m)
        }
    }

    private fun setObserver() {
        /* if (view == null) return

         chatViewModel.allCount.observe(viewLifecycleOwner) {
             val count = if (it == 0)
                 ""
             else "(${it})"
             binding.tabLayout.getTabAt(0)?.text = "${resources.getString(R.string.all)} $count"
         }

         chatViewModel.onlineCount.observe(viewLifecycleOwner, {
             val count = if (it == 0)
                 ""
             else "(${it})"
             binding.tabLayout.getTabAt(1)?.text = "${resources.getString(R.string.online)} $count"
         })

         chatViewModel.inPersonCount.observe(viewLifecycleOwner, {
             val count = if (it == 0)
                 ""
             else "(${it})"
             binding.tabLayout.getTabAt(2)?.text = "${resources.getString(R.string.inperson)} $count"

         })*/
    }


    private fun setBadge(isDataAvailable: Boolean) {
        /* val allDialogList = chatViewModel.getDialogsList()
         val onlineDialogList = chatViewModel.getOnlineDialogsList()
         val inPersonDialogList = chatViewModel.getInPersonDialogsList()*/


        /* if (allDialogList.size == 0)
             binding.tabLayout.getTabAt(0)?.text = resources.getString(R.string.all)
         else
             binding.tabLayout.getTabAt(0)?.text =
                 "${resources.getString(R.string.all)} (${allDialogList.size})"


         if (onlineDialogList.size == 0)
             binding.tabLayout.getTabAt(1)?.text = resources.getString(R.string.online)
         else
             binding.tabLayout.getTabAt(1)?.text =
                 "${resources.getString(R.string.online)} (${onlineDialogList.size})"


         if (inPersonDialogList.size == 0)
             binding.tabLayout.getTabAt(2)?.text = resources.getString(R.string.inperson)
         else
             binding.tabLayout.getTabAt(2)?.text =
                 "${resources.getString(R.string.inperson)} (${inPersonDialogList.size})"*/

    }

    private fun handleVisibility(value: Boolean) {
//        binding.progressBar.visibility = if (value) View.GONE else View.VISIBLE
    }


    private fun login() {
        binding.progressBar.visibility = View.VISIBLE
        chatViewModel.qbLogin().observe(viewLifecycleOwner) {
            handleVisibility(it)
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
                reLogin()
            } else {
                binding.llChatEmpty.visibility =
                    if (BaseUtils.isInternetAvailable()) View.VISIBLE else View.GONE
                binding.tvNoInternet.visibility =
                    if (!BaseUtils.isInternetAvailable()) View.VISIBLE else View.GONE
                binding.progressBar.visibility = View.GONE
                clearBadge()
                chatViewModel.yourMoveCount = 0
                setMessageCount(chatViewModel.yourMoveCount)
            }
        }
    }

    private fun clearBadge() {
//        binding.tabLayout.getTabAt(0)?.removeBadge()
//        binding.tabLayout.getTabAt(1)?.removeBadge()
//        binding.tabLayout.getTabAt(2)?.removeBadge()

    }

    private fun reLogin() {
        chatViewModel.reLoginToChat().observe(viewLifecycleOwner) {
            handleVisibility(it)
            if (it) {
                chatViewModel.isUserLoggedIn = true
//                binding.progressBar.visibility = View.GONE
                getUserChatList()
            } else {
                binding.progressBar.visibility = View.GONE
                clearBadge()
                login()
            }
        }
    }

    private fun getUserChatList() {
        if (view != null) {
            chatViewModel.updatePositions.clear()
            chatViewModel.getChatUsersList()
                .observe(viewLifecycleOwner) {
                    when (it) {
                        200 -> {
                            chatViewModel.isDataAvailable = true
                            getUserMatches(MatchDataRequest(chatViewModel.idsList, 0))
                        }

                        201 -> {
                            getUserMatches(MatchDataRequest(chatViewModel.idsList, 0))
                        }

                        400 -> {
                            clearEverything()
                        }

                        422 -> {
                            login()
                        }
                    }
                }
        }
    }

    private fun clearEverything() {
        binding.progressBar.visibility = View.GONE
        chatViewModel.clearEverything()
        handleVisibility(false)
        setBadge(false)
    }

    private fun getUserMatches(request: MatchDataRequest) {
        lifecycleScope.launch {
            chatViewModel.getUserMatches(request)
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }
                        is DataResult.Success -> {
                            chatViewModel.usersList = it.data.data

                            BaseUtils.hideProgressbar()
                            when (chatViewModel.storeImages(requireContext())) {
                                0 -> {
                                    /*  runBlocking {
                                          preferencesHelper.removeChatUserImage()
                                      }*/
                                }

                                1, 2, 3 -> {
                                    runBlocking {
                                        preferencesHelper.saveChatUserImage(
//                                            chatViewModel.imageHasMap,
                                            chatViewModel.imageUrlHasMap
                                        )
                                    }
                                }
                            }

                            binding.progressBar.visibility = View.GONE

                            notifyAdapters()
                            setBadge(true)

                            dialog?.dismiss()
                            showProgressBar(false)

                            joinTask()

                            runBlocking {
                                Log.d("Count", "Chat count - $${chatViewModel.yourMoveCount}")
                                preferencesHelper.saveYourMoveCount(chatViewModel.yourMoveCount)
                            }
                            setMessageCount(chatViewModel.yourMoveCount)

                        }
                        is DataResult.Failure -> {
                            binding.progressBar.visibility = View.GONE
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
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }

        }
    }

    private fun setMessageCount(count: Int) {
        (activity as HomeActivity).setMessageCount(count)
    }

    private fun joinTask() {
        val joinerTask =
            DialogJoinerAsyncTask(
                this,
                chatViewModel.userChatDialogs
            )
        joinerTasksSet.add(joinerTask)
        joinerTask.execute()
    }


    private fun notifyAdapters() {
        val listAll = chatViewModel.getDialogsList()
        val listOnline = chatViewModel.getOnlineDialogsList()
        val listInPerson = chatViewModel.getInPersonDialogsList()

        if (chatViewModel.isDataAvailable)
            binding.llChatEmpty.visibility =
                if ((listAll.size == 0 && chatViewModel.tabSelectedPosition == 0) || (listOnline.size == 0 && chatViewModel.tabSelectedPosition == 1) || (listInPerson.size == 0 && chatViewModel.tabSelectedPosition == 2)) if (BaseUtils.isInternetAvailable()) View.VISIBLE else View.GONE else View.GONE

        binding.tvNoInternet.visibility =
            if ((listAll.size == 0 && chatViewModel.tabSelectedPosition == 0) || (listOnline.size == 0 && chatViewModel.tabSelectedPosition == 1) || (listInPerson.size == 0 && chatViewModel.tabSelectedPosition == 2)) if (!BaseUtils.isInternetAvailable()) View.VISIBLE else View.GONE else View.GONE
        allDialogsAdapter.dialogs = listAll
        onlineDialogsAdapter.dialogs = listOnline
        inPersonDialogsAdapter.dialogs = listInPerson

        setMessageCount(chatViewModel.yourMoveCount)
    }

    override fun onRefresh() {
        binding.swipeRefreshLayout.isRefreshing = false
        if (chatViewModel.isUserLoggedIn) {
            getUserChatList()
        } else {
            login()
        }
    }

    override fun onDialogCreated(chatDialog: QBChatDialog) {
        getUserChatList()
    }

    override fun onDialogUpdated(chatDialog: String) {
        notifyAdapters()
    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog) {
        notifyAdapters()
    }

    fun openBottomSheet(position: Int, chatDialog: QBChatDialog) {
        dialog = BottomSheetDialog(requireActivity())

        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_unmatch, null)

        val tvUnmatch = view.findViewById<TextView>(R.id.tv_umatch)
        val tvReport = view.findViewById<TextView>(R.id.tv_report)
        val tvCancel = view.findViewById<TextView>(R.id.tv_cancel)

        tvReport.setOnClickListener {
            dialog?.dismiss()
            openReportDialog { data, subOption, child, reason ->
                val reportRequest = ReportRequest(
                    getUserId(),
                    data._id,
                    suboption_id = subOption._id,
                    sub_suboption_id = child._id,
                    other_info = reason
                )
                Log.d("Request", "$reportRequest")
                reportUser(reportRequest) {
                    unMatch(chatDialog)
                }
            }
        }

        tvUnmatch.setOnClickListener {
            BaseUtils.showProgressbar(requireContext())
            unMatch(chatDialog)
        }

        tvCancel.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.setCancelable(true)

        dialog?.setContentView(view)

        dialog?.show()

    }

    private fun unMatch(chatDialog: QBChatDialog) {
        for (id in chatViewModel.usersList) {
            val userListId =
                if (chatViewModel.qbObject.occupants[0] == chatViewModel.selfId.toInt())
                    chatViewModel.qbObject.occupants[1].toString()
                else chatViewModel.qbObject.occupants[0].toString()
            if (id.quickblox_user_id == userListId) {
                unMatch(UnMatchRequest(id._id), chatDialog, id.quickblox_user_id)
                break
            }
        }
    }

    private fun getUserId(): String {
        for (id in chatViewModel.usersList) {
            val userListId =
                if (chatViewModel.qbObject.occupants[0] == chatViewModel.selfId.toInt())
                    chatViewModel.qbObject.occupants[1].toString()
                else chatViewModel.qbObject.occupants[0].toString()
            if (id.quickblox_user_id == userListId) {
                return id._id
            }
        }

        return ""
    }

    private fun reportUser(request: ReportRequest, response: () -> Unit) {
        if (view == null) return
        lifecycleScope.launch {
            getProfileViewModel.reportUser(request = request).observe(viewLifecycleOwner, {
                when (it) {
                    DataResult.Empty -> {
                    }
                    is DataResult.Failure -> {
                    }
                    DataResult.Loading -> {
                    }
                    is DataResult.Success -> {
                        if (it.statusCode == 200) {
                            response.invoke()
                        }
                    }
                }
            })
        }
    }


    private fun unMatch(request: UnMatchRequest, chatDialog: QBChatDialog, id: String) {
        val list = ArrayList<QBChatDialog>()
        list.add(chatDialog)
        lifecycleScope.launch {
            chatViewModel.unMatch(request)
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {
                        }
                        is DataResult.Success -> {
                            dialog?.dismiss()
                            try {
                                if (chatViewModel.imageUrlHasMap.containsKey(id)) {
                                    chatViewModel.imageUrlHasMap.remove(id)
                                }
                                if (chatViewModel.imageUrlHasMap.isEmpty()) {
                                    runBlocking {
                                        preferencesHelper.removeChatUserImage()
                                    }
                                } else {
                                    runBlocking {
                                        preferencesHelper.saveChatUserImage(
//                                    chatViewModel.imageHasMap,
                                            chatViewModel.imageUrlHasMap
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                            }
                            blockUser()
                            getUserChatList()
                        }
                        is DataResult.Failure -> {
                            dialog?.dismiss()
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
        val id = if (chatViewModel.qbObject.occupants[0] == chatViewModel.selfId.toInt())
            chatViewModel.qbObject.occupants[1].toString()
        else chatViewModel.qbObject.occupants[0].toString()
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
    }

    private fun deletePrivateDialogs(privateDialogsToDelete: List<QBChatDialog>, id: String) {
        ChatHelper.deletePrivateDialogs(
            privateDialogsToDelete,
            object : QBEntityCallback<ArrayList<String>> {
                override fun onSuccess(dialogsIds: ArrayList<String>, bundle: Bundle?) {
                    dialog?.dismiss()
                    try {
                        if (chatViewModel.imageUrlHasMap.containsKey(id)) {
                            chatViewModel.imageUrlHasMap.remove(id)
                        }
                        if (chatViewModel.imageUrlHasMap.isEmpty()) {
                            runBlocking {
                                preferencesHelper.removeChatUserImage()
                            }
                        } else {
                            runBlocking {
                                preferencesHelper.saveChatUserImage(
//                                    chatViewModel.imageHasMap,
                                    chatViewModel.imageUrlHasMap
                                )
                            }
                        }
                    } catch (e: Exception) {
                    }
                    getUserChatList()
                }

                override fun onError(e: QBResponseException) {
                    dialog?.dismiss()
                    BaseUtils.hideProgressbar()
                    getUserChatList()

                }
            })
    }

    private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
            Log.v("TAG", "Received broadcast " + intent.action + " with data: " + message)
            getUserChatList()
        }
    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage)
        }

        override fun processError(e: QBChatException, qbChatMessage: QBChatMessage) {

        }
    }

    private inner class AllDialogsMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(
            s: String,
            qbChatMessage: QBChatMessage,
            integer: Int?
        ) {
            Log.d("TAG", "Processing received Message: " + qbChatMessage.body)
            if (integer != currentUser.id) {
                for (pos in chatViewModel.filteredChatDialogs.indices) {
                    if (chatViewModel.filteredChatDialogs[pos].dialogId == s) {
                        chatViewModel.filteredChatDialogs[pos].updatedAt =
                            Calendar.getInstance().time

                        chatViewModel.filteredChatDialogs[pos].lastMessage =
                            qbChatMessage.body

                        chatViewModel.filteredChatDialogs[pos].lastMessageUserId =
                            qbChatMessage.senderId
                        chatViewModel.filteredChatDialogs[pos].unreadMessageCount += 1

                    }
                }
                chatViewModel.yourMoveCount = 0
                for (values in chatViewModel.filteredChatDialogs) {
                    if (chatViewModel.quickBloxId.isEmpty() || chatViewModel.quickBloxId == if (values.lastMessageUserId == null) chatViewModel.quickBloxId else values.lastMessageUserId.toString()) {

                    } else {
                        if (values.lastMessageUserId != null) chatViewModel.yourMoveCount++
                    }
                }
                setMessageCount(chatViewModel.yourMoveCount)
                dialogsManager.onGlobalMessageReceived(s, qbChatMessage)
            }
        }
    }

    private class DialogJoinerAsyncTask internal constructor(
        chatUserFragment: ChatUserFragment,
        private val dialogs: ArrayList<QBChatDialog>
    ) : BaseAsyncTask<Void, Void, Void>() {
        private val activityRef: WeakReference<ChatUserFragment> = WeakReference(chatUserFragment)

        @Throws(Exception::class)
        override fun performInBackground(vararg params: Void): Void? {
            if (!isCancelled) {
                ChatHelper.join(dialogs)
            }
            return null
        }

        override fun onResult(result: Void?) {
            if (!isCancelled && !activityRef.get()?.hasMoreDialogs!!) {
                activityRef.get()?.disableProgress()
            } else {
            }
        }

        override fun onException(e: Exception) {
            super.onException(e)
            if (!isCancelled) {
                Log.d("Dialog Joiner Task", "Error: $e")
                shortToast("Error: " + e.message)
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            cancel(true)
        }
    }

    private fun disableProgress() {
        isProcessingResultInProgress = false
    }

    private fun handleView(type: ChatType) {
        this.chatType = type
        binding.type = type


        loadFragment()
    }

    private fun loadFragment() {
        fragment = when (chatType) {
            ChatType.ALL -> allConnectionFragment
            ChatType.REAL_LIFE -> realLifeConnectionsFragment
            ChatType.FAVOURITES -> favouritesConnectionFragment
        }
        fragment?.let {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.container, it)
            transaction.commit()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvAllConnections -> {
                if (chatType != ChatType.ALL) {
                    chatType = ChatType.ALL
                    handleView(chatType)
                }
            }

            R.id.tvRealLife -> {
                if (chatType != ChatType.REAL_LIFE) {
                    chatType = ChatType.REAL_LIFE
                    handleView(chatType)
                }
            }

            R.id.tvFavourites -> {
                if (chatType != ChatType.FAVOURITES) {
                    chatType = ChatType.FAVOURITES
                    handleView(chatType)
                }
            }
        }
    }

    enum class ChatType : ChatTypeInterface {
        ALL {
            override fun value(): String = "all"
        },
        REAL_LIFE {
            override fun value(): String = "online"
        },
        FAVOURITES {
            override fun value(): String = "inperson"
        }
    }

    interface ChatTypeInterface {
        fun value(): String
    }


* */