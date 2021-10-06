package com.kshitijpatil.tazabazar.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentSigninSignupBinding
import com.kshitijpatil.tazabazar.ui.MainActivityViewModel

class SignUpFragment : Fragment() {
    private var _binding: FragmentSigninSignupBinding? = null
    private val binding: FragmentSigninSignupBinding get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSigninSignupBinding.inflate(inflater, container, false)
        val context = requireContext()
        binding.textFieldName.isVisible = true
        binding.textFieldConfirmPassword.isVisible = true
        binding.txtHeader.text = context.getString(R.string.label_sign_up)
        binding.btnAction.text = context.getText(R.string.label_create_account)
        binding.textFieldPassword.editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityViewModel.disableClearFocus()
    }

    override fun onDestroyView() {
        _binding = null
        mainActivityViewModel.resetClearFocusToDefault()
        super.onDestroyView()
    }
}