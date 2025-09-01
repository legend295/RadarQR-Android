package com.radarqr.dating.android.ui.bottomSheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.radarqr.dating.android.databinding.LayoutBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBottomSheetFragment : BottomSheetDialogFragment() {
lateinit var layoutBottomSheetBinding: LayoutBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutBottomSheetBinding = LayoutBottomSheetBinding.inflate(inflater, container, false)

        return layoutBottomSheetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//}
    }



    private var mListener: ItemClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ItemClickListener) {
            mListener = context as ItemClickListener
        } else {
            throw RuntimeException(
                context.toString()
                    .toString() + " must implement ItemClickListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
    interface ItemClickListener {
        fun onItemClick(item: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): OptionsBottomSheetFragment {
            val fragment = OptionsBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}