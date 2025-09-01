package com.radarqr.dating.android.ui.home.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentProfileMainBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.utility.PreferencesHelper
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList


var tv_name_p: TextView = TODO()

class ProfileMainFragment : BaseFragment<FragmentProfileMainBinding>() {
    private val tabIcons = intArrayOf(
        R.drawable.ic_fill_color,
        R.drawable.ic_view
    )
    var name = ""
    var titleArray = ArrayList<String>()

    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileMainFragment = this


        /*  if (getProfileViewModel.stateSaved) {
              return
          }
  */
        runBlocking{
            name = preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_FIRSTNAME)
                .first() ?: ""
            setTitle(name)
        }
        setupViewPager(binding.viewPager)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titleArray[position]
        }.attach()
        setupTabIcons()
        binding.viewPager.isUserInputEnabled = false
        binding.tvCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_profile_main

    private fun setupViewPager(viewPager: ViewPager2) {
        titleArray.clear()
        titleArray.add(getString(R.string.edit))
        titleArray.add(getString(R.string.view_mode))
        val adapter = ViewPagerAdapter(childFragmentManager, viewLifecycleOwner)
        adapter.addFragment(EditProfileFragment(), getString(R.string.edit))
        adapter.addFragment(ViewProfileFragment(), getString(R.string.view_mode))
        viewPager.adapter = adapter
    }

    internal class ViewPagerAdapter(manager: FragmentManager, lifecycleOwner: LifecycleOwner) :
        FragmentStateAdapter(manager, lifecycleOwner.lifecycle) {
        private val mFragmentList: MutableList<Fragment> =
            ArrayList()
        private val mFragmentTitleList: MutableList<String> =
            ArrayList()

        fun addFragment(
            fragment: Fragment,
            title: String
        ) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getItemCount(): Int = mFragmentList.size

        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }
    }

    private fun setupTabIcons() {
        val newTab = LayoutInflater.from(requireActivity())
            .inflate(R.layout.layout_custom_tab, null) as TextView
        newTab.text = "Edit" //tab label txt
        newTab.compoundDrawablePadding = 15
        newTab.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_fill_color, 0)
        binding.tabLayout.getTabAt(0)?.customView = newTab

        val newTab1 = LayoutInflater.from(requireActivity())
            .inflate(R.layout.layout_custom_tab, null) as TextView
        newTab1.text = "View Mode" //tab label txt
        newTab1.compoundDrawablePadding = 15
        newTab1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_view, 0)
        binding.tabLayout.getTabAt(1)?.customView = newTab1

    }


}
