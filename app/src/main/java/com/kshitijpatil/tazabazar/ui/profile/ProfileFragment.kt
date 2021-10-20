package com.kshitijpatil.tazabazar.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentProfileBinding
import com.kshitijpatil.tazabazar.di.ProfileViewModelFactory
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.ui.DashboardFragmentDirections
import com.kshitijpatil.tazabazar.util.UiState
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.util.tazabazarApplication
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory(tazabazarApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val loggedInUser = viewModel.viewState.value.loggedInUser
        updateVisibilityWith(loggedInUser)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener { openAuthNavigation() }
        binding.btnLogout.setOnClickListener {
            onLogout()
        }
        launchAndRepeatWithViewLifecycle {
            launch { updateUIWithLoggedInUser() }
        }
    }

    private fun onLogout() {
        val logoutJob = viewModel.logout()
        val stateObserver = viewModel.viewState
            .onEach { viewState ->
                binding.progressLogout.isVisible = viewState.logoutState is UiState.Loading
                binding.btnLogout.isVisible =
                    viewState.logoutState !is UiState.Loading && viewState.loggedInUser != null
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        logoutJob.invokeOnCompletion { stateObserver.cancel() }
    }

    private suspend fun updateUIWithLoggedInUser() {
        viewModel.selectSubscribe(ProfileViewState::loggedInUser)
            .collect {
                binding.progressUser.isVisible = true
                updateVisibilityWith(it)
                it?.let {
                    val fullName = it.fullName
                    binding.txtFullName.text = fullName
                    binding.txtPhone.text = it.phone
                    binding.txtAvatarLabel.text = fullName.take(1)
                }
                binding.progressUser.isVisible = false
            }
    }

    private fun updateVisibilityWith(user: LoggedInUser?) {
        binding.btnLogin.isVisible = user == null
        binding.cardviewProfile.isVisible = user != null
        binding.btnLogout.isVisible = user != null
    }

    private fun openAuthNavigation() {
        val direction = DashboardFragmentDirections.actionFragmentDashboardToNavigationAuth()
        requireActivity().findNavController(R.id.main_activity_nav_host_fragment)
            .navigate(direction)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}