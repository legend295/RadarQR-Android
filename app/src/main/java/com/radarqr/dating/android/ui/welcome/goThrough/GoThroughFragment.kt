package com.radarqr.dating.android.ui.welcome.goThrough

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentWelcomeScreensBinding
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.goThrough.adapter.GoThroughAdapter
import com.radarqr.dating.android.ui.welcome.goThrough.adapter.ViewPagerAdapter
import com.radarqr.dating.android.ui.welcome.goThrough.screens.FirstScreen
import com.radarqr.dating.android.ui.welcome.goThrough.screens.SecondScreen
import com.radarqr.dating.android.ui.welcome.goThrough.screens.ThirdScreen


class GoThroughFragment : BaseFragment<FragmentWelcomeScreensBinding>() {
    var page = 0
    var timer = 5000
    val handler = Handler(Looper.getMainLooper())
    lateinit var runnable: Runnable
    private var screenList = ArrayList<GoThroughModel>()
    var viewPagerAdapter: ViewPagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.goThroughFragment = this
//        showToolbarLayout(false)
//        showToolbarWhite(true)

        binding.tvGetStarted.setOnClickListener {
            this.findNavController()
                .navigate(R.id.action_goThroughFragment_to_loginMainFragment, arguments)
        }
//        settingLayout()

        init()
        val mainIntent = requireActivity().intent
        val type = mainIntent.getIntExtra(Constants.TYPE, 0)
        if (type == Constants.REPORT) {
            showReportDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity?)?.hideShowWholeToolbar(isVisible = false)
        }
    }

    private fun init() {
        viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter?.apply {
            addFragment(FirstScreen())
            addFragment(SecondScreen())
            addFragment(ThirdScreen())
            binding.viewPager.adapter = this
            binding.viewPager.offscreenPageLimit = getFragments().size
            binding.indicator.setViewPager(binding.viewPager)

            runnable = Runnable {
                if (getFragments().size == page) {
                    page = 0
                } else {
                    page++
                }
                binding.viewPager.setCurrentItem(page, true)
                handler.postDelayed(runnable, timer.toLong())
            }
            handler.postDelayed(runnable, timer.toLong())

            binding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    page = binding.viewPager.currentItem
                    timer = 5000
                }
            })
        }

    }

    private fun settingLayout() {


        var ob1: GoThroughModel = GoThroughModel(
            R.drawable.ic_screen_one,
            getString(R.string.screen_one),
            getString(R.string.screen_one_des)
        )
        this.screenList.add(ob1)
        ob1 = GoThroughModel(
            R.drawable.ic_screen_two,
            getString(R.string.screen_two),
            resources.getString(R.string.screen_two_des)
        )
        this.screenList.add(ob1)
        ob1 = GoThroughModel(
            R.drawable.ic_screen_three,
            getString(R.string.screen_three),
            getString(R.string.screen_three_des)
        )
        this.screenList.add(ob1)
        ob1 = GoThroughModel(
            R.drawable.ic_screen_four,
            getString(R.string.screen_four),
            getString(R.string.screen_four_des)
        )
        this.screenList.add(ob1)

        ob1 = GoThroughModel(
            R.drawable.ic_screen_five,
            getString(R.string.screen_five),
            getString(R.string.screen_five_des)
        )
        this.screenList.add(ob1)
        ob1 = GoThroughModel(
            R.drawable.ic_screen_six,
            getString(R.string.screen_six),
            getString(R.string.screen_six_des)
        )
        this.screenList.add(ob1)
        val adapter = GoThroughAdapter(screenList)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.adapter = adapter
        binding.indicator.setViewPager(binding.viewPager)
        runnable = Runnable {
            if (adapter.itemCount == page) {
                page = 0
            } else {
                page++
            }
            binding.viewPager.setCurrentItem(page, true)
            handler.postDelayed(runnable, timer.toLong())
        }
        handler.postDelayed(runnable, timer.toLong())

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                page = binding.viewPager.currentItem
                timer = 5000
            }
        })

    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_welcome_screens
    }

}
