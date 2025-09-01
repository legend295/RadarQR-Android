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
import com.radarqr.dating.android.databinding.FragmentAllConnectionsBinding
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

class AllConnectionsFragment : ConnectionBaseFragment<FragmentAllConnectionsBinding>() {

    var adapter: ConnectionsAdapter? = null

    //    private val quickBloxManager: QuickBloxManager by inject()
    val chatViewModel: ChatViewModel by viewModel()

    //    private var isLastPage = false
    private var isLoading = false
    var totalEntries = 0
    private var skip = 0
    private var limit = 10

    override fun getLayoutRes(): Int = R.layout.fragment_all_connections

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skip = 0/*chatViewModel.allUserChatDialogs.size*/
        limit =
            if (chatViewModel.allUserChatDialogs.isEmpty() || chatViewModel.allUserChatDialogs.size < 10) 10 else chatViewModel.allUserChatDialogs.size
        setAdapter()
        init()
        setListener()
//        binding.llChatEmpty.visible(chatViewModel.allUserMatchesList.isEmpty())
        binding.progressBar.visible(chatViewModel.allUserChatDialogsMap.isEmpty())

        binding.swipeRefreshLayout.setOnRefreshListener {
            skip = 0
            chatViewModel.isAllLastPage = true
            init()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }


    private fun init() {
        chatViewModel.handler?.signInToQB {
            if (it && skip == 0) {
                getChatDialogs()
            } else {
                binding.progressBar.visible(isVisible = false)
                binding.llChatEmpty.visible(chatViewModel.allUserMatchesList.isEmpty())
            }
        }
    }

    private fun setMessageCount(count: Int) {
        (activity as HomeActivity?)?.setUnReadMessageCount()
    }

    private fun setListener() {
        chatViewModel.dataListener = object : ChatViewModel.DataListener {
            override fun incomingMessageListener(p0: String?, p1: QBChatMessage?, p2: Int?) {
                chatViewModel.updateDialog(p1, p2) {
                    adapter?.refresh()
                }
                setMessageCount(0)
                /*val id = p2?.toString() ?: ""
                if (chatViewModel.allUserChatDialogsMap.containsKey(id)) {
                    chatViewModel.allUserChatDialogsMap[id]?.apply {
                        lastMessage = p1?.body ?: ""
                        lastMessageUserId = p2
                        unreadMessageCount += 1
                        p1?.dateSent?.apply {
                            lastMessageDateSent = this
                        }
                        chatViewModel.allUserChatDialogs.clear()
                        chatViewModel.allUserChatDialogs.addAll(chatViewModel.allUserChatDialogsMap.values)
                        chatViewModel.allUserChatDialogs.sortByDescending {
                            it?.lastMessageDateSent
                        }
                    }
                }*/

            }

            override fun onDialogCreated() {
                if (view != null && isVisible)
                    this@AllConnectionsFragment.init()
            }
        }
    }

    private fun getChatDialogs() {
        QuickBloxManager.getChatDialogs(skip, limit, { data, bundle ->
//            totalEntries = bundle["total_entries"]?.toString()?.toInt() ?: 0
            totalEntries = bundle.getInt("total_entries", 0)
            val tempDialogList = ArrayList(data)
//            val listOfDialogsToBeDeleted = arrayListOf<String>()
            val listOfDialogsToBeUpdated = arrayListOf<QBChatDialog>()
            val list = ArrayList<String>()
            runBlocking {
                chatViewModel.getMatchesDialogsOnly {
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
                        chatViewModel.deleteDialog(it, this@AllConnectionsFragment)
                    }*/

                    listOfDialogsToBeUpdated.forEach {
                        chatViewModel.updateDialog(it, this@AllConnectionsFragment)
                    }

                    binding.llChatEmpty.visible(data.isEmpty())
                    binding.progressBar.visible(isVisible = false)
                    chatViewModel.isAllLastPage =
                        chatViewModel.allUserChatDialogsMap.size >= totalEntries
                    if (list.isNotEmpty())
                        getUserMatches(MatchDataRequest(list), tempDialogList)
                    else tempDialogList.mapList()
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
        binding.rvAllConnections.layoutManager = layoutManager
//        chatViewModel.allUserChatDialogs.sortByDescending {
//            it?.lastMessageDateSent
//        }
        adapter = ConnectionsAdapter(
            chatViewModel.allUserChatDialogs,
            QuickBloxManager,
            { qbData, position ->
                chatViewModel.qbObject = qbData
                val data = Bundle()
                data.putSerializable(EXTRA_DIALOG_ID, qbData)
                data.putString(Constants.FROM, ChatUserFragment.ChatType.ALL.value())
                data.putInt(Constants.POSITION, position)
                this.view?.findNavController()?.navigate(R.id.chat_fragment, data)
            }, { qbData, itemId, position, text ->
                when (itemId) {
                    0 -> {
                        chatViewModel.handler?.markFavourite(
                            qbData,
                            position,
                            ChatUserFragment.ChatType.ALL.value(), text
                        ) {
                            adapter?.notifyItemChanged(position)
                        }
                    }

                    1 -> {
                        chatViewModel.handler?.unMatchChat(qbData) {
                            chatViewModel.allUserChatDialogs.removeAt(position)
                            binding.llChatEmpty.visible(chatViewModel.allUserChatDialogs.isEmpty())
                            adapter?.notifyItemRemoved(position)
                        }
                    }
                }

            })
        adapter?.setHasStableIds(true)
        binding.rvAllConnections.setHasFixedSize(true)
        binding.rvAllConnections.adapter = adapter

        binding.rvAllConnections.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                adapter?.addLoadingView()
                this@AllConnectionsFragment.isLoading = true
                skip = chatViewModel.allUserChatDialogs.size
                getChatDialogs()
            }

            override val isLastPage: Boolean
                get() = chatViewModel.isAllLastPage
            override val isLoading: Boolean
                get() = this@AllConnectionsFragment.isLoading
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
                                    chatViewModel.allUserMatchesList
                                )*/
                                val map = it.data.data.associateBy { it1 -> it1.quickblox_user_id }
                                chatViewModel.allUserMatchesList = map
                                if (this@AllConnectionsFragment.isLoading)
                                    adapter?.removeLoadingView()
                                this@AllConnectionsFragment.isLoading = false
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
        if (skip == 0) chatViewModel.allUserChatDialogs.clear()
        chatViewModel.mapAllDialogsList(this, this@AllConnectionsFragment) {
            binding.llChatEmpty.visible(chatViewModel.allUserChatDialogs.isEmpty())
            binding.progressBar.visible(isVisible = false)
            setMessageCount(totalEntries)
            chatViewModel.isAllLastPage =
                chatViewModel.allUserChatDialogs.size >= totalEntries
            adapter?.refresh()
        }
    }

    fun updateTotalEntries() {
        totalEntries--
        setMessageCount(totalEntries)
        chatViewModel.isAllLastPage =
            chatViewModel.allUserChatDialogs.size >= totalEntries
        binding.llChatEmpty.visible(chatViewModel.allUserChatDialogs.isEmpty())
    }
}