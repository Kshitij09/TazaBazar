package com.kshitijpatil.tazabazar.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.fredporciuncula.phonemoji.PhonemojiTextInputEditText
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentSignupBinding
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.ui.MainActivityViewModel
import com.kshitijpatil.tazabazar.ui.common.LifecycleAwareJobManager
import com.kshitijpatil.tazabazar.ui.common.ResourceMessage
import com.kshitijpatil.tazabazar.ui.common.TextMessage
import com.kshitijpatil.tazabazar.util.*
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding: FragmentSignupBinding get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by navGraphViewModels(R.id.navigation_auth) {
        AuthViewModelFactory(this, requireContext().applicationContext, arguments)
    }

    private val emailState = FieldState { it.isValidEmail() }
    private val passwordState = FieldState { !it.isNullOrEmpty() }
    private val fullNameState = FieldState { !it.isNullOrEmpty() }
    private val phoneState = FieldState {
        phoneEditText?.isTextValidInternationalPhoneNumber() == true
    }
    private val confirmPasswordState =
        FieldState { it.toString() == passwordState.currentText.toString() }

    private val signupJobManager = LifecycleAwareJobManager(cancelOnBackPressed = true)
    private var snackbar: FadingSnackbar? = null
    private var phoneEditText: PhonemojiTextInputEditText? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(signupJobManager.backPressCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        snackbar = binding.snackbar
        phoneEditText = binding.textFieldPhone.editText as? PhonemojiTextInputEditText
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityViewModel.disableClearFocus()
        binding.btnAction.setOnClickListener {
            signupJobManager.handleCancellation { authViewModel.register() }
        }
        launchTextFieldObservers()
        launchAndRepeatWithViewLifecycle {
            launch { observeStateAggregatorForActionButton() }
            launch { observeRegisterState() }
            launch { observeSnackbarMessages() }
            launch { observeLoginState() }
        }
        restoreFieldStates()
    }

    private suspend fun observeSnackbarMessages() {
        authViewModel.snackbarMessages
            .collect { message ->
                when (message) {
                    is ResourceMessage -> snackbar?.show(message.resId)
                    is TextMessage -> snackbar?.show(messageText = message.text)
                }
            }
    }

    private suspend fun observeRegisterState() {
        getRegisterStateFlow().collect {
            setProgressComponents(it)
            if (it is UiState.Success) {
                signupJobManager.handleCancellation { authViewModel.login() }
            }
        }
    }

    private suspend fun observeLoginState() {
        authViewModel.viewState.map { it.loginState }
            .collect {
                binding.progressIndicator.isVisible = it is UiState.Loading
                binding.progressMessage.isVisible = it is UiState.Loading
                if (it is UiState.Loading) {
                    binding.progressMessage.text =
                        resources.getString(it.msgResId ?: R.string.loading)
                }
                if (it is UiState.Success) {
                    showWelcomeMessage(it.value)
                }
            }
    }

    private fun showWelcomeMessage(user: LoggedInUser) {
        snackbar?.show(
            messageText = resources.getString(
                R.string.info_welcome_new_user,
                user.fullName
            ),
            longDuration = false,
            dismissListener = { popOutOfAuthNavigation() }
        )
    }

    private fun popOutOfAuthNavigation() {
        findNavController().navigate(R.id.action_pop_out_of_auth)
    }

    private fun <T> setProgressComponents(targetState: UiState<T>) {
        binding.progressIndicator.isVisible = targetState is UiState.Loading
        binding.progressMessage.isVisible = targetState is UiState.Loading
        if (targetState is UiState.Loading) {
            binding.progressMessage.text =
                resources.getString(targetState.msgResId ?: R.string.loading)
        }
    }

    private fun getRegisterStateFlow(): Flow<UiState<LoggedInUser>> {
        return authViewModel.viewState
            .map { it.registerState }
            .distinctUntilChanged()
    }

    private suspend fun observeStateAggregatorForActionButton() {
        getActionEnabledAggregatorFlow().collect {
            binding.btnAction.isEnabled = it
        }
    }

    private fun getActionEnabledAggregatorFlow(): Flow<Boolean> {
        val fieldValidationFlow = combine(
            fullNameState.isValid,
            emailState.isValid,
            phoneState.isValid,
            passwordState.isValid,
            confirmPasswordState.isValid,
        ) { fullName, email, phone, password, confirmPassword ->
            fullName && email && phone && password && confirmPassword
        }
        val registerStateCheckFlow =
            getRegisterStateFlow().map { it is UiState.Idle || it is UiState.Error }

        return combine(
            fieldValidationFlow,
            registerStateCheckFlow
        ) { fieldsValid, registerStateValid ->
            fieldsValid && registerStateValid
        }
    }

    private fun restoreFieldStates() {
        val viewState = authViewModel.viewState.value
        binding.textFieldName.editText?.setText(viewState.fullName)
        if (!viewState.phone.isNullOrEmpty()) {
            binding.textFieldPhone.editText?.setText(viewState.phone)
        }
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