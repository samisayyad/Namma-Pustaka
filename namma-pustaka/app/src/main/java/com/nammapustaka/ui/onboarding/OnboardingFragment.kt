package com.nammapustaka.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nammapustaka.R
import com.nammapustaka.databinding.FragmentOnboardingPagerBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingPagerBinding? = null
    private val binding get() = _binding!!

    private val pages = listOf(
        OnboardingPage(
            title = "Discover Books",
            description = "Explore thousands of books curated for your school library. Find what you love.",
            lottieRes = R.raw.anim_onboarding_books
        ),
        OnboardingPage(
            title = "AI Librarian",
            description = "Ask our AI assistant anything. Get summaries, recommendations, and insights instantly.",
            lottieRes = R.raw.anim_onboarding_ai
        ),
        OnboardingPage(
            title = "Track & Learn",
            description = "Monitor your reading journey, earn badges, and climb the leaderboard with your friends.",
            lottieRes = R.raw.anim_onboarding_trophy
        )
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = OnboardingPagerAdapter(this, pages)
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.size - 1) {
                binding.viewPager.currentItem = current + 1
                if (current + 1 == pages.size - 1) {
                    binding.btnNext.text = getString(R.string.get_started)
                }
            } else {
                navigateToLogin()
            }
        }

        binding.tvSkip.setOnClickListener { navigateToLogin() }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_onboarding_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieRes: Int
) : java.io.Serializable

class OnboardingPagerAdapter(fragment: Fragment, private val pages: List<OnboardingPage>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount() = pages.size

    override fun createFragment(position: Int): Fragment {
        return OnboardingPageFragment.newInstance(pages[position])
    }
}
