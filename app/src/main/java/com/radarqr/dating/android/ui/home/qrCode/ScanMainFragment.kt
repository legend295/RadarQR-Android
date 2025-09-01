package com.radarqr.dating.android.ui.home.qrCode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentScanMainBinding
import java.util.*


class ScanMainFragment : BaseFragment<FragmentScanMainBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scanMainFragment = this
        showToolbarLayout(false)
        showNavigation(true)

        setupViewPager(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager)



        binding.ivInfo.setOnClickListener {
            findNavController().navigate(R.id.action_to_common_dialog)
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_scan_main

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(ScanQRFragment(), getString(R.string.scan))
        adapter.addFragment(ShowQRFragment(), getString(R.string.my_code))
        viewPager.adapter = adapter
    }

    internal class ViewPagerAdapter(manager: FragmentManager?) :
        FragmentPagerAdapter(manager!!) {
        private val mFragmentList: MutableList<Fragment> =
            ArrayList()
        private val mFragmentTitleList: MutableList<String> =
            ArrayList()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(
            fragment: Fragment,
            title: String
        ) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }


}
