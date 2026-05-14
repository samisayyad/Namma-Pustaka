package com.nammapustaka.ui.aichat

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammapustaka.databinding.FragmentAiChatBinding
import com.nammapustaka.ui.adapter.ChatAdapter
import com.nammapustaka.viewmodel.AiChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiChatViewModel by viewModels()
    private val chatAdapter = ChatAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.layoutManager = layoutManager
        binding.rvChat.adapter = chatAdapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        binding.ivClear.setOnClickListener { viewModel.clearChat() }

        // Quick suggestion chips
        binding.chipSuggest1.setOnClickListener { sendQuickMessage("Suggest books for Class 8 students") }
        binding.chipSuggest2.setOnClickListener { sendQuickMessage("What is the best book to read this month?") }
        binding.chipSuggest3.setOnClickListener { sendQuickMessage("Tell me about Kannada literature books") }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.etMessage.setText("")
        viewModel.sendMessage(text)
    }

    private fun sendQuickMessage(message: String) {
        viewModel.sendMessage(message)
        binding.chipGroupSuggestions.isVisible = false
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messages.collect { messages ->
                        chatAdapter.submitList(messages) {
                            binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }
                }
                launch {
                    viewModel.isTyping.collect { isTyping ->
                        binding.lottieTyping.isVisible = isTyping
                        binding.btnSend.isEnabled = !isTyping
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
