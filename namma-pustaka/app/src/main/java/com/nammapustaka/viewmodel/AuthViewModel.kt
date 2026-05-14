package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.User
import com.nammapustaka.data.repository.UserRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()

    fun isLoggedIn(): Boolean = userRepo.isLoggedIn()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = userRepo.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String, role: String, schoolId: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = userRepo.register(name, email, password, role, schoolId)
        }
    }

    fun logout() = userRepo.logout()
}
