package com.learner.lm.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.ui.components.HomeworkCameraPreview
import com.learner.lm.ui.components.NotebookBadge
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.ui.theme.AppColors
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
import com.learner.lm.viewmodel.ScannerViewModel

@Composable
fun ScannerScreen(
    userId: String,
    subscriptionTier: String,
    onTextScanned: (String) -> Unit,
    onNavigateToUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setCameraPermission(granted)
    }

    LaunchedEffect(userId, subscriptionTier) {
        viewModel.setUser(userId, subscriptionTier)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermission(granted)
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.scanSucceeded, uiState.extractedText) {
        val text = uiState.extractedText
        if (uiState.scanSucceeded && text != null) {
            onTextScanned(text)
            viewModel.resetScanResult()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Homework scanner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Point your camera at a worksheet. We'll extract the text and send it to your tutor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.isQuotaLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                NotebookBadge(
                    text = uiState.quotaLabel,
                    highlighted = uiState.isPremium
                )
            }
            if (!uiState.isPremium && !uiState.canScan) {
                Text(
                    text = "Limit reached",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            shape = RoundedCornerShape(AppRadii.xl),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            shadowElevation = 4.dp
        ) {
            when {
                !uiState.hasCameraPermission -> {
                    CameraPermissionPrompt(
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
                uiState.isProcessing -> {
                    ProcessingOverlay()
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeworkCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onImageCaptureReady = viewModel::bindImageCapture
                        )
                        ViewfinderOverlay()
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Surface(
                shape = RoundedCornerShape(AppRadii.md),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (!uiState.canScan && !uiState.isPremium) {
            NotebookCard(elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.ProGold)
                        Text(
                            text = "Daily scan limit reached",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Text(
                        text = "Standard includes 3 homework scans per day. Premium unlocks unlimited scanning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onNavigateToUpgrade,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppRadii.md)
                    ) {
                        Text("Upgrade for unlimited scans", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            Button(
                onClick = viewModel::captureAndScan,
                enabled = uiState.hasCameraPermission && !uiState.isProcessing && uiState.canScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(AppRadii.md),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Capture & scan", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        NotebookCard(elevated = false) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tips for best results",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                listOf(
                    "Use good lighting and hold the phone steady",
                    "Fill the frame with the worksheet text",
                    "After scanning, open Chat to ask questions about it"
                ).forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 8.dp, top = 2.dp)
                                .size(16.dp)
                        )
                        Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionPrompt(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "Camera access required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Allow camera access to scan homework pages.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = AppSpacing.md)
        )
        Button(onClick = onRequestPermission, shape = RoundedCornerShape(AppRadii.md)) {
            Text("Allow camera")
        }
    }
}

@Composable
private fun ProcessingOverlay() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp)
        Text(
            text = "Reading text…",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = AppSpacing.md),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ViewfinderOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.lg)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(AppRadii.lg))
                .background(AppColors.Accent.copy(alpha = 0.08f))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(AppRadii.lg))
                    .background(androidx.compose.ui.graphics.Color.Transparent)
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = AppSpacing.md),
            shape = RoundedCornerShape(AppRadii.pill),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ) {
            Text(
                text = "Align worksheet inside the frame",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
