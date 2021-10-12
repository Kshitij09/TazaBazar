package com.kshitijpatil.tazabazar.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentSigninSignupBinding
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.ui.MainActivityViewModel
import com.kshitijpatil.tazabazar.ui.common.LifecycleAwareJobManager
import com.kshitijpatil.tazabazar.ui.common.ResourceMessage
import com.kshitijpatil.tazabazar.ui.common.TextMessage
import com.kshitijpatil.tazabazar.util.*
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    private var _binding: FragmentSigninSignupBinding? = null
    private val binding: FragmentSigninSignupBinding get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by navGraphViewModels(R.id.navigation_auth) {
        AuthViewModelFactory(this, requireContext().applicationContext, arguments)
    }
    private val emailState = FieldState { it.isValidEmail() }
    private val passwordState = FieldState { !it.isNullOrEmpty() }
    private var snackbar: FadingSnackbar? = null
    private val loginJobManager = LifecycleAwareJobManager(cancelOnBackPressed = true)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            loginJobManager.backPressCallback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSigninSignupBinding.inflate(inflater, container, false)
        snackbar = binding.snackbar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityViewModel.disableClearFocus()
        viewLifecycleOwner.lifecycle.addObserver(loginJobManager)
        binding.btnAction.setOnClickListener {
            loginJobManager.handleCancellation { authViewModel.login() }
        }
        launchTextFieldObservers()
        launchAndRepeatWithViewLifecycle {
            launch { observeLoginState() }
            launch { observeSnackbarMessages() }
            launch { observeEnableActionButton() }
        }
        restoreFieldState()
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
                R.string.info_welcome_back_user,
                user.fullName
            ),
            longDuration = false,
            dismissListener = { popOutOfAuthNavigation() }
        )
    }

    private fun popOutOfAuthNavigation() {
        findNavController().navigate(R.id.action_pop_out_of_auth)
    }

    private fun launchTextFieldObservers() {
        launchAndRepeatWithViewLifecycle {
            launchTextInputLayoutObservers(
                textInputLayout = binding.textFieldEmail,
                fieldState = emailState,
                onTextChanged = { authViewModel.updateUsername(it) },
                getErrorFor = { resources.getString(R.string.error_must_be_valid_email) }
            )

            launchTextInputLayoutObservers(
                textInputLayout = binding.textFieldPassword,
                fieldState = passwordState,
                onTextChanged = { authViewModel.updatePassword(it) },
            )
        }
    }

    private suspend fun observeEnableActionButton() {
        combine(emailState.isValid, passwordState.isValid) { emailValid, passwordValid ->
            emailValid && passwordValid
        }.collect { enableActionButton ->
            binding.btnAction.isEnabled = enableActionButton
        }
    }

    private fun restoreFieldState() {
        val viewState = authViewModel.viewState.value
        viewState.username?.let { binding.textFieldEmail.editText?.setText(it) }
    }

    override fun onDestroyView() {
        snackbar = null
        _binding = null
        authViewModel.clearPassword()
        mainActivityViewModel.resetClearFocusToDefault()
        super.onDestroyView()
    }
}