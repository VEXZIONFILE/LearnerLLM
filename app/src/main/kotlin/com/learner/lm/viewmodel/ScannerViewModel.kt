package com.learner.lm.viewmodel

import android.app.Application
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.ocr.HomeworkScanner
import com.learner.lm.repository.ScanQuotaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val hasCameraPermission: Boolean = false,
    val isProcessing: Boolean = false,
    val extractedText: String? = null,
    val error: String? = null,
    val quotaLabel: String = "",
    val canScan: Boolean = true,
    val isPremium: Boolean = false,
    val scanSucceeded: Boolean = false
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val scanQuotaRepository = ScanQuotaRepository(application)
    private val homeworkScanner = HomeworkScanner()
    private var imageCapture: ImageCapture? = null
    private var subscriptionTier: String = "FREE"

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun setSubscriptionTier(tier: String) {
        subscriptionTier = tier
        refreshQuota()
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun bindImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun refreshQuota() {
        val premium = scanQuotaRepository.isPremiumTier(subscriptionTier)
        _uiState.update {
            it.copy(
                isPremium = premium,
                quotaLabel = scanQuotaRepository.quotaLabel(subscriptionTier),
                canScan = scanQuotaRepository.canScan(subscriptionTier)
            )
        }
    }

    fun captureAndScan() {
        val capture = imageCapture
        if (capture == null) {
            _uiState.update { it.copy(error = "Camera not ready. Try again in a moment.") }
            return
        }
        if (!scanQuotaRepository.canScan(subscriptionTier)) {
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
                                scanQuotaRepository.recordScan(subscriptionTier)
                                refreshQuota()
                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        extractedText = text,
                                        scanSucceeded = true,
                                        error = null
                                    )
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
