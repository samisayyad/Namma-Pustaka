package com.nammapustaka.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nammapustaka.databinding.FragmentStudentProfileBinding
import com.nammapustaka.utils.Resource
import com.nammapustaka.utils.loadAvatar
import com.nammapustaka.utils.showToast
import com.nammapustaka.viewmodel.AuthViewModel
import com.nammapustaka.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUser()
        setupClickListeners()
    }

    private fun observeUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.currentUser.collect { result ->
                    if (result is Resource.Success) {
                        val user = result.data
                        binding.tvUserName.text = user.name
                        binding.tvUserEmail.text = user.email
                        binding.tvUserClass.text = "Class ${user.classGrade}"
                        binding.tvBooksRead.text = user.totalBooksRead.toString()
                        binding.tvStreak.text = "${user.currentStreak} days"
                        binding.tvPoints.text = user.points.toString()
                        binding.ivAvatar.loadAvatar(user.avatarUrl)
                        binding.progressReading.progress = (user.totalBooksRead % 10) * 10
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.menuMyBooks.setOnClickListener { showToast("My Books coming soon") }
        binding.menuReadingHistory.setOnClickListener { showToast("Reading History coming soon") }
        binding.menuBadges.setOnClickListener { showToast("Badges coming soon") }
        binding.menuSettings.setOnClickListener { showToast("Settings coming soon") }
        binding.menuLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(com.nammapustaka.R.id.action_profile_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
