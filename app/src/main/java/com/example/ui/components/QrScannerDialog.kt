package com.example.ui.components

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantCard
import com.example.ui.theme.VibrantPrimary
import com.example.ui.theme.VibrantSurface
import com.example.ui.theme.VibrantTextMuted
import com.example.ui.theme.VibrantTextOnPrimary
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var manualCodeInput by remember { mutableStateOf("") }
    var isScanned by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Laser Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VibrantSurface,
            border = BorderStroke(1.dp, VibrantBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VibrantCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = VibrantPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Scan Room QR Code",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_scanner_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VibrantTextMuted
                        )
                    }
                }

                // Camera Scanner Viewport or Permission Request
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(VibrantCard)
                        .border(1.5.dp, VibrantBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

                        DisposableEffect(Unit) {
                            onDispose {
                                cameraExecutor.shutdown()
                            }
                        }

                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    val reader = MultiFormatReader()

                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        if (!isScanned) {
                                            val scannedResult = scanBarcode(imageProxy, reader)
                                            if (scannedResult != null) {
                                                isScanned = true
                                                previewView.post {
                                                    onQrScanned(scannedResult)
                                                }
                                            }
                                        }
                                        imageProxy.close()
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        // Ignore or handle camera binding failure
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Viewfinder Overlay Reticle
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .border(2.dp, VibrantPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Animated Laser Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset(y = laserOffset.dp)
                                    .background(VibrantPrimary)
                            )
                        }
                    } else {
                        // Camera Permission Request State
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = VibrantPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Camera permission needed to scan QR code.",
                                color = VibrantTextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantPrimary,
                                    contentColor = VibrantTextOnPrimary
                                )
                            ) {
                                Text("Grant Permission", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Or Paste / Enter Code Manually
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OR ENTER CODE MANUALLY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantTextSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualCodeInput,
                            onValueChange = { manualCodeInput = it },
                            placeholder = {
                                Text("Room Code or IP", color = VibrantTextMuted, fontSize = 13.sp)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VibrantCard,
                                unfocusedContainerColor = VibrantCard,
                                focusedBorderColor = VibrantPrimary,
                                unfocusedBorderColor = VibrantBorder,
                                focusedTextColor = VibrantTextPrimary,
                                unfocusedTextColor = VibrantTextPrimary
                            ),
                            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val item = clipboard.primaryClip?.getItemAt(0)
                                val pasted = item?.text?.toString()?.trim()
                                if (!pasted.isNullOrEmpty()) {
                                    manualCodeInput = pasted
                                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(VibrantCard)
                                .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = VibrantPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank()) {
                                    onQrScanned(manualCodeInput.trim())
                                }
                            },
                            enabled = manualCodeInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VibrantPrimary,
                                contentColor = VibrantTextOnPrimary,
                                disabledContainerColor = VibrantCard,
                                disabledContentColor = VibrantTextMuted
                            ),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Join", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun scanBarcode(imageProxy: ImageProxy, reader: MultiFormatReader): String? {
    return try {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val width = imageProxy.width
        val height = imageProxy.height
        val source = PlanarYUVLuminanceSource(
            bytes,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result = reader.decodeWithState(binaryBitmap)
        result.text
    } catch (_: NotFoundException) {
        null
    } catch (_: Exception) {
        null
    } finally {
        reader.reset()
    }
}
