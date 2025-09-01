package com.radarqr.dating.android.ui.report

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentReportBinding
import com.radarqr.dating.android.ui.adapter.ReportAdapter
import com.radarqr.dating.android.ui.adapter.ReportType
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportFragment<T>(val type: ReportType) :
    BaseFragment<FragmentReportBinding>() {

    private val getProfileViewModel: GetProfileViewModel by viewModel()

    lateinit var clickHandler: (Int, ReportType, ArrayList<T>) -> Unit

    lateinit var submitClickHandler: DialogClickHandler<Pair<String, View>>

    var adapter: ReportAdapter<T>? = null
    override fun getLayoutRes(): Int = R.layout.fragment_report

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ReportAdapter(requireContext(), type)
        adapter?.clickHandler = { position, reportType, data ->
            clickHandler(position, reportType, data)
        }
        binding.rvReport.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            submitClickHandler.onClick(Pair(binding.etReason.text.toString(),it))
        }

        getProfileViewModel.reportAdapterObserver.value = Pair(true, type)

        setTouchListener(binding.etReason)
    }

    override fun onResume() {
        super.onResume()
//            binding.root.invalidate()
//            binding.root.requestLayout()

    }

    override fun onPause() {
        super.onPause()
        binding.etReason.setText("")
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setTouchListener(view: View) {
        view.setOnTouchListener(View.OnTouchListener { v, event ->
            if (view.hasFocus()) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_SCROLL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        return@OnTouchListener true
                    }
                }
            }
            false
        })
    }

    fun setList(t: ArrayList<T>, header: String) {
        adapter?.let { adapter ->
            adapter.clear {
                adapter.addAll(t)
            }
        }

        try {
            binding.tvReportName.visibility =
                if (type == ReportType.MENU) View.GONE else View.VISIBLE
            binding.tvReportName.text = header
            binding.rvReport.visibility = if (type == ReportType.EDIT) View.GONE else View.VISIBLE
            binding.etReason.visibility = if (type == ReportType.EDIT) View.VISIBLE else View.GONE
            binding.btnSubmit.visibility = if (type == ReportType.EDIT) View.VISIBLE else View.GONE


        } catch (e: UninitializedPropertyAccessException) {

        }


    }
}