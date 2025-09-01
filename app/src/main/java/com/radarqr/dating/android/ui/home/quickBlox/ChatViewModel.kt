package com.radarqr.dating.android.ui.home.quickBlox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogCustomData
import com.quickblox.core.request.QBRequestUpdateBuilder
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.MatchResponseData
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.home.chat.ChatUserFragment.Companion.USER_DATA
import com.radarqr.dating.android.ui.home.chat.container.AllConnectionsFragment
import com.radarqr.dating.android.ui.home.chat.container.FavouritesConnectionFragment
import com.radarqr.dating.android.ui.home.chat.container.RealLifeConnectionsFragment
import com.radarqr.dating.android.ui.home.quickBlox.model.MatchedData
import com.radarqr.dating.android.ui.welcome.mobileLogin.MatchDataRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.UnMatchRequest
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.QuickBloxManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class ChatViewModel constructor(
    private val dataRepository: DataRepository,
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {

    /**
     * variables used for all connection
     *
     * clear all the lists in clearEverything Function
     * */
    var allUserMatchesList: Map<String, MatchedData> = LinkedHashMap()
    var allUserChatDialogs: ArrayList<QBChatDialog?> = ArrayList()
    var allUserChatDialogsMap: LinkedHashMap<String, QBChatDialog?> = LinkedHashMap()
    var allUserChatDialogsMapForNotification: LinkedHashMap<String, QBChatDialog?> = LinkedHashMap()
    var isAllLastPage = false

    /**
     * variables used for Real life connection
     *
     * clear all the lists in clearEverything Function
     * */
    var realLifeUserMatchesList: Map<String, MatchedData> = LinkedHashMap()
    var realLifeUserChatDialogs: ArrayList<QBChatDialog?> = ArrayList()
    var realLifeUserChatDialogsMap: LinkedHashMap<String, QBChatDialog?> = LinkedHashMap()
    var isRealLastPage = false

    /**
     * variables used for Favourite connection
     *
     * clear all the lists in clearEverything Function
     * */
    var favouriteUserMatchesList: Map<String, MatchedData> = LinkedHashMap()
    var favouriteUserChatDialogs: ArrayList<QBChatDialog?> = ArrayList()
    var favouritesUserChatDialogsMap: LinkedHashMap<String, QBChatDialog?> = LinkedHashMap()
    var isFavLastPage = false

    /**
     * qbObject used at many places (Like to unMatch user or report user) but getting value on adapter click
     * */
    var qbObject = QBChatDialog()

    var matchesData: Map<String, MatchResponseData> = LinkedHashMap()

    var dialogsIdsOverServer = ArrayList<String>()

    var dataListener: DataListener? = null
    var handler: Handler? = null

    init {
        viewModelScope.launch {
            getMatchesDialogsOnly { }
        }
    }


    fun removeDialogFromMaps(qbChatDialog: QBChatDialog) {
        qbChatDialog.apply {
            val id = QuickBloxManager.getOtherUserId(this)
            allUserChatDialogsMap.remove(id)
            realLifeUserChatDialogsMap.remove(id)
            favouritesUserChatDialogsMap.remove(id)
        }
    }

    fun getUnMatchRequest(qbChatDialog: QBChatDialog): UnMatchRequest? {
        return qbChatDialog.let {
            val matchData = it.customData?.get("user_data")
            val id = matchData?.let { it1 -> (it1 as MatchedData)._id }
            id?.let { it1 ->
                UnMatchRequest(it1)
            } ?: kotlin.run { null }
        }
            ?: kotlin.run { null }
    }


    /**
     * mapping the qb dialogs received from QB and match match data received from api
     * mapping because if user update any name and image so we can update in our UI using this function
     * */
    fun mapAllDialogsList(
        list: ArrayList<QBChatDialog?>,
        fragment: AllConnectionsFragment,
        isSuccess: () -> Unit
    ) {
        val listOfDialogsToBeDeleted = arrayListOf<String>()
        allUserChatDialogsMap.clear()
        list.forEach {
            it?.apply {
                val id = QuickBloxManager.getOtherUserId(it)
                id ?: return@apply
                val data = allUserMatchesList[id]
                data?.let {
                    val isMatch =
                        customData != null && customData?.get(QuickBloxManager.CUSTOM_KEY_IS_MATCH) == null
                                || customData?.get(QuickBloxManager.CUSTOM_KEY_IS_MATCH) == true
                    if (isMatch) {
                        customData?.apply {
                            fields[USER_DATA] = data
                        } ?: kotlin.run {
                            val customData = QBDialogCustomData().apply {
                                put(USER_DATA, data)
                            }
                            this.customData = customData
                        }
                        allUserChatDialogsMap[id] = this
                        allUserChatDialogsMapForNotification[this.dialogId] = this
                        allUserChatDialogs.add(allUserChatDialogsMap[id])
                    } else {
                        listOfDialogsToBeDeleted.add(dialogId)
                    }
                } ?: run {
                    listOfDialogsToBeDeleted.add(dialogId)
                }
            }
        }
        isSuccess()
        listOfDialogsToBeDeleted.dialogsToBeDeleted(fragment)
    }

    /**
     * mapping the qb dialogs received from QB and match match data received from api
     * mapping because if user update any name and image so we can update in our UI using this function
     * */
    fun mapRealLifeDialogsList(
        list: ArrayList<QBChatDialog?>,
        fragment: RealLifeConnectionsFragment,
        isSuccess: () -> Unit
    ) {
        val listOfDialogsToBeDeleted = arrayListOf<String>()
        realLifeUserChatDialogsMap.clear()
        list.forEach {
            it?.apply {
                val id = QuickBloxManager.getOtherUserId(it)
                id ?: return@apply
                val data = realLifeUserMatchesList[id]
                data?.let {
                    val isMatch =
                        customData != null && customData?.get(QuickBloxManager.CUSTOM_KEY_IS_MATCH) == null
                                || customData?.get(QuickBloxManager.CUSTOM_KEY_IS_MATCH) == true
                    if (isMatch) {
                        customData?.apply {
                            fields[USER_DATA] = data
                        } ?: kotlin.run {
                            val customData = QBDialogCustomData().apply {
                                put(USER_DATA, data)
                            }
                            this.customData = customData
                        }
                        realLifeUserChatDialogsMap[id] = this
                        realLifeUserChatDialogs.add(realLifeUserChatDialogsMap[id])
                    } else listOfDialogsToBeDeleted.add(dialogId)
                } ?: run {
                    listOfDialogsToBeDeleted.add(dialogId)
                }
            }
        }
        isSuccess()
        listOfDialogsToBeDeleted.dialogsToBeDeleted(fragment)
    }

    /**
     * mapping the qb dialogs received from QB and match match data received from api
     * mapping because if user update any name and image so we can update in our UI using this function
     * */
    fun mapFavouritesDialogsList(
        list: ArrayList<QBChatDialog?>,
        fragment: FavouritesConnectionFragment,
        isSuccess: () -> Unit
    ) {
        val listOfDialogsToBeDeleted = arrayListOf<String>()
        favouritesUserChatDialogsMap.clear()
        list.forEach {
            it?.apply {
                val id = QuickBloxManager.getOtherUserId(it)
                id ?: return@apply
                val data = favouriteUserMatchesList[id]
                data?.let {
                    val isMatch =
                        customData != null && (customData?.get(QuickBloxManager.CUSTOM_KEY_IS_MATCH) == null || customData?.get(
                            QuickBloxManager.CUSTOM_KEY_IS_MATCH
                        ) == true)
                    if (isMatch) {
                        customData?.apply {
                            fields[USER_DATA] = data
                        } ?: kotlin.run {
                            val customData = QBDialogCustomData().apply {
                                put(USER_DATA, data)
                            }
                            this.customData = customData
                        }
                        favouritesUserChatDialogsMap[id] = this
                        favouriteUserChatDialogs.add(favouritesUserChatDialogsMap[id])
                    } else listOfDialogsToBeDeleted.add(dialogId)

                } ?: run {
                    listOfDialogsToBeDeleted.add(dialogId)
                }
            }
        }
        isSuccess()
        listOfDialogsToBeDeleted.dialogsToBeDeleted(fragment)
    }


    private fun <T> ArrayList<String>.dialogsToBeDeleted(fragment: T) {
        forEach {
            deleteDialog(it, fragment)
        }
    }

    fun updateDialog(p1: QBChatMessage?, p2: Int?, success: () -> Unit) {
        val id = p2?.toString() ?: ""
        if (allUserChatDialogsMap.containsKey(id)) {
            allUserChatDialogsMap[id]?.update(p1, p2)

            allUserChatDialogs.clear()
            allUserChatDialogs.addAll(allUserChatDialogsMap.values)
            allUserChatDialogs.sort()
        }

        if (realLifeUserChatDialogsMap.containsKey(id)) {
            realLifeUserChatDialogsMap[id]?.update(p1, p2)
            realLifeUserChatDialogs.clear()
            realLifeUserChatDialogs.addAll(realLifeUserChatDialogsMap.values)
            realLifeUserChatDialogs.sort()
        }

        if (favouritesUserChatDialogsMap.containsKey(id)) {
            favouritesUserChatDialogsMap[id]?.update(p1, p2)

            favouriteUserChatDialogs.clear()
            favouriteUserChatDialogs.addAll(favouritesUserChatDialogsMap.values)
            favouriteUserChatDialogs.sort()

        }
        success()
    }

    private fun ArrayList<QBChatDialog?>.sort() {
        sortByDescending {
            it?.updatedAt?.time ?: it?.createdAt?.time
        }
    }

    private fun QBChatDialog.update(p1: QBChatMessage?, p2: Int?): QBChatDialog {
        apply {
            lastMessage = p1?.body ?: ""
            lastMessageUserId = p2
            unreadMessageCount += 1
            p1?.dateSent?.apply {
                lastMessageDateSent = this
            }
            updatedAt = Date()
        }
        return this
    }

    fun <T> deleteDialog(dialogId: String, fragment: T) {
        try {
            QBRestChatService.deleteDialog(dialogId, false).perform()
            when (fragment) {
                is AllConnectionsFragment -> (fragment as AllConnectionsFragment).updateTotalEntries()
                is RealLifeConnectionsFragment -> (fragment as RealLifeConnectionsFragment).updateTotalEntries()
                is FavouritesConnectionFragment -> (fragment as FavouritesConnectionFragment).updateTotalEntries()
            }
        } catch (_: Exception) {

        }
    }

    fun <T> updateDialog(dialog: QBChatDialog, fragment: T) {
        try {
            dialog.customData?.apply {
                className = QuickBloxManager.CLASS_NAME
                this.fields[QuickBloxManager.CUSTOM_KEY_IS_MATCH] = false
            }
            QBRestChatService.updateChatDialog(dialog, QBRequestUpdateBuilder()).perform()
            when (fragment) {
                is AllConnectionsFragment -> (fragment as AllConnectionsFragment).updateTotalEntries()
                is RealLifeConnectionsFragment -> (fragment as RealLifeConnectionsFragment).updateTotalEntries()
                is FavouritesConnectionFragment -> (fragment as FavouritesConnectionFragment).updateTotalEntries()
            }
        } catch (_: Exception) {

        }
    }


    fun getUserMatches(request: MatchDataRequest) =
        dataRepository.getUserMatchesApi(request).asLiveData()

    fun unMatch(request: UnMatchRequest) =
        dataRepository.unMatchApi(request).asLiveData()

    suspend fun getMatches(
        pageNo: Int = 1,
        limit: Int = 100,
        platform: String = "all",
        isSuccess: (Boolean) -> Unit
    ) {

        dataRepository.getMatches(pageNo, limit, platform).collect {
            when (it) {
                DataResult.Empty -> {}
                is DataResult.Failure -> {
                    matchesData = LinkedHashMap()
                    isSuccess(false)
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    matchesData =
                        it.data.data.associateBy { data -> data.qb_dialog_id }
                    isSuccess(true)
                }
            }
        }
    }

    suspend fun getMatchesDialogsOnly(
        pageNo: Int = 1,
        limit: Int = 2000,
        platform: String = "all",
        isSuccess: (Boolean) -> Unit
    ) {
        dataRepository.getMatchesDialogsOnly(pageNo, limit, platform).collect {
            when (it) {
                DataResult.Empty -> {}
                is DataResult.Failure -> {
                    dialogsIdsOverServer.clear()
                    isSuccess(false)
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    dialogsIdsOverServer = it.data.data
                    isSuccess(true)
                }
            }
        }
    }

    fun clearEverything() {
        qbObject = QBChatDialog()
        allUserMatchesList = LinkedHashMap()
        allUserChatDialogs.clear()
        allUserChatDialogsMap.clear()
        allUserChatDialogsMapForNotification.clear()

        realLifeUserChatDialogs.clear()
        realLifeUserChatDialogsMap.clear()
        realLifeUserMatchesList = LinkedHashMap()

        favouriteUserChatDialogs.clear()
        favouriteUserMatchesList = LinkedHashMap()
        favouritesUserChatDialogsMap.clear()

        matchesData = LinkedHashMap()
        dialogsIdsOverServer.clear()
        isAllLastPage = false
        isRealLastPage = false
        isFavLastPage = false
    }

    interface ChatTypeInterface {
        fun value(): String
    }

    interface DataListener {
        fun init() {
            //default implementation
        }

        fun onDialogCreated()
        fun incomingMessageListener(p0: String?, p1: QBChatMessage?, p2: Int?)
    }

    interface Handler {
        fun signInToQB(isSuccess: (Boolean) -> Unit)
        fun markFavourite(
            qbData: QBChatDialog,
            clickedPosition: Int,
            from: String,
            text: String,
            callBack: () -> Unit
        )

        fun unMatchChat(qbChatDialog: QBChatDialog, callBack: () -> Unit)
    }
}
