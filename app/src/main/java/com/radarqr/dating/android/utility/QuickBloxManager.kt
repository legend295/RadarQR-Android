package com.radarqr.dating.android.utility

import android.os.Bundle
import android.util.Log
import com.quickblox.auth.QBAuth
import com.quickblox.auth.session.QBSession
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSessionParameters
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.core.request.QBRequestUpdateBuilder
import com.quickblox.messages.QBPushNotifications
import com.quickblox.messages.model.QBEnvironment
import com.quickblox.messages.model.QBNotificationChannel
import com.quickblox.messages.model.QBSubscription
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.SubscribeService
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.radarqr.dating.android.app.*
import com.radarqr.dating.android.utility.environment.Environment
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.XMPPConnection

object QuickBloxManager {

    private val TAG = QuickBloxManager::class.java.simpleName
    var qbChatService: QBChatService = QBChatService.getInstance()
    var qbSessionManager: QBSessionManager = QBSessionManager.getInstance()
    var qbPushManager: QBPushManager = QBPushManager.getInstance()
    const val CUSTOM_KEY_FAVOURITE = "favourite"
    const val CUSTOM_KEY_CATEGORY = "category"
    const val CUSTOM_KEY_CLASS_NAME = "class_name"
    const val CUSTOM_KEY_IN_PERSON = "inperson"
    const val CUSTOM_KEY_VENUE = "venue"
    const val CLASS_NAME = "occupantDetails"
    const val CUSTOM_KEY_FAVOURITE_MARKED_BY = "favouriteMarkedBy"
    const val CUSTOM_KEY_IS_MATCH = "ismatch"
    const val CUSTOM_KEY_DIALOG_OWNER_ID = "dialogownerid"

    /*  companion object {

      }
  */
    init {
        initializeListeners()

        QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(buildChatConfigs())
        QBChatService.setDefaultPacketReplyTimeout(10000)
        qbChatService.setUseStreamManagement(true)
    }

    private fun buildChatConfigs(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()

        configurationBuilder.socketTimeout = SOCKET_TIMEOUT
        configurationBuilder.isUseTls = USE_TLS
        configurationBuilder.isKeepAlive = KEEP_ALIVE
        configurationBuilder.isAutojoinEnabled = AUTO_JOIN
        configurationBuilder.setAutoMarkDelivered(AUTO_MARK_DELIVERED)
        configurationBuilder.isReconnectionAllowed = RECONNECTION_ALLOWED
        configurationBuilder.setAllowListenNetwork(ALLOW_LISTEN_NETWORK)
        configurationBuilder.port = CHAT_PORT

        return configurationBuilder
    }

    fun signIn(userName: String, userData: (QBUser?, QBResponseException?) -> Unit) {
        if (userName.isEmpty()) userData(null, null)
        val user = QBUser().apply {
            login = userName
            password = userName
        }
        QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(p0: QBUser?, p1: Bundle?) {
                p0?.let { SharedPrefsHelper.saveQbUser(it) }
                userData(p0, null)
                handleQuickBlockNotification()
            }

            override fun onError(p0: QBResponseException?) {
                Log.e(TAG, "signIn error ${p0?.localizedMessage}")
                userData(null, p0)
            }
        })
    }

    fun getUnReadMessageCount(
        ids: ArrayList<String>,
        response: (Int?, QBResponseException?) -> Unit
    ) {
        QBRestChatService.getTotalUnreadMessagesCount(ids.toSet(), Bundle())
            .performAsync(object : QBEntityCallback<Int> {
                override fun onSuccess(p0: Int?, p1: Bundle?) {
                    Log.e(TAG, "getTotalUnreadMessagesCount success $p0")
                    response(p0, null)
                }

                override fun onError(p0: QBResponseException?) {
                    response(null, p0)
                    Log.e(TAG, "getTotalUnreadMessagesCount error ${p0?.localizedMessage}")
                }
            })
    }

    fun connectToChat(isSuccess: (Boolean) -> Unit) {
        if (!qbChatService.isLoggedIn) {
            val user = QBUser().apply {
                id = qbSessionManager.sessionParameters?.userId ?: 0
                password = qbSessionManager.activeSession?.token ?: ""
            }
            qbChatService.login(user, object : QBEntityCallback<Void> {
                override fun onSuccess(p0: Void?, p1: Bundle?) {
                    isSuccess(true)
                    Log.d(TAG, "connectToChat Connected")
                    handleQuickBlockNotification()
                }

                override fun onError(p0: QBResponseException?) {
                    isSuccess(false)
                    Log.e(TAG, "connectToChat error ${p0?.localizedMessage}")
                }
            })

        } else isSuccess(true)
    }

    fun getChatDialogs(
        skip: Int = 0,
        limit: Int = 10,
        result: (ArrayList<QBChatDialog?>, Bundle) -> Unit,
        error: (exception: QBResponseException) -> Unit
    ) {
        val builder = QBRequestGetBuilder().apply {
            this.limit = limit
            this.skip = skip
            sortDesc("updated_at")
            addRule("data", "[$CUSTOM_KEY_CLASS_NAME]", CLASS_NAME)
            addRule("data", "[$CUSTOM_KEY_IS_MATCH]", true)
        }

        QBRestChatService.getChatDialogs(QBDialogType.PRIVATE, builder)
            .performAsync(object : QBEntityCallback<java.util.ArrayList<QBChatDialog?>?> {
                override fun onSuccess(result: ArrayList<QBChatDialog?>?, bundle: Bundle) {
                    Log.e(TAG, "getChatDialogs onSuccess dialogs count ${result?.size}")
                    result(result ?: ArrayList(), bundle)
                }

                override fun onError(exception: QBResponseException) {
                    Log.e(TAG, "getChatDialogs error ${exception.localizedMessage}")
                    error(exception)
                }
            })
    }

    fun getAllChatDialogs(result: (ArrayList<QBChatDialog?>, Bundle) -> Unit) {
        val builder = QBRequestGetBuilder().apply {
        }

        QBRestChatService.getChatDialogs(QBDialogType.PRIVATE, builder)
            .performAsync(object : QBEntityCallback<java.util.ArrayList<QBChatDialog?>?> {
                override fun onSuccess(result: ArrayList<QBChatDialog?>?, bundle: Bundle) {
                    Log.e(TAG, "getChatDialogs onSuccess dialogs count ${result?.size}")
                    result(result ?: ArrayList(), bundle)
                }

                override fun onError(exception: QBResponseException) {
                    result(arrayListOf(), Bundle())
                    Log.e(TAG, "getChatDialogs error ${exception.localizedMessage}")
                }
            })
    }

    fun getInPersonChatDialogs(
        skip: Int = 0,
        limit: Int = 10,
        result: (ArrayList<QBChatDialog?>, Bundle) -> Unit,
        error: (exception: QBResponseException) -> Unit
    ) {
        val builder = QBRequestGetBuilder().apply {
            this.limit = limit
            this.skip = skip
            sortDesc("updated_at")
            addRule("data", "[$CUSTOM_KEY_CLASS_NAME]", CLASS_NAME)
            addRule(
                "data",
                "[$CUSTOM_KEY_CATEGORY][in]",
                "${CUSTOM_KEY_IN_PERSON},$CUSTOM_KEY_VENUE"
            )
            addRule("data", "[$CUSTOM_KEY_IS_MATCH]", true)
        }

        QBRestChatService.getChatDialogs(QBDialogType.PRIVATE, builder)
            .performAsync(object : QBEntityCallback<java.util.ArrayList<QBChatDialog?>?> {
                override fun onSuccess(result: ArrayList<QBChatDialog?>?, bundle: Bundle) {
                    Log.e(TAG, "getChatDialogs onSuccess dialogs count ${result?.size}")
                    result(result ?: ArrayList(), bundle)
                }

                override fun onError(exception: QBResponseException) {
                    error(exception)
                    Log.e(TAG, "getChatDialogs error ${exception.localizedMessage}")
                }
            })
    }

    fun getFavouritesChatDialogs(
        skip: Int = 0,
        limit: Int = 10,
        result: (ArrayList<QBChatDialog?>, Bundle) -> Unit,
        error: (exception: QBResponseException) -> Unit
    ) {
        val builder = QBRequestGetBuilder().apply {
            this.limit = limit
            this.skip = skip
            sortDesc("updated_at")
            addRule("data", "[$CUSTOM_KEY_CLASS_NAME]", CLASS_NAME)
//            addRule("data", "[$CUSTOM_KEY_FAVOURITE]", true)
            addRule("data", "[$CUSTOM_KEY_IS_MATCH]", true)
            addRule(
                "data",
                "[$CUSTOM_KEY_FAVOURITE_MARKED_BY][in]",
                qbSessionManager.sessionParameters.userId
            )
        }

        QBRestChatService.getChatDialogs(QBDialogType.PRIVATE, builder)
            .performAsync(object : QBEntityCallback<java.util.ArrayList<QBChatDialog?>?> {
                override fun onSuccess(result: ArrayList<QBChatDialog?>?, bundle: Bundle) {
                    Log.e(TAG, "getChatDialogs onSuccess dialogs count ${result?.size}")
                    result(result ?: ArrayList(), bundle)
                }

                override fun onError(exception: QBResponseException) {
                    error(exception)
                    Log.e(TAG, "getChatDialogs error ${exception.localizedMessage}")
                }
            })
    }

    fun getDialogById(dialogId: String, callBack: (QBChatDialog?) -> Unit) {
        QBRestChatService.getChatDialogById(dialogId)
            .performAsync(object : QBEntityCallback<QBChatDialog> {
                override fun onSuccess(p0: QBChatDialog?, p1: Bundle?) {
                    callBack(p0)
                }

                override fun onError(p0: QBResponseException?) {
                    callBack(null)
                }
            })
    }

    fun markFavUnFav(qbChatDialog: QBChatDialog, isSuccess: (Boolean, QBChatDialog?) -> Unit) {
        val map = HashMap<String, Any>()
        // Fetch favourite data from dialog
        val favouriteMarkedBy: ArrayList<String>? = try {
            qbChatDialog.customData[CUSTOM_KEY_FAVOURITE_MARKED_BY] as ArrayList<String>?
        } catch (e: ClassCastException) {
            ArrayList()
        }

        // check if array contains self id then remove to mark un-favourite else add self id to mark favourite
        favouriteMarkedBy?.let {
            if (it.contains(qbSessionManager.sessionParameters.userId.toString())) {
                it.remove(qbSessionManager.sessionParameters.userId.toString())
//                if (it.isEmpty())
//                    it.add("1")
            } else {
                it.add(qbSessionManager.sessionParameters.userId.toString())
            }
            map[CUSTOM_KEY_FAVOURITE_MARKED_BY] = it
        } ?: kotlin.run {
            // if array empty then add self id to mark favourite for first time
            map[CUSTOM_KEY_FAVOURITE_MARKED_BY] = ArrayList<String>().apply {
                add(qbSessionManager.sessionParameters.userId.toString())
            }
        }

        // set value to the existing dialog so that other custom data doesn't effect
        qbChatDialog.customData?.apply {
            className = CLASS_NAME
            this.fields[CUSTOM_KEY_FAVOURITE_MARKED_BY] = map[CUSTOM_KEY_FAVOURITE_MARKED_BY]
        }

        QBChatDialog().apply {
            dialogId = qbChatDialog.dialogId  // to make updates - the dialog must contain dialogId
            customData = qbChatDialog.customData
            updateDialog(qbChatDialog, isSuccess)
        }
    }

    fun unMatch(qbChatDialog: QBChatDialog, isSuccess: (Boolean, QBChatDialog?) -> Unit) {
        // Update ismatch key in custom data to false to unMatch
        qbChatDialog.customData?.apply {
            className = CLASS_NAME
            this.fields[CUSTOM_KEY_IS_MATCH] = false
        }

        // update dialog
        QBChatDialog().apply {
            dialogId = qbChatDialog.dialogId  // to make updates - the dialog must contain dialogId
            customData = qbChatDialog.customData
            updateDialog(qbChatDialog, isSuccess)
        }
    }

    private fun QBChatDialog.updateDialog(
        qbChatDialog: QBChatDialog,
        isSuccess: (Boolean, QBChatDialog?) -> Unit
    ) {
        QBRestChatService.updateChatDialog(this, QBRequestUpdateBuilder())
            .performAsync(object : QBEntityCallback<QBChatDialog> {
                override fun onSuccess(updatedDialog: QBChatDialog?, bundle: Bundle?) {
                    Log.d(
                        QuickBloxManager::class.simpleName,
                        "Marked Favourite Success ${qbChatDialog.customData[CUSTOM_KEY_FAVOURITE_MARKED_BY]}"
                    )
                    Log.d(
                        QuickBloxManager::class.simpleName,
                        "Marked Favourite Success update dialog ${
                            updatedDialog?.customData?.get(CUSTOM_KEY_FAVOURITE_MARKED_BY)
                        }"
                    )
                    qbChatDialog.customData?.apply {
                        this.fields[CUSTOM_KEY_FAVOURITE_MARKED_BY] =
                            updatedDialog?.customData?.get(CUSTOM_KEY_FAVOURITE_MARKED_BY)
                    }
                    isSuccess(true, qbChatDialog)
                }

                override fun onError(exception: QBResponseException?) {
                    Log.d(
                        QuickBloxManager::class.simpleName,
                        "Marked Favourite Failed ${exception?.localizedMessage}"
                    )
                    isSuccess(false, null)
                }
            })
    }

    fun signOut(isSuccess: (Boolean) -> Unit) {
        qbSessionManager.deleteActiveSession()
        qbSessionManager.deleteSessionParameters()
        QBUsers.signOut().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(p0: Void?, p1: Bundle?) {
                qbChatService.destroy()
                isSuccess(true)
            }

            override fun onError(p0: QBResponseException?) {
                qbChatService.destroy()
                isSuccess(false)
                Log.e(TAG, "signOut onError ${p0?.localizedMessage}")
            }
        })
    }

    fun logout(isSuccess: () -> Unit) {
        qbChatService.logout(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                qbChatService.destroy()
                isSuccess()
            }

            override fun onError(exception: QBResponseException?) {
                qbChatService.destroy()
                isSuccess()
                Log.e(TAG, "logOut onError ${exception?.localizedMessage}")
            }
        })
    }

    fun deleteSession(isSuccess: (Boolean) -> Unit) {
        QBAuth.deleteSession().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                signOut(isSuccess)
            }

            override fun onError(exception: QBResponseException?) {
                signOut(isSuccess)
                Log.e(TAG, "deleteSession onError ${exception?.localizedMessage}")
            }
        })
    }

    fun deleteDialog(dialog: QBChatDialog, isSuccess: (Boolean) -> Unit) {
        QBRestChatService.deleteDialog(dialog.dialogId, false)
            .performAsync(object : QBEntityCallback<Void> {
                override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                    isSuccess(true)
                }

                override fun onError(exception: QBResponseException?) {
                    isSuccess(false)
                    Log.e(TAG, "deleteSession onError ${exception?.localizedMessage}")
                }
            })
    }

    fun deleteAllDialog(dialogIds: StringifyArrayList<String>, isSuccess: (Boolean) -> Unit) {
        QBRestChatService.deleteDialogs(dialogIds, false, Bundle())
            .performAsync(object : QBEntityCallback<ArrayList<String>> {
                override fun onSuccess(aVoid: ArrayList<String>, bundle: Bundle?) {
                    deleteSession(isSuccess)
                }

                override fun onError(exception: QBResponseException?) {
                    deleteSession(isSuccess)
                    Log.e(TAG, "deleteSession onError ${exception?.localizedMessage}")
                }
            })
    }


    fun getOtherUserId(dialog: QBChatDialog): String? =
        if ((qbSessionManager.sessionParameters?.userId ?: 0) == 0) null else
            if (dialog.occupants[0] == (qbSessionManager.sessionParameters?.userId ?: 0))
                dialog.occupants[1].toString()
            else dialog.occupants[0].toString()

    fun handleQuickBlockNotification() {
        // Enabling push so that after log in user get notifications
        QBSettings.getInstance().isEnablePushNotification = true
        SubscribeService.subscribeToPushes(RaddarApp.getInstance().applicationContext, true)

        QBPushNotifications.getSubscriptions()
            .performAsync(object : QBEntityCallback<ArrayList<QBSubscription>> {
                override fun onSuccess(p0: ArrayList<QBSubscription>?, p1: Bundle?) {
                    p0?.let {
                        if (it.size >= 9) {
                            for (pos in it.indices) {
                                Log.d(TAG, "ACTIVE_SESSION id: ${p0[pos].id}")
                                if (pos < 2) QBPushNotifications.deleteSubscription(it[pos].id)
                                    .perform()
                            }
                        }
                    }
                }

                override fun onError(p0: QBResponseException?) {
                    Log.e(TAG, "ACTIVE_SESSION error ${p0?.localizedMessage}")
                }
            })
        val qbSubscription = QBSubscription().apply {
            this.notificationChannel = QBNotificationChannel.GCM
            environment = when (RaddarApp.getEnvironment()) {
                Environment.PRODUCTION, Environment.RELEASE, Environment.STAGING -> QBEnvironment.PRODUCTION
                else -> QBEnvironment.DEVELOPMENT
            }
        }
        QBPushNotifications.createSubscription(qbSubscription)
            .performAsync(object : QBEntityCallback<ArrayList<QBSubscription>> {
                override fun onSuccess(p0: ArrayList<QBSubscription>?, p1: Bundle?) {
                    Log.d(TAG, "createSubscription Success : ${p0?.size}")
                }

                override fun onError(p0: QBResponseException?) {
                    Log.d(TAG, "createSubscription Error : ${p0?.localizedMessage}")
                }
            })


    }


    private fun initializeListeners() {


        qbSessionManager.addListener(object : QBSessionManager.QBSessionListener {
            override fun onSessionCreated(p0: QBSession?) {

            }

            override fun onSessionUpdated(p0: QBSessionParameters?) {

            }

            override fun onSessionDeleted() {

            }

            override fun onSessionRestored(p0: QBSession?) {

            }

            override fun onSessionExpired() {
//                signIn(HomeActivity.userMobileNumber) {}
            }

            override fun onProviderSessionExpired(p0: String?) {

            }
        })
        qbChatService.addConnectionListener(object : ConnectionListener {
            override fun connected(p0: XMPPConnection?) {
                Log.d(TAG, "connected")
                /*   if (qbSessionManager.activeSession != null)
                       connectToChat { }*/
            }

            override fun authenticated(p0: XMPPConnection?, p1: Boolean) {
                Log.d(TAG, "authenticated")
            }

            override fun connectionClosed() {
                Log.d(TAG, "connectionClosed")
            }

            override fun connectionClosedOnError(p0: java.lang.Exception?) {
                Log.e(TAG, "connectionClosedOnError ${p0?.localizedMessage}")
            }

            override fun reconnectionSuccessful() {
                Log.d(TAG, "reconnectionSuccessful")
            }

            override fun reconnectingIn(p0: Int) {
                Log.d(TAG, "reconnectingIn")
            }

            override fun reconnectionFailed(p0: java.lang.Exception?) {
                Log.e(TAG, "reconnectionFailed ${p0?.localizedMessage}")
            }
        })
    }


}