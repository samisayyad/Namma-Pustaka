package com.nammapustaka.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nammapustaka.databinding.FragmentOnboardingPageBinding

class OnboardingPageFragment : Fragment() {

    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val page = arguments?.getSerializable(ARG_PAGE) as? OnboardingPage ?: return
        binding.tvTitle.text = page.title
        binding.tvDescription.text = page.description
        binding.lottieAnim.setAnimation(page.lottieRes)
        binding.lottieAnim.playAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PAGE = "page"
        fun newInstance(page: OnboardingPage): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = Bundle().apply { putSerializable(ARG_PAGE, page) }
            }
        }
    }
}
