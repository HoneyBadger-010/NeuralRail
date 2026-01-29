package com.neuralrail.neuralrailapp.presentation.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.data.repository.SettingsRepository
import com.neuralrail.neuralrailapp.presentation.theme.*

// Helper function to vibrate the device safely
private fun vibrateDevice(context: Context) {
    try {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        vibrator?.let {
            if (it.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(100)
                }
            }
        }
    } catch (e: Exception) {
        // Silently fail if vibration is not available
        e.printStackTrace()
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    
    // Use shared settings repository
    val notificationsEnabled by SettingsRepository.notificationsEnabled.collectAsState()
    val darkModeEnabled by SettingsRepository.darkModeEnabled.collectAsState()
    val locationEnabled by SettingsRepository.locationEnabled.collectAsState()
    val dataSync by SettingsRepository.dataSyncEnabled.collectAsState()
    val appColors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BluePrimary, shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White) }
                Icon(Icons.Default.Settings, null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.settings), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(stringResource(R.string.customize_experience), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text(stringResource(R.string.general), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { 
                SettingsToggle(Icons.Default.Notifications, stringResource(R.string.push_notifications), stringResource(R.string.receive_updates), AccentYellow, notificationsEnabled, appColors) { 
                    SettingsRepository.setNotificationsEnabled(it)
                    // Vibrate when notifications are turned OFF
                    if (!it) {
                        vibrateDevice(context)
                    }
                } 
            }
            item { SettingsToggle(Icons.Default.DarkMode, stringResource(R.string.dark_mode), stringResource(R.string.use_dark_theme), BlueAccent, darkModeEnabled, appColors) { SettingsRepository.setDarkModeEnabled(it) } }
            item { SettingsToggle(Icons.Default.LocationOn, stringResource(R.string.location_services), stringResource(R.string.enable_nearby_stations), AccentGreen, locationEnabled, appColors) { SettingsRepository.setLocationEnabled(it) } }
            item { SettingsToggle(Icons.Default.Sync, stringResource(R.string.auto_sync), stringResource(R.string.sync_automatically), AccentCyan, dataSync, appColors) { SettingsRepository.setDataSyncEnabled(it) } }
            
            item { Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.language), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { 
                LanguageSelector(appColors) { languageCode ->
                    SettingsRepository.setLanguage(languageCode)
                    // Activity needs to be recreated to apply language change
                    (context as? android.app.Activity)?.recreate()
                }
            }
            
            item { Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.data_storage), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { SettingsItem(Icons.Default.Storage, stringResource(R.string.clear_cache), stringResource(R.string.free_storage), AccentOrange, appColors) }
            item { SettingsItem(Icons.Default.Download, stringResource(R.string.download_maps), stringResource(R.string.offline_access), BlueAccent, appColors) }
            item { SettingsItem(Icons.Default.Backup, stringResource(R.string.backup_data), stringResource(R.string.sync_to_cloud), AccentGreen, appColors) }
            
            item { Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.support), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = appColors.textPrimary, modifier = Modifier.padding(vertical = 4.dp)) }
            item { SettingsItem(Icons.Default.Help, stringResource(R.string.help_center), stringResource(R.string.faqs_guides), AccentCyan, appColors) }
            item { SettingsItem(Icons.Default.Feedback, stringResource(R.string.send_feedback), stringResource(R.string.help_improve), AccentYellow, appColors) }
            item { SettingsItem(Icons.Default.BugReport, stringResource(R.string.report_bug), stringResource(R.string.let_us_know), AccentRed, appColors) }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


@Composable
private fun SettingsToggle(icon: ImageVector, title: String, subtitle: String, color: Color, checked: Boolean, appColors: AppColors, onCheckedChange: (Boolean) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = appColors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = appColors.textSecondary)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = color, uncheckedThumbColor = appColors.textSecondary, uncheckedTrackColor = appColors.divider))
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, color: Color, appColors: AppColors) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = appColors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = appColors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = appColors.textSecondary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun LanguageSelector(appColors: AppColors, onLanguageChange: (String) -> Unit) {
    val currentLanguage by SettingsRepository.selectedLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = appColors.backgroundCard) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp), color = AccentCyan.copy(alpha = 0.2f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Language, null, tint = AccentCyan, modifier = Modifier.size(24.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.language), fontWeight = FontWeight.Medium, fontSize = 15.sp, color = appColors.textPrimary)
                    Text(if (currentLanguage == "te") stringResource(R.string.telugu) else stringResource(R.string.english), fontSize = 12.sp, color = appColors.textSecondary)
                }
                
                Row {
                    // English option
                    Surface(
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentLanguage == "en") AccentGreen else appColors.background
                    ) {
                        TextButton(onClick = { onLanguageChange("en") }) {
                            Text("EN", color = if (currentLanguage == "en") Color.White else appColors.textSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Telugu option
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentLanguage == "te") AccentGreen else appColors.background
                    ) {
                        TextButton(onClick = { onLanguageChange("te") }) {
                            Text("తె", color = if (currentLanguage == "te") Color.White else appColors.textSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
