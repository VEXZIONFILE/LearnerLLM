package com.learner.lm.viewmodel

import android.app.Application
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.billing.ScanQuotaExceededException
import com.learner.lm.ocr.HomeworkScanner
import com.learner.lm.repository.ScanQuotaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isCameraActive: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isProcessing: Boolean = false,
    val isQuotaLoading: Boolean = false,
    val extractedText: String? = null,
    val error: String? = null,
    val quotaLabel: String = "",
    val canScan: Boolean = false,
    val isPremium: Boolean = false,
    val scanSucceeded: Boolean = false
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val scanQuotaRepository = ScanQuotaRepository(application)
    private val homeworkScanner = HomeworkScanner()
    private var imageCapture: ImageCapture? = null
    private var userId: String = ""
    private var subscriptionTier: String = "FREE"

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun setUser(userId: String, subscriptionTier: String) {
        this.userId = userId
        this.subscriptionTier = subscriptionTier
        refreshQuota()
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun openCamera() {
        _uiState.update { it.copy(isCameraActive = true, error = null) }
    }

    fun closeCamera() {
        imageCapture = null
        _uiState.update { it.copy(isCameraActive = false) }
    }

    fun bindImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun refreshQuota() {
        if (userId.isBlank()) {
            _uiState.update {
                it.copy(
                    isQuotaLoading = false,
                    canScan = false,
                    quotaLabel = "",
                    error = "Sign in to scan homework."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isQuotaLoading = true, error = null) }
            scanQuotaRepository.fetchStatus(userId, subscriptionTier)
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            isQuotaLoading = false,
                            isPremium = status.isPremium,
                            quotaLabel = status.quotaLabel,
                            canScan = status.canScan
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isQuotaLoading = false,
                            canScan = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun captureAndScan() {
        val capture = imageCapture
        if (capture == null) {
            _uiState.update { it.copy(error = "Camera not ready. Try again in a moment.") }
            return
        }
        if (userId.isBlank()) {
            _uiState.update { it.copy(error = "Sign in to scan homework.") }
            return
        }
        if (!_uiState.value.canScan && !_uiState.value.isPremium) {
            _uiState.update {
                it.copy(
                    error = "Daily scan limit reached. Upgrade to Premium for unlimited homework scans.",
                    canScan = false
                )
            }
            return
        }

        _uiState.update { it.copy(isProcessing = true, error = null, scanSucceeded = false) }

        capture.takePicture(
            ContextCompat.getMainExecutor(getApplication()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    viewModelScope.launch {
                        val result = homeworkScanner.extractText(image)
                        result.fold(
                            onSuccess = { text ->
                                scanQuotaRepository.recordSuccessfulScan(userId, subscriptionTier)
                                    .onSuccess { status ->
                                        _uiState.update {
                                            it.copy(
                                                isProcessing = false,
                                                extractedText = text,
                                                scanSucceeded = true,
                                                error = null,
                                                isPremium = status.isPremium,
                                                quotaLabel = status.quotaLabel,
                                                canScan = status.canScan
                                            )
                                        }
                                    }
                                    .onFailure { quotaError ->
                                        val message = when (quotaError) {
                                            is ScanQuotaExceededException ->
                                                "Daily scan limit reached. Upgrade to Premium for unlimited homework scans."
                                            else -> quotaError.message
                                                ?: "Could not verify scan quota."
                                        }
                                        _uiState.update {
                                            it.copy(
                                                isProcessing = false,
                                                scanSucceeded = false,
                                                error = message,
                                                canScan = false
                                            )
                                        }
                                        refreshQuota()
                                    }
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        error = error.message ?: "Could not read text from photo",
                                        scanSucceeded = false
                                    )
                                }
                            }
                        )
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = exception.message ?: "Camera capture failed",
                            scanSucceeded = false
                        )
                    }
                }
            }
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetScanResult() {
        _uiState.update { it.copy(extractedText = null, scanSucceeded = false) }
    }

    override fun onCleared() {
        homeworkScanner.close()
        super.onCleared()
    }
}
