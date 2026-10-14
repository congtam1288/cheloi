package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Checking : ConnectionState()
    data class Success(val modelName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
    private val aiRepository = AiRepository()

    private val _apiKey = MutableStateFlow(
        prefs.getString("gemini_api_key", null) ?: BuildConfig.GEMINI_API_KEY
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isKeyVisible = MutableStateFlow(false)
    val isKeyVisible: StateFlow<Boolean> = _isKeyVisible.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(
        ConnectionState.Success("Gemini 3.5 Flash")
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _saveToast = MutableStateFlow<String?>(null)
    val saveToast: StateFlow<String?> = _saveToast.asStateFlow()

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun toggleKeyVisibility() {
        _isKeyVisible.value = !_isKeyVisible.value
    }

    fun testConnection() {
        _connectionState.value = ConnectionState.Checking
        viewModelScope.launch {
            val result = aiRepository.testConnection(_apiKey.value)
            result.onSuccess { model ->
                _connectionState.value = ConnectionState.Success(model)
            }.onFailure { err ->
                _connectionState.value = ConnectionState.Error(err.message ?: "Kết nối thất bại")
            }
        }
    }

    fun saveConfiguration() {
        prefs.edit().putString("gemini_api_key", _apiKey.value).apply()
        _saveToast.value = "Đã lưu cấu hình thành công!"
    }

    fun clearToast() {
        _saveToast.value = null
    }
}
