package com.kshitijpatil.tazabazar.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.navGraphViewModels
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentSigninSignupBinding
import com.kshitijpatil.tazabazar.ui.MainActivityViewModel
import com.kshitijpatil.tazabazar.ui.common.LifecycleAwareJobManager
import com.kshitijpatil.tazabazar.util.FieldState
import com.kshitijpatil.tazabazar.util.isValidEmail
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.util.launchTextInputLayoutObservers

class SignUpFragment : Fragment() {
    private var _binding: FragmentSigninSignupBinding? = null
    private val binding: FragmentSigninSignupBinding get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by navGraphViewModels(R.id.navigation_auth) {
        AuthViewModelFactory(this, requireContext().applicationContext, arguments)
    }

    private val emailState = FieldState { it.isValidEmail() }
    private val passwordState = FieldState { !it.isNullOrEmpty() }
    private val fullNameState = FieldState { !it.isNullOrEmpty() }
    private val phoneState = FieldState {
        // regex reference: https://stackoverflow.com/a/5933940/6738702
        !it.isNullOrEmpty() && it.matches("""^\+[1-9]{1}[0-9]{3,14}${'$'}""".toRegex())
    }
    private val confirmPasswordState =
        FieldState { it.toString() == passwordState.currentText.toString() }

    private val signupJobManager = LifecycleAwareJobManager(cancelOnBackPressed = true)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(signupJobManager.backPressCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSigninSignupBinding.inflate(inflater, container, false)
        binding.textFieldName.isVisible = true
        binding.textFieldPhone.isVisible = true
        binding.textFieldConfirmPassword.isVisible = true
        binding.txtHeader.text = resources.getString(R.string.label_sign_up)
        binding.btnAction.text = resources.getText(R.string.label_create_account)
        binding.textFieldPassword.editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityViewModel.disableClearFocus()
        launchTextFieldObservers()
    }

    private fun launchTextFieldObservers() {
        launchAndRepeatWithViewLifecycle {
            launchTextInputLayoutObservers(
                binding.textFieldName,
                fullNameState,
                onTextChanged = { authViewModel.updateFullName(it) },
                getErrorFor = { emptyFieldErrorProducer(R.string.label_name).getError() }
            )

            launchTextInputLayoutObservers(
                binding.textFieldPhone,
                phoneState,
                onTextChanged = { authViewModel.updatePhone(it) },
                getErrorFor = { resources.getString(R.string.error_invalid_phone_number) }
            )

            launchTextInputLayoutObservers(
                binding.textFieldEmail,
                emailState,
                onTextChanged = { authViewModel.updateUsername(it) },
                getErrorFor = { resources.getString(R.string.error_must_be_valid_email) }
            )

            launchTextInputLayoutObservers(
                binding.textFieldPassword,
                passwordState,
                onTextChanged = { authViewModel.updatePassword(it) },
                getErrorFor = { emptyFieldErrorProducer(R.string.label_password).getError() }
            )

            launchTextInputLayoutObservers(
                binding.textFieldConfirmPassword,
                confirmPasswordState,
                onTextChanged = { },
                getErrorFor = { getString(R.string.error_passwords_must_be_same) }
            )
        }
    }

    private fun emptyFieldErrorProducer(@StringRes labelResId: Int): EmptyFieldErrorProducer {
        return EmptyFieldErrorProducer {
            val label = resources.getString(labelResId)
            resources.getString(R.string.error_field_must_not_be_empty, label)
        }
    }

    override fun onDestroyView() {
        _binding = null
        mainActivityViewModel.resetClearFocusToDefault()
        super.onDestroyView()
    }
}


fun interface EmptyFieldErrorProducer {
    fun getError(): String
}