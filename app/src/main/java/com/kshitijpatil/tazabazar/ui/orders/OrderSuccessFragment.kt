package com.kshitijpatil.tazabazar.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.kshitijpatil.tazabazar.R

class OrderSuccessFragment : Fragment(R.layout.fragment_order_success) {
    private val args: OrderSuccessFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val affirmationText =
            getString(R.string.info_order_success_affirmation_template, args.userFullName)
        view?.let {
            it.findViewById<TextView>(R.id.txt_affirmation).text = affirmationText
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialButton>(R.id.btn_track_order)
            .setOnClickListener { navigationUp() }
    }

    private fun navigationUp() {
        /*requireActivity()
            .findNavController(R.id.main_activity_nav_host_fragment)
            .popBackStack()*/
        findNavController().navigateUp()
    }
}