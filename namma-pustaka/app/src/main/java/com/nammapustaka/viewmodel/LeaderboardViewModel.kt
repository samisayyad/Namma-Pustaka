package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.LeaderboardEntry
import com.nammapustaka.data.repository.UserRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _leaderboard = MutableStateFlow<Resource<List<LeaderboardEntry>>>(Resource.Loading())
    val leaderboard: StateFlow<Resource<List<LeaderboardEntry>>> = _leaderboard.asStateFlow()

    private val _currentUserEntry = MutableStateFlow<LeaderboardEntry?>(null)
    val currentUserEntry: StateFlow<LeaderboardEntry?> = _currentUserEntry.asStateFlow()

    init {
        viewModelScope.launch {
            userRepo.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    val schoolId = result.data.schoolId
                    loadLeaderboard(schoolId)
                }
            }
        }
    }

    private fun loadLeaderboard(schoolId: String) {
        viewModelScope.launch {
            userRepo.getLeaderboard(schoolId).collect { result ->
                _leaderboard.value = result
                if (result is Resource.Success) {
                    _currentUserEntry.value = result.data.find { it.isCurrentUser }
                }
            }
        }
    }
}
