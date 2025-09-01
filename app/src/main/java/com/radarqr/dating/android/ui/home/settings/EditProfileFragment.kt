package com.radarqr.dating.android.ui.home.settings

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentEditProfileBinding
import com.radarqr.dating.android.hotspots.helpers.addThreeImageDialog
import com.radarqr.dating.android.hotspots.helpers.addThreeImageDialogConfirmation
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.EditProfile.openBottomSheetToUpdateProfile
import com.radarqr.dating.android.ui.home.settings.EditProfile.updateHobbiesAndInterestBottomSheet
import com.radarqr.dating.android.ui.home.settings.adapter.EditProfileGeneralContentAdapter
import com.radarqr.dating.android.ui.home.settings.adapter.HobbyAdapter
import com.radarqr.dating.android.ui.home.settings.model.EditProfileGeneralContentData
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteImagesRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.ui.welcome.registerScreens.ImageUploadViewModel
import com.radarqr.dating.android.ui.welcome.registerScreens.adapter.ImageStringAdapter
import com.radarqr.dating.android.ui.welcome.registerScreens.back_press
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.EditProfileGeneralContentTypes
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility.getEditProfileGeneralContentList
import com.radarqr.dating.android.utility.Utility.getEditProfileWorkEducationContentList
import com.radarqr.dating.android.utility.Utility.getImageUrl
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>(), View.OnClickListener,
    ViewClickHandler, SwipeRefreshLayout.OnRefreshListener {

    private var itemClicked = false
    private val preferencesHelper: PreferencesHelper by inject()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    lateinit var hobbyAdapter: HobbyAdapter
    lateinit var imageAdapter: ImageStringAdapter

    private var generalAdapter: EditProfileGeneralContentAdapter? = null
    private var workEducationAdapter: EditProfileGeneralContentAdapter? = null
    private val generalList = ArrayList<EditProfileGeneralContentData>()
    private val workEducationList = ArrayList<EditProfileGeneralContentData>()
    private var isLocationClicked = false

    var hobbiesList: ArrayList<HobbiesData> = ArrayList()
    val list = ArrayList<String>()
    val pathList = ArrayList<String>()
    var remove_imageList: ArrayList<String> = ArrayList()
    var image_listEdit: ArrayList<String> = ArrayList()
    var tag = 0
    var lat = 0.0
    var longt = 0.0
    private var mRootView: ViewGroup? = null
    private var mIsFirstLoad = false
    var education_level = ""
    var profileData: ProfileData? = null

    // set this value to true if images are less then 3 else set to false
    private var doWeNeedToShowImageDialog = false

    enum class UpdateType {
        IMAGE, HOBBIES, ABOUT_ME, OTHER
    }

    override fun getLayoutRes(): Int = R.layout.fragment_edit_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        binding.viewModel = getProfileViewModel
        binding.viewHandler = this
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        (activity as HomeActivity?)?.setFragmentContainer()

        setGeneralAdapter()
        setWorkEducationAdapter()

        initializeAdapters()
        initializeObserver()
        when (getProfileViewModel.profileData.value) {
            null -> {
                try {
                    getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
                        data?.let {
                            /*it.replaceProfileImagesWithUrl(requireContext()) { data ->
                                getProfileViewModel.profileData.value = data
                            }*/
                            getProfileViewModel.profileData.value = it
                        }
                    }
                } catch (e: java.lang.Exception) {

                }
            }

            else -> {
                if (!BaseUtils.isInternetAvailable()) {
                    CommonCode.setToast(
                        requireContext(),
                        resources.getString(R.string.no_internet_msg)
                    )
                }
            }
        }


        if (mRootView == null) {
            binding.editProfileFragment = this
            mIsFirstLoad = true
        } else {
            mIsFirstLoad = false
        }

        Log.e("STATE", "EditProfileFragment")

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
//        setImages()
//        setdata()
//

        clickListener()

        binding.clAddMore.setOnClickListener {
            if (imageAdapter.listItem.size % 3 != 0) return@setOnClickListener
            if (imageAdapter.listItem.size < 15) {
                if (imageAdapter.listItem.size == imageAdapter.size) {
                    imageAdapter.size += 3
                    imageAdapter.refresh()
                }
                binding.clAddMore.visible(imageAdapter.itemCount != 15)
                binding.tvAdd.text = "Upload(${15 - (imageAdapter.size)} Remaining)"
            }
        }

    }

    override fun onResume() {
        super.onResume()
        /*activity?.let {
            if (it is HomeActivity) {
                it.setHomeToolbarVisibility(isVisible = true)
                it.hideShowUserIcon(isVisible = true)
                it.binding.bottomNav.visible(isVisible = false)
            }
        }*/
        if (requireContext().isGpsEnabled() && isLocationClicked) {
            openLocationFragment()
        }
    }

    private fun initializeAdapters() {
        hobbyAdapter = HobbyAdapter(
            hobbiesList,
            requireActivity(), "0"
        )
        hobbyAdapter.setHasStableIds(true)
        hobbyAdapter.clickHandler = {
            val list = ArrayList<String>()
            for (value in hobbyAdapter.getList()) {
                list.add(value._id)
            }
            editProfile(EditProfileApiRequest(hobbies_interest = list), null, UpdateType.HOBBIES)
        }
        binding.rvHobby.apply {
            layoutManager =
                ChipsLayoutManager.newBuilder(requireContext()).setChildGravity(Gravity.TOP)
                    .setScrollingEnabled(true)
                    .setGravityResolver { Gravity.CENTER }
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            itemAnimator = DefaultItemAnimator()
            adapter = hobbyAdapter
        }

        imageAdapter = ImageStringAdapter(
            list,
            requireActivity(), "1", "0",
            object : ImageStringAdapter.ImageListener {
                override fun emptyClick(position: Int) {
                    back_press = ""
                    imageViewModel.clickedPosition = list.size
                    openImageUploadFragment(pos = imageViewModel.clickedPosition)
                }

                override fun itemClick(url: String, pos: Int) {
                    imageViewModel.clickedPosition = pos
//                    var uri = url
                    /*if (uri.contains(Constants.THUMB)) {
                        uri =
                            getProfileViewModel.viewModeUserImages[getProfileViewModel.profileData.value?.images?.get(pos)].toString()
                    }*/
                    val uri = RaddarApp.imagesMap[url] ?: requireContext().getImageUrl(url)
                    openImageUploadFragment(uri, pos)
                }

                override fun removeImage(
                    url: String,
                    position: Int,
                    view: View,
                    isRemoved: () -> Unit
                ) {
                    if (BaseUtils.isInternetAvailable()) {
                        if (position != 0) {
                            AlertDialog.Builder(requireContext())
                                .setMessage("Are you sure you want to delete this picture?")
                                .setPositiveButton(
                                    "Yes"
                                ) { dialog, _ ->
                                    view.visible(isVisible = true)
                                    imageAdapter.isDeleteInProgress = true
                                    imageViewModel.clickedPosition = position
                                    remove_imageList.add(image_listEdit[imageViewModel.clickedPosition])
                                    deleteImages(DeleteImagesRequest(remove_imageList), isRemoved)
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }.show()

                        } else {
                            imageViewModel.clickedPosition = position
                            openImageUploadFragment(url, position)
                        }
                    } else CommonCode.setToast(
                        requireContext(),
                        resources.getString(R.string.no_internet_msg)
                    )
                }
            }
        )

        binding.rvPhoto.adapter = imageAdapter

        dragDropImages()
    }

    fun openImageUploadFragment(url: String? = "", pos: Int? = 0) {
        /* val data = Bundle()
         data.putInt("tag", 1)
         data.putString("url", url)
         data.putInt("position", pos ?: 0)
         data.putStringArrayList("imageList_edit", image_listEdit)*/
        /*  findNavController()
              .navigate(R.id.action_editFragment_to_imageUploadFragment, data)*/

        val data = Bundle()
        data.putInt(Constants.TYPE, Constants.FROM_EDIT)
        data.putString(Constants.EXTRA_DATA, url)
        data.putInt(Constants.POSITION, pos ?: 0)
        data.putStringArrayList(Constants.EXTRA, getThreeImagesAccordingToPosition(pos ?: 0))
        data.putStringArrayList("imageList_edit", image_listEdit)
        this.view?.findNavController()?.navigate(R.id.action_editFragment_to_uploadFragment, data)
    }

    private fun getThreeImagesAccordingToPosition(pos: Int): ArrayList<String> {
        val threeImagesList = arrayListOf<String>()
        val listOfPositions: ArrayList<Int> = when (pos / 3) {
            0 -> {
                ArrayList<Int>().apply {
                    add(0)
                    add(1)
                    add(2)
                }
            }

            1 -> {
                ArrayList<Int>().apply {
                    add(3)
                    add(4)
                    add(5)
                }
            }

            2 -> {
                ArrayList<Int>().apply {
                    add(6)
                    add(7)
                    add(8)
                }
            }

            3 -> {
                ArrayList<Int>().apply {
                    add(9)
                    add(10)
                    add(11)
                }
            }

            4 -> {
                ArrayList<Int>().apply {
                    add(12)
                    add(13)
                    add(14)
                }
            }

            else -> {
                ArrayList<Int>().apply {
                    add(0)
                    add(1)
                    add(2)
                }
            }
        }
        threeImagesList.clear()
        listOfPositions.forEach {
            if (list.size > it) {
                threeImagesList.add(list[it])
            }
        }

        return threeImagesList
    }

    private fun dragDropImages() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun isLongPressDragEnabled() = true
            override fun isItemViewSwipeEnabled() = false

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags =
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                val swipeFlags =
                    if (isItemViewSwipeEnabled) ItemTouchHelper.START or ItemTouchHelper.END else 0
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewHolder.itemViewType != target.itemViewType)
                    return false
                val fromPosition = viewHolder.absoluteAdapterPosition
                val toPosition = target.absoluteAdapterPosition
                val item = list.removeAt(fromPosition)
                list.add(toPosition, item)
                /* for (value in list) {
                     (viewHolder as ImageStringAdapter.ViewHolder).itemView.iv_close1.visibility =
                         View.GONE
                 }*/
                val item2 = pathList.removeAt(fromPosition)
                pathList.add(toPosition, item2)
                /*val item3 = getProfileViewModel().profileData.value?.images?.removeAt(fromPosition)
                  getProfileViewModel().profileData.value?.images?.add(toPosition, item3!!)*/
                recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)
                /*   binding.tvSave.visibility = View.VISIBLE
                   binding.tvDrag.visibility = View.GONE*/
                return true
            }

            override fun onMoved(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                fromPos: Int,
                target: RecyclerView.ViewHolder,
                toPos: Int,
                x: Int,
                y: Int
            ) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        /* if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                             // Item successfully dragged
                             listener.onItemDragged(dragFromPosition, dragToPosition)
                             // Reset drag positions
                             dragFromPosition = -1
                             dragToPosition = -1
                         }*/
                        Handler(Looper.getMainLooper()).postDelayed({
                            imageAdapter.refresh()
                            editProfile(
                                EditProfileApiRequest(images = pathList),
                                null,
                                UpdateType.IMAGE
                            )
                        }, 150)


                        /*
                        * delete subscription
                        * subscription
                        * */
                    }
                }
//                imageAdapter.refresh()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.rvPhoto)


    }

    private fun initializeObserver() {
        if (view != null && isAdded && isVisible)
            getProfileViewModel.profileData.observe(viewLifecycleOwner) { profileData ->
                binding.swipeRefreshLayout.isRefreshing = false
                profileData?.let {
                    runBlocking {
                        preferencesHelper.saveUserData(it)
                    }
                    // set this value to true if images are less then 3 else set to false
                    // by using this key we will show image add confirmation dialog on back press
                    doWeNeedToShowImageDialog = (it.images?.size ?: 0) < 3

                    binding.clAddMore.visible((it.images?.size ?: 0) < 13)
                    binding.tvAdd.text =
                        StringBuilder().append("Upload(${15 - (it.images?.size ?: 0)} Remaining)")
                    setTitle(it.name ?: "")
                    hobbiesList.clear()
                    hobbiesList.addAll(it.hobbies_interest as ArrayList<HobbiesData>)
                    hobbyAdapter.refresh()

                    if (generalList.isEmpty()) {
                        generalList.addAll(it.getEditProfileGeneralContentList())
                        generalAdapter?.refresh()
                    } else {
                        if (generalList[1].value != profileData.username) {
                            generalList[1] = EditProfileGeneralContentData(
                                1,
                                EditProfileGeneralContentTypes.USERNAME,
                                "Username",
                                profileData.username ?: "",
                                profileData.username ?: ""
                            )

                            generalAdapter?.notifyItemChanged(1)
                        }
                    }

                    if (workEducationList.isEmpty()) {
                        workEducationList.addAll(it.getEditProfileWorkEducationContentList())
                        workEducationAdapter?.refresh()
                    }
                    try {
                        lat =
                            it.location.latlon?.coordinates?.get(1) ?: 0.0
                        longt =
                            it.location.latlon?.coordinates?.get(0) ?: 0.0
                        if (getProfileViewModel.isLocationUpdated) {
                            getProfileViewModel.isLocationUpdated = false
                            generalList[getProfileViewModel.locationPosition].value =
                                it.getEditProfileGeneralContentList()[getProfileViewModel.locationPosition].value
                            generalAdapter?.notifyItemChanged(
                                getProfileViewModel.locationPosition
                            )
                        }
                    } catch (e: Exception) {

                    }

                    it.images?.apply {
                        if (it.images.isNotEmpty()) {
                            for (i in it.images.indices) {
                                imageViewModel.imageUrlList[i] = it.images[i]
                            }

                            image_listEdit.clear()
                            image_listEdit.addAll(it.images)
                            pathList.clear()
                            pathList.addAll(it.images)
                            /*getProfileViewModel.storeImages(
                                it.images,
                                requireContext()
                            )
*/
                            list.clear()
                            list.addAll(this)
                            /*for (data in it.images) {
                                getProfileViewModel.userImages[data]?.let { image ->
                                    list.add(
                                        image
                                    )
                                }
                            }*/
                            imageAdapter.setSize()
                        }
                    }

                }
            }
    }

    private fun setGeneralAdapter() {
        generalAdapter =
            EditProfileGeneralContentAdapter(list = generalList, fromEditProfile = true) {
                if (itemClicked) return@EditProfileGeneralContentAdapter
                itemClicked = true
                it.handleAdapterClick()
            }
        generalAdapter?.setHasStableIds(true)
        binding.rvGeneralContent.adapter = generalAdapter
    }

    private fun setWorkEducationAdapter() {
        workEducationAdapter =
            EditProfileGeneralContentAdapter(list = workEducationList, fromEditProfile = true) {
                if (itemClicked) return@EditProfileGeneralContentAdapter
                itemClicked = true
                it.handleAdapterClick()
            }
        workEducationAdapter?.setHasStableIds(true)
        binding.rvWorkEducation.adapter = workEducationAdapter

    }

    private fun EditProfileGeneralContentData.handleAdapterClick() {
        if (contentType == EditProfileGeneralContentTypes.LOCATION) {
            itemClicked = false
            if (BaseUtils.isInternetAvailable()) {
                isLocationClicked = true
                getProfileViewModel.locationPosition = this.id
                if (requireContext().isGpsEnabled()) {
                    openLocationFragment()
                } else {
                    this@EditProfileFragment.view?.findNavController()
                        ?.navigate(R.id.enableLocationFragment)
                }
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        } else if (contentType == EditProfileGeneralContentTypes.USERNAME) {
            itemClicked = false
            if (BaseUtils.isInternetAvailable()) {
                this@EditProfileFragment.view?.findNavController()
                    ?.navigate(R.id.fragmentUpdateUsername)
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        } else {
            getProfileViewModel.profileData.value?.apply {
                openBottomSheetToUpdateProfile(
                    requireContext(),
                    profileData = this,
                    saveData = null,
                    fragment = this@EditProfileFragment
                ) { apiRequest, _ ->
                    editProfile(apiRequest, this@handleAdapterClick, UpdateType.OTHER)
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({ itemClicked = false }, 200)
        }
    }

    private fun openLocationFragment() {
        isLocationClicked = false
        val data = Bundle()
        data.putDouble("lati", lat)
        data.putDouble("longt", longt)
        data.putString("screen_tag", "0")
        findNavController()
            .navigate(R.id.location_Fragment, data)
    }

    fun clickListener() {
        binding.etAbout.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.etAbout.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.etAbout.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val inputMethodManager =
                    requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
                binding.etAbout.clearFocus()
                editProfile(
                    EditProfileApiRequest(about_me = getProfileViewModel.profileData.value?.about_me),
                    null,
                    UpdateType.ABOUT_ME
                )
            }
            false
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                handleBack()
            }

            R.id.tv_add_interest -> {
                if (!binding.progressBarInterests.isVisible) {
                    binding.progressBarInterests.visible(true)
                    getHobbies()
                }
            }

            R.id.tvViewMode -> {
                val bundle = Bundle().apply {
                    putSerializable(Constants.EXTRA_DATA, getProfileViewModel.profileData.value)
                    putString(Constants.USER_ID, getProfileViewModel.profileData.value?._id)
                    putInt(Constants.FROM, ProfileFragment.FROM_EDIT)
                }
                this.view?.findNavController()
                    ?.navigate(R.id.action_editProfile_to_ProfileFragment, bundle)
            }
        }
    }


    private fun editProfile(
        editProfileRequest: EditProfileApiRequest,
        editProfileGeneralContentData: EditProfileGeneralContentData?,
        type: UpdateType
    ) {
        if (view != null && isAdded && isVisible)
            try {
                lifecycleScope.launch {
                    getProfileViewModel.editProfile(editProfileRequest)
                        .observe(viewLifecycleOwner) {
                            when (it) {
                                is DataResult.Loading -> {}
                                is DataResult.Success -> {
                                    it.data.data.let {
                                        runBlocking(Dispatchers.IO) {
                                            preferencesHelper.saveDataEditProfile(
                                                it
                                            )
                                        }
                                    }
                                    /*it.data.data.replaceProfileImagesWithUrl(requireContext()) { data ->
                                        getProfileViewModel.profileData.value = data
                                    }*/

                                    mixPanelWrapper.setSuperProperties(data = it.data.data)
                                    getProfileViewModel.profileData.value = it.data.data
                                    it.data.data.apply {
                                        when (type) {
                                            UpdateType.IMAGE -> {
                                                if (it.data.data.images?.isNotEmpty() == true) {
                                                    val image = requireContext().getImageUrl(
                                                        it.data.data.images[0]
                                                    )
                                                    runBlocking {
                                                        preferencesHelper.saveImage(
                                                            image,
                                                            it.data.data.images[0]
                                                        )
                                                    }

                                                    (activity as HomeActivity?)?.binding?.homeToolbar?.ivUser?.loadImage(
                                                        image
                                                    )
                                                }

                                            }

                                            UpdateType.HOBBIES -> {

                                            }

                                            UpdateType.ABOUT_ME -> {

                                            }

                                            UpdateType.OTHER -> {
                                                editProfileGeneralContentData ?: return@observe

                                                if (editProfileGeneralContentData.contentType == EditProfileGeneralContentTypes.JOB_TITLE ||
                                                    editProfileGeneralContentData.contentType == EditProfileGeneralContentTypes.EDUCATION_LEVEL ||
                                                    editProfileGeneralContentData.contentType == EditProfileGeneralContentTypes.JOB ||
                                                    editProfileGeneralContentData.contentType == EditProfileGeneralContentTypes.SCHOOL
                                                ) {
                                                    workEducationList[editProfileGeneralContentData.id] =
                                                        this.getEditProfileWorkEducationContentList()[editProfileGeneralContentData.id]
                                                    workEducationAdapter?.notifyItemChanged(
                                                        editProfileGeneralContentData.id
                                                    )
                                                } else {
                                                    generalList[editProfileGeneralContentData.id] =
                                                        this.getEditProfileGeneralContentList()[editProfileGeneralContentData.id]
                                                    generalAdapter?.notifyItemChanged(
                                                        editProfileGeneralContentData.id
                                                    )
                                                }

                                            }
                                        }
                                    }


                                }

                                is DataResult.Failure -> {
                                    it.message?.let { msg ->
                                        requireContext().showToast(msg)
                                    }
                                    reportApiError(
                                        Exception().stackTrace[0].lineNumber,
                                        it.statusCode ?: 0,
                                        "user/edit-profile",
                                        requireActivity().componentName.className,
                                        it.message ?: ""
                                    )

                                    FirebaseCrashlytics.getInstance()
                                        .recordException(Exception("user/edit-profile Api Error"))
                                }

                                DataResult.Empty -> {}
                            }
                        }

                }
            } catch (e: Exception) {

            }
    }

    private fun deleteImages(deleteImagesRequest: DeleteImagesRequest, isRemoved: () -> Unit) {
        if (view != null && isAdded && isVisible)
            try {
                lifecycleScope.launch {
                    getProfileViewModel.deleteImage(deleteImagesRequest)
                        .observe(viewLifecycleOwner) {
                            when (it) {
                                is DataResult.Loading -> {

                                }

                                is DataResult.Success -> {
                                    getProfileViewModel.profileData.value?.let { profileData ->
//                                    if (profileData.images.isNotEmpty() && (profileData.images.size - 1) >= imageViewModel.clickedPosition)
                                        (profileData.images as ArrayList).remove(profileData.images[imageViewModel.clickedPosition])
                                    }
                                    deleteImagesRequest.images?.let { list ->
                                        if (list.isNotEmpty()) {
//                                            getProfileViewModel.userImages.remove(list[0])
//                                            getProfileViewModel.viewModeUserImages.remove(list[0])
                                        }
                                    }
                                    isRemoved()
                                    list.removeAt(imageViewModel.clickedPosition)
                                    pathList.removeAt(imageViewModel.clickedPosition)
                                    imageAdapter.refresh()
//                                pathList.removeAt(imageViewModel.clickedPosition)
//                                imageAdapter.remove(imageViewModel.clickedPosition)
                                    image_listEdit.removeAt(imageViewModel.clickedPosition)
                                    imageAdapter.isDeleteInProgress = false
                                }

                                is DataResult.Failure -> {
                                    isRemoved()
                                    imageAdapter.isDeleteInProgress = false
                                    reportApiError(
                                        Exception().stackTrace[0].lineNumber,
                                        it.statusCode ?: 0,
                                        "user/delete-image",
                                        requireActivity().componentName.className,
                                        it.message ?: ""
                                    )

                                    FirebaseCrashlytics.getInstance()
                                        .recordException(Exception("user/delete-image Api Error"))
                                }

                                else -> {}
                            }
                        }

                }
            } catch (e: Exception) {

            }
    }

    private fun getHobbies() {
        if (view != null && isAdded && isVisible)
            activity?.let { fragmentActivity ->
                fragmentActivity.lifecycleScope.launch {
                    getProfileViewModel.getAllHobbies().observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {
                            }

                            is DataResult.Failure -> {
                                binding.progressBarInterests.visible(isVisible = false)
                                it.message?.apply {
                                    requireContext().showToast(this)
                                }
                            }

                            DataResult.Loading -> {
                            }

                            is DataResult.Success -> {
                                getProfileViewModel.profileData.value?.let { data ->
                                    data.hobbies_interest?.apply {
                                        val idsObject = JSONObject()
                                        this.forEach { hobbiesData ->
                                            idsObject.put(
                                                hobbiesData._id,
                                                hobbiesData._id
                                            )
                                        }
                                        it.data.data.forEach { hobbiesAndInterestData ->
                                            if (idsObject.has(hobbiesAndInterestData._id)) {
                                                hobbiesAndInterestData.isSelected = true
                                            }
                                        }
                                    }
                                }
                                it.data.data.updateHobbiesAndInterestBottomSheet(requireContext()) { request ->
                                    editProfile(request, null, UpdateType.HOBBIES)
                                }
                                Handler(Looper.getMainLooper()).postDelayed({
                                    binding.progressBarInterests.visible(isVisible = false)
                                }, 500)
                            }
                        }
                    }
                }
            }
    }

    override fun onRefresh() {
        if (view != null && isAdded && isVisible)
            if (BaseUtils.isInternetAvailable()) {
                try {
                    try {
                        getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
                            data?.let {
                                getProfileViewModel.profileData.value = it
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                } catch (e: java.lang.Exception) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
    }

    /**
     * @see doWeNeedToShowImageDialog this variable is used to show add image confirmation dialog on back press
     *
     * @see RaddarApp.isBackConfirmationImageDialogShown this variable is used to handle one time show of dialog
     * @see list - this variable contains images data as Array
     * This function is called in HomeActivity under onBackPress function
     * if doWeNeedToShowImageDialog is true then show confirmation popup
     * else call findNavController()?.popBackStack()
     * */
    fun handleBack() {
        if (!RaddarApp.isBackConfirmationImageDialogShown && list.size < 3) {
            RaddarApp.isBackConfirmationImageDialogShown = true
            requireContext().addThreeImageDialogConfirmation(list.size) {
                if (!it) {
                    this.view?.findNavController()?.popBackStack()
                } else {
                    imageViewModel.clickedPosition = list.size
                    openImageUploadFragment(pos = imageViewModel.clickedPosition)
                }
            }
        } else {
            this.view?.findNavController()?.popBackStack()
        }

    }

}
