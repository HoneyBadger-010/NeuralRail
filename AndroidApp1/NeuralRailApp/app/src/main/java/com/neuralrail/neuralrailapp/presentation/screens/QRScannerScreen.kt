package com.neuralrail.neuralrailapp.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.UiState
import com.neuralrail.neuralrailapp.data.models.*
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.QRScannerViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


@Composable
fun QRScannerScreen(viewModel: QRScannerViewModel) {
    val context = LocalContext.current
    val scanResultState by viewModel.scanResultState.collectAsState()
    
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
    ) {
        // Modern Header
        ScannerHeader()
        
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                when (val result = scanResultState) {
                    null -> CameraPreviewWithScanner(
                        onQRCodeScanned = { viewModel.processQRCode(it) }
                    )
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GreenPrimary)
                        }
                    }
                    is UiState.Success -> {
                        ModernScanResultContent(result.data, onScanAgain = { viewModel.clearResult() })
                    }
                    is UiState.Error -> {
                        ErrorContent(result.message, onScanAgain = { viewModel.clearResult() })
                    }
                }
            } else {
                NoCameraPermission(onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }
        }
    }
}

@Composable
private fun ScannerHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GreenPrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📷", fontSize = 28.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    stringResource(R.string.qr_scanner),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    stringResource(R.string.scan_tickets_info),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewWithScanner(onQRCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val isScanning = remember { AtomicBoolean(true) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    
    val scannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
            .build()
    }
    val scanner = remember { BarcodeScanning.getClient(scannerOptions) }
    
    DisposableEffect(Unit) {
        onDispose {
            isScanning.set(false)
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
            scanner.close()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider
                        
                        camera = bindCameraUseCasesWithCamera(
                            provider = provider,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            scanner = scanner,
                            executor = cameraExecutor,
                            isScanning = isScanning,
                            onQRCodeScanned = onQRCodeScanned
                        )
                    } catch (e: Exception) {
                        Log.e("QRScanner", "Camera initialization failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Scanner overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(3.dp, BluePrimary, RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.point_camera_qr),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Flashlight button
                val flashlightText = if (isFlashlightOn) stringResource(R.string.turn_off_flashlight) else stringResource(R.string.turn_on_flashlight)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            camera?.let { cam ->
                                if (cam.cameraInfo.hasFlashUnit()) {
                                    isFlashlightOn = !isFlashlightOn
                                    cam.cameraControl.enableTorch(isFlashlightOn)
                                }
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isFlashlightOn) AccentOrange else Color.Black.copy(alpha = 0.7f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flashlight",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            flashlightText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun bindCameraUseCasesWithCamera(
    provider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    executor: ExecutorService,
    isScanning: AtomicBoolean,
    onQRCodeScanned: (String) -> Unit
): androidx.camera.core.Camera? {
    provider.unbindAll()
    
    val preview = Preview.Builder()
        .build()
        .also { it.setSurfaceProvider(previewView.surfaceProvider) }
    
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
    
    imageAnalysis.setAnalyzer(executor) { imageProxy ->
        processImageProxy(scanner, imageProxy, isScanning, onQRCodeScanned)
    }
    
    return try {
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
    } catch (e: Exception) {
        Log.e("QRScanner", "Use case binding failed", e)
        null
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    isScanning: AtomicBoolean,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    
    if (mediaImage == null || !isScanning.get()) {
        imageProxy.close()
        return
    }
    
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            if (isScanning.get()) {
                barcodes.firstOrNull()?.rawValue?.let { value ->
                    if (isScanning.compareAndSet(true, false)) {
                        onQRCodeScanned(value)
                    }
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("QRScanner", "Barcode scanning failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

@Composable
private fun ModernScanResultContent(result: QRScanResult, onScanAgain: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            when (result.type) {
                QRContentType.TRAIN_INFO -> result.trainInfo?.let { ModernTrainInfoCard(it, result.energyData) }
                QRContentType.TICKET -> result.ticketInfo?.let { ModernTicketCard(it) }
                QRContentType.STATION -> result.stationInfo?.let { ModernStationCard(it) }
                QRContentType.UNKNOWN -> ModernUnknownCard(result.rawValue)
            }
        }
        
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onScanAgain),
                color = GreenPrimary
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📷", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.scan_another_qr),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ModernTrainInfoCard(train: TrainStatus, energyData: TrainEnergyData? = null) {
    val appColors = LocalAppColors.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = GreenPrimary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🚂", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.train_information), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = appColors.textPrimary)
                    Text(stringResource(R.string.scanned_from_qr), fontSize = 12.sp, color = appColors.textSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            InfoRow("🔢", stringResource(R.string.train_number), train.trainNumber, appColors.textPrimary)
            InfoRow("📝", stringResource(R.string.train_name), train.trainName, appColors.textPrimary)
            
            // Status with color
            val statusColor = when (train.currentStatus) {
                TrainRunningStatus.ON_TIME -> GreenPrimary
                TrainRunningStatus.DELAYED -> WarningOrange
                TrainRunningStatus.STOPPED -> ErrorRed
                else -> appColors.textPrimary
            }
            InfoRow("📊", stringResource(R.string.status), train.currentStatus.name.replace("_", " "), statusColor)
            InfoRow("📍", stringResource(R.string.location), train.currentLocation, appColors.textPrimary)
            InfoRow("🚉", stringResource(R.string.next_station), train.nextStation, appColors.textPrimary)
            InfoRow("⏰", stringResource(R.string.expected_arrival), train.expectedArrival, appColors.textPrimary)
            
            if (train.delay > 0) {
                InfoRow("⚠️", stringResource(R.string.delay), "${train.delay} ${stringResource(R.string.minutes)}", ErrorRed)
                train.delayReason?.let {
                    InfoRow("❓", stringResource(R.string.reason), getDelayReasonText(it), ErrorRed)
                }
            }
            
            // Route info if available
            if (train.route.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.route_progress), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appColors.textPrimary)
                Spacer(Modifier.height(8.dp))
                train.route.forEach { stop ->
                    RouteStopItem(stop, appColors)
                }
            }
        }
    }
    
    // Energy Data Card
    energyData?.let { energy ->
        Spacer(Modifier.height(12.dp))
        TrainEnergyCard(energy, appColors)
    }
}

@Composable
private fun RouteStopItem(stop: StationStop, appColors: AppColors) {
    val statusColor = when (stop.status) {
        StopStatus.COMPLETED -> GreenPrimary
        StopStatus.CURRENT -> BluePrimary
        StopStatus.UPCOMING -> appColors.textMuted
        StopStatus.SKIPPED -> ErrorRed
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = RoundedCornerShape(5.dp),
            color = statusColor
        ) {}
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stop.stationName,
                fontSize = 13.sp,
                fontWeight = if (stop.status == StopStatus.CURRENT) FontWeight.Bold else FontWeight.Normal,
                color = if (stop.status == StopStatus.CURRENT) BluePrimary else appColors.textPrimary
            )
            Text(
                "${stop.stationCode} • Platform ${stop.platform ?: "TBD"}",
                fontSize = 11.sp,
                color = appColors.textSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                stop.scheduledArrival,
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
            if (stop.actualArrival != null && stop.actualArrival != stop.scheduledArrival) {
                Text(
                    stop.actualArrival,
                    fontSize = 11.sp,
                    color = WarningOrange
                )
            }
        }
    }
}

@Composable
private fun TrainEnergyCard(energy: TrainEnergyData, appColors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = appColors.backgroundCard,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = AccentGreen.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚡", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.energy_sustainability), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = appColors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (energy.isElectric) {
                            Text("🔋", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.electric_train), fontSize = 12.sp, color = GreenPrimary)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Energy Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnergyStatItem("⚡", "${energy.currentPowerUsage.toInt()}", stringResource(R.string.kw_usage), appColors)
                EnergyStatItem("♻️", "${energy.regenerativeRecovery.toInt()}%", stringResource(R.string.recovered), appColors)
                EnergyStatItem("🌱", "${energy.renewablePercent.toInt()}%", stringResource(R.string.renewable), appColors)
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Efficiency Score
            val excellentText = stringResource(R.string.excellent)
            val goodText = stringResource(R.string.good)
            val averageText = stringResource(R.string.average)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = GreenPrimary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.energy_efficiency_score), fontSize = 13.sp, color = appColors.textSecondary)
                        Text("${energy.energyEfficiencyScore}/100", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GreenPrimary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenPrimary
                    ) {
                        Text(
                            if (energy.energyEfficiencyScore >= 90) excellentText else if (energy.energyEfficiencyScore >= 70) goodText else averageText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // More stats
            InfoRow("🌍", stringResource(R.string.co2_saved_today), "${energy.co2SavedToday} ${stringResource(R.string.kg)}", GreenPrimary)
            InfoRow("🛤️", stringResource(R.string.distance_traveled), "${energy.totalKmTraveled.toInt()} ${stringResource(R.string.km)}", appColors.textPrimary)
            InfoRow("☀️", stringResource(R.string.solar_coaches), "${energy.solarPoweredCoaches} ${stringResource(R.string.coaches)}", SolarYellow)
        }
    }
}

@Composable
private fun EnergyStatItem(emoji: String, value: String, label: String, appColors: AppColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = appColors.textPrimary)
        Text(label, fontSize = 11.sp, color = appColors.textSecondary)
    }
}

@Composable
private fun ModernTicketCard(ticket: TicketInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = EnergyBlue.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎫", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.ticket_information), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(stringResource(R.string.e_ticket_details), fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(16.dp))
            InfoRow("🔢", stringResource(R.string.pnr), ticket.pnr)
            InfoRow("🚂", stringResource(R.string.train), "${ticket.trainNumber} - ${ticket.trainName}")
            InfoRow("🚉", stringResource(R.string.from), ticket.from)
            InfoRow("📍", stringResource(R.string.to), ticket.to)
            InfoRow("📅", stringResource(R.string.date), ticket.journeyDate)
            InfoRow("💺", stringResource(R.string.coach_seat), "${ticket.coach} / ${ticket.seat}")
            InfoRow("👤", stringResource(R.string.passenger), ticket.passengerName)
            InfoRow("✅", stringResource(R.string.status), ticket.status, if (ticket.status == "Confirmed") GreenPrimary else WarningOrange)
        }
    }
}

@Composable
private fun ModernStationCard(station: StationInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = GreenSecondary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🏛️", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.station_information), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (station.isGreenStation) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.green_station), fontSize = 12.sp, color = GreenPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            InfoRow("🔤", stringResource(R.string.station_code), station.stationCode)
            InfoRow("📝", stringResource(R.string.station_name), station.stationName)
            InfoRow("🚉", stringResource(R.string.platforms), station.platforms.toString())
            if (station.isGreenStation) {
                InfoRow("☀️", stringResource(R.string.solar_capacity), "${station.solarCapacity} kW", SolarYellow)
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.amenities), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                station.amenities.take(4).forEach { amenity ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            amenity,
                            fontSize = 11.sp,
                            color = GreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernUnknownCard(rawValue: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Gray.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📄", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.qr_code_content), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.raw_content), fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    rawValue,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(emoji: String, label: String, value: String, valueColor: Color = Color.DarkGray) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.Gray, fontSize = 14.sp)
        }
        Text(value, fontWeight = FontWeight.Medium, color = valueColor, fontSize = 14.sp)
    }
}

@Composable
private fun ErrorContent(message: String, onScanAgain: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ErrorRed.copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("❌", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(message, color = ErrorRed, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onScanAgain),
            color = GreenPrimary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.try_again),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun NoCameraPermission(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📷", fontSize = 64.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.camera_permission_required),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.grant_camera_permission),
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(Modifier.height(24.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onRequestPermission),
            color = GreenPrimary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.grant_permission),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun getDelayReasonText(reason: DelayReason): String = when (reason) {
    DelayReason.SIGNAL_FAILURE -> stringResource(R.string.signal_failure)
    DelayReason.TRACK_MAINTENANCE -> stringResource(R.string.track_maintenance)
    DelayReason.WEATHER_CONDITIONS -> stringResource(R.string.weather_conditions)
    DelayReason.TECHNICAL_ISSUE -> stringResource(R.string.technical_issue)
    DelayReason.PASSENGER_EMERGENCY -> stringResource(R.string.passenger_emergency)
    DelayReason.SECURITY_CHECK -> stringResource(R.string.security_check)
    DelayReason.CONGESTION -> stringResource(R.string.track_congestion)
    DelayReason.ACCIDENT_AHEAD -> stringResource(R.string.accident_ahead)
    DelayReason.POWER_FAILURE -> stringResource(R.string.power_failure)
    DelayReason.CREW_CHANGE -> stringResource(R.string.crew_change)
    DelayReason.UNKNOWN -> stringResource(R.string.unknown)
}
