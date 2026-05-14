package com.nammapustaka.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nammapustaka.R
import com.nammapustaka.databinding.FragmentLoginBinding
import com.nammapustaka.utils.Resource
import com.nammapustaka.utils.showToast
import com.nammapustaka.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole = "STUDENT"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeRegisterState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.text = getString(R.string.register)
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val name = "Student" // In real app, add name field to layout
            val schoolId = "SCHOOL_001" // In real app, get from input
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(name, email, password, selectedRole, schoolId)
            } else {
                showToast("Please fill all fields")
            }
        }
    }

    private fun observeRegisterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.btnLogin.isEnabled = false
                        }
                        is Resource.Success -> {
                            binding.progressBar.isVisible = false
                            val user = state.data
                            if (user.role == "TEACHER") {
                                findNavController().navigate(R.id.action_login_to_teacher_dashboard)
                            } else {
                                findNavController().navigate(R.id.action_register_to_home)
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            showToast(state.message)
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
