package com.radarqr.dating.android.ui.home.likes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentLikesMainBinding
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.utility.PreferencesHelper
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LikesMainFragment : BaseFragment<FragmentLikesMainBinding>() {
    companion object {
        var newTab: TabLayout? = null
    }

    var adapter: ViewPagerAdapter? = null
    private val tabIcons = intArrayOf(
        R.drawable.ic_fill_color,
        R.drawable.ic_view
    )
    var fragList: ArrayList<String> = ArrayList()
    var name = ""
    private val likesViewModel: LikesViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.likeMainFragment = this

        showToolbarLayout(false)
        newTab = binding.tabLayout
        showNavigation(true)
        setPager()
    }

    private fun setPager() {
        setupViewPager(binding.viewPager)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragList[position]

        }.attach()
    }

    override fun getLayoutRes(): Int = R.layout.fragment_likes_main

    private fun setupViewPager(viewPager: ViewPager2) {
        adapter =
            ViewPagerAdapter(childFragmentManager, viewLifecycleOwner)
        fragList.clear()
        fragList.add(getString(R.string.all))
        fragList.add(getString(R.string.online))
        fragList.add(getString(R.string.inperson))
//        adapter?.addFragment(AllLikesFragment(), getString(R.string.all_likes))
//        adapter?.addFragment(OnlineLikesFragment(), getString(R.string.online))
//        adapter?.addFragment(InPersonLikesFragment(), getString(R.string.inperson))
        viewPager.adapter = adapter
    }


//
//    private fun setupTabIcons() {
//        newTab = LayoutInflater.from(requireActivity()).inflate(
//            R.layout.layout_custom_tabview,
//            null
//        )
//        newTab?.findViewById<TextView>(R.id.tv_text)?.setText(getString(R.string.all_likes))
//        newTab?.findViewById<TextView>(R.id.tv_count)?.setText(getString(R.string.all_likes))
//        binding.tabLayout.getTabAt(0)?.setCustomView(newTab)
//
//        var newTab1 = LayoutInflater.from(requireActivity()).inflate(
//            R.layout.layout_custom_tab,
//            null
//        ) as TextView
//        newTab1.text = "View Mode" //tab label txt
//        newTab1.compoundDrawablePadding = 15
//        newTab1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_view, 0)
//        binding.tabLayout.getTabAt(1)?.setCustomView(newTab1)
//        var newTab2 = LayoutInflater.from(requireActivity()).inflate(
//            R.layout.layout_custom_tab,
//            null
//        ) as TextView
//        newTab2.text = "View Mode" //tab label txt
//        newTab2.compoundDrawablePadding = 15
//        newTab2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_view, 0)
//        binding.tabLayout.getTabAt(2)?.setCustomView(newTab2)
//
//    }

    class ViewPagerAdapter(manager: FragmentManager?, lifecycleOwner: LifecycleOwner) :
        FragmentStateAdapter(manager!!, lifecycleOwner.lifecycle) {

        private val mFragmentList: MutableList<Fragment> =
            ArrayList()
        private val mFragmentTitleList: MutableList<String> =
            ArrayList()

        override fun getItemCount(): Int {
            return mFragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }

        fun addFragment(
            fragment: Fragment,
            title: String
        ) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        fun getFragment(): MutableList<Fragment> = mFragmentList

    }


}
