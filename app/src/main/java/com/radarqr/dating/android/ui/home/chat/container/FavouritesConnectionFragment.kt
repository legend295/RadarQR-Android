package com.radarqr.dating.android.ui.home.chat.container

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentFavouritesConnectionsBinding
import com.radarqr.dating.android.ui.home.chat.ChatUserFragment
import com.radarqr.dating.android.ui.home.chat.adapter.ConnectionsAdapter
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.quickBlox.ChatFragment.Companion.EXTRA_DIALOG_ID
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ConnectionBaseFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.MatchDataRequest
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavouritesConnectionFragment :
    ConnectionBaseFragment<FragmentFavouritesConnectionsBinding>() {

    var adapter: ConnectionsAdapter? = null

    //    private val quickBloxManager: QuickBloxManager by inject()
    val chatViewModel: ChatViewModel by viewModel()

    //    private var isLastPage = false
    private var isLoading = false
    var totalEntries = 0
    private var skip = 0
    private var limit = 10

    override fun getLayoutRes(): Int = R.layout.fragment_favourites_connections

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skip = 0/*chatViewModel.favouriteUserChatDialogs.size*/
        limit =
            if (chatViewModel.favouriteUserChatDialogs.isEmpty() || chatViewModel.favouriteUserChatDialogs.size < 10) 10 else chatViewModel.favouriteUserChatDialogs.size
        setAdapter()
        init()
        setListener()
//        binding.llChatEmpty.visible(chatViewModel.favouritesUserChatDialogsMap.isEmpty())
        binding.progressBar.visible(chatViewModel.favouritesUserChatDialogsMap.isEmpty())

        binding.swipeRefreshLayout.setOnRefreshListener {
            skip = 0
            chatViewModel.isFavLastPage = true
            init()
            binding.swipeRefreshLayout.isRefreshing = false
        }

    }

    private fun init() {
        chatViewModel.handler?.signInToQB {
            if (it && skip == 0)
                getChatDialogs()
            else {
                binding.progressBar.visible(isVisible = false)
                binding.llChatEmpty.visible(chatViewModel.favouriteUserChatDialogs.isEmpty())
            }
        }
    }

    private fun setListener() {
        chatViewModel.dataListener = object : ChatViewModel.DataListener {
            override fun incomingMessageListener(p0: String?, p1: QBChatMessage?, p2: Int?) {
                /*val id = p2?.toString() ?: ""
                if (chatViewModel.favouritesUserChatDialogsMap.containsKey(id)) {
                    chatViewModel.favouritesUserChatDialogsMap[id]?.apply {
                        lastMessage = p1?.body ?: ""
                        unreadMessageCount += 1
                        p1?.dateSent?.apply {
                            lastMessageDateSent = this
                        }
                        chatViewModel.favouriteUserChatDialogs.clear()
                        chatViewModel.favouriteUserChatDialogs.addAll(chatViewModel.favouritesUserChatDialogsMap.values)
                        adapter?.refresh()
                    }
                }*/
                chatViewModel.updateDialog(p1, p2) {
                    adapter?.refresh()
                }

                setMessageCount()
            }

            override fun onDialogCreated() {
                if (view != null && isVisible)
                    this@FavouritesConnectionFragment.init()
            }
        }
    }

    private fun getChatDialogs() {
        QuickBloxManager.getFavouritesChatDialogs(skip, limit, { data, bundle ->
//            totalEntries = bundle["total_entries"]?.toString()?.toInt() ?: 0
            totalEntries = bundle.getInt("total_entries", 0)
            chatViewModel.isFavLastPage =
                chatViewModel.favouritesUserChatDialogsMap.size >= totalEntries
            val tempDialogList = ArrayList(data)
//            val listOfDialogsToBeDeleted = arrayListOf<String>()
            val listOfDialogsToBeUpdated = arrayListOf<QBChatDialog>()
            runBlocking {
                chatViewModel.getMatchesDialogsOnly {
                    binding.llChatEmpty.visible(data.isEmpty())
                    binding.progressBar.visible(isVisible = false)
                    val list = ArrayList<String>()
                    data.forEach { dialog ->
                        dialog?.let {
                            if (chatViewModel.dialogsIdsOverServer.contains(dialog.dialogId))
                                QuickBloxManager.getOtherUserId(dialog)
                                    ?.let { it1 -> list.add(it1) }
                            else {
                                tempDialogList.remove(it)
//                                listOfDialogsToBeDeleted.add(dialog.dialogId)
                                listOfDialogsToBeUpdated.add(it)
                            }
                        }
                    }
                    /*listOfDialogsToBeDeleted.forEach {
                        chatViewModel.deleteDialog(it, this@FavouritesConnectionFragment)
                    }*/

                    listOfDialogsToBeUpdated.forEach {
                        chatViewModel.updateDialog(it, this@FavouritesConnectionFragment)
                    }
                    if (list.isNotEmpty())
                        getUserMatches(MatchDataRequest(list), tempDialogList)
                    else {
                        tempDialogList.mapList()
                    }
                }
            }
        }, {
            if (it.httpStatusCode == 422 && it.message == "base Forbidden. Need user.") {
                context?.showToast("Forbidden user, Retrying login")
                QuickBloxManager.deleteSession {
                    init()
                }
            }
        })
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavouriteConnections.layoutManager = layoutManager
//        chatViewModel.favouriteUserChatDialogs.sortByDescending {
//            it?.lastMessageDateSent
//        }
        adapter = ConnectionsAdapter(
            chatViewModel.favouriteUserChatDialogs,
            QuickBloxManager,
            { qbData, position ->
                chatViewModel.qbObject = qbData
                val data = Bundle()
                data.putSerializable(EXTRA_DIALOG_ID, qbData)
                data.putInt(Constants.POSITION, position)
                data.putString(Constants.FROM, ChatUserFragment.ChatType.FAVOURITES.value())
                this.view?.findNavController()?.navigate(R.id.chat_fragment, data)
            }, { qbData, itemId, position, text ->
                when (itemId) {
                    0 -> {
                        chatViewModel.handler?.markFavourite(
                            qbData,
                            position,
                            ChatUserFragment.ChatType.FAVOURITES.value(), text
                        ) {
                            chatViewModel.favouriteUserChatDialogs.removeAt(position)
                            binding.llChatEmpty.visible(chatViewModel.favouriteUserChatDialogs.isEmpty())
                            adapter?.notifyItemRemoved(position)
                        }
                    }

                    1 -> {
                        chatViewModel.handler?.unMatchChat(qbData) {
                            chatViewModel.favouriteUserChatDialogs.removeAt(position)
                            binding.llChatEmpty.visible(chatViewModel.favouriteUserChatDialogs.isEmpty())
                            adapter?.notifyItemRemoved(position)
                        }
                    }
                }
            })
        adapter?.setHasStableIds(true)
        binding.rvFavouriteConnections.setHasFixedSize(true)
        binding.rvFavouriteConnections.adapter = adapter

        binding.rvFavouriteConnections.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                adapter?.addLoadingView()
                skip = chatViewModel.favouriteUserChatDialogs.size
                getChatDialogs()
                this@FavouritesConnectionFragment.isLoading = true
            }

            override val isLastPage: Boolean
                get() = chatViewModel.isFavLastPage
            override val isLoading: Boolean
                get() = this@FavouritesConnectionFragment.isLoading
        })
    }

    private fun getUserMatches(request: MatchDataRequest, list: ArrayList<QBChatDialog?>) {
        if (view != null && isAdded && isVisible)
            lifecycleScope.launch {
                chatViewModel.getUserMatches(request)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                /*val data = it.data.data.replaceUserMatchesImageWithUrl(
                                    requireContext(),
                                    chatViewModel.favouriteUserMatchesList
                                )*/
                                val map = it.data.data.associateBy { it1 -> it1.quickblox_user_id }
                                chatViewModel.favouriteUserMatchesList = map
                                if (isLoading)
                                    adapter?.removeLoadingView()
                                isLoading = false
                                list.mapList()

                            }

                            is DataResult.Failure -> {
                                binding.llChatEmpty.visible(isVisible = true)
                                binding.progressBar.visible(isVisible = false)
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

    private fun ArrayList<QBChatDialog?>.mapList() {
        if (skip == 0) chatViewModel.favouriteUserChatDialogs.clear()
        chatViewModel.mapFavouritesDialogsList(this, this@FavouritesConnectionFragment) {
            binding.llChatEmpty.visible(chatViewModel.favouriteUserChatDialogs.isEmpty())
            binding.progressBar.visible(isVisible = false)
            chatViewModel.isFavLastPage =
                chatViewModel.favouriteUserChatDialogs.size >= totalEntries
            adapter?.refresh()
        }

    }

    fun updateTotalEntries() {
        totalEntries--
        chatViewModel.isFavLastPage =
            chatViewModel.favouriteUserChatDialogs.size >= totalEntries
        binding.llChatEmpty.visible(chatViewModel.favouriteUserChatDialogs.isEmpty())
    }

    private fun setMessageCount() {
        (activity as HomeActivity?)?.setUnReadMessageCount()
    }
}