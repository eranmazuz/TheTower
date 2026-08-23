package com.example.thetower.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.thetower.R
import com.example.thetower.data.model.GameState
import java.io.File
import java.time.LocalTime

@Composable
fun SettingsScreen(
    state: GameState,
    onLanguageChange: (String) -> Unit,
    onAlarmTimesChange: (Map<String, String>) -> Unit,
    onAlarmModeToggle: (Boolean) -> Unit,
    onHydrationTargetChange: (Double) -> Unit,
    onRingtoneUploaded: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Alarm inputs local state
    var morningTime by remember { mutableStateOf(state.alarmTimes["MORNING"] ?: "09:00") }
    var noonTime by remember { mutableStateOf(state.alarmTimes["NOON"] ?: "12:00") }
    var eveningTime by remember { mutableStateOf(state.alarmTimes["EVENING"] ?: "18:00") }
    var nightTime by remember { mutableStateOf(state.alarmTimes["NIGHT"] ?: "21:00") }
    var isTimeValid by remember { mutableStateOf(true) }

    // Validate times on typing
    LaunchedEffect(morningTime, noonTime, eveningTime, nightTime) {
        isTimeValid = isValidTime(morningTime) && isValidTime(noonTime) &&
                isValidTime(eveningTime) && isValidTime(nightTime)
    }

    // Hydration Target local state
    var targetSliderValue by remember { mutableFloatStateOf(state.hydrationTarget.toFloat()) }

    // Ringtone display name
    val ringtoneFile = File(context.filesDir, "user_ringtone.mp3")
    val ringtoneName = if (ringtoneFile.exists() && ringtoneFile.length() > 0) {
        "user_ringtone.mp3 (${String.format("%.2f", ringtoneFile.length().toFloat() / (1024 * 1024))} MB)"
    } else {
        null
    }

    // Alarm ringtone file picker
    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val copied = copyCustomRingtone(context, uri)
            if (copied) {
                onRingtoneUploaded()
            }
        }
    }

    // Permission warnings
    var showExactAlarmWarning by remember { mutableStateOf(false) }
    var showNotifWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            showExactAlarmWarning = !alarmManager.canScheduleExactAlarms()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            showNotifWarning = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Permission warnings
        if (showExactAlarmWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.exact_alarm_perm_warning),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (showNotifWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.notif_perm_warning),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Language Switcher Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onLanguageChange("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.appLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (state.appLanguage == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text(stringResource(R.string.settings_lang_en))
                    }
                    Button(
                        onClick = { onLanguageChange("he") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.appLanguage == "he") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (state.appLanguage == "he") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Text(stringResource(R.string.settings_lang_he))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hydration Target Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.water_tracker_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.water_target_label, targetSliderValue),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = targetSliderValue,
                    onValueChange = {
                        targetSliderValue = (Math.round(it * 10) / 10.0f)
                    },
                    onValueChangeFinished = {
                        onHydrationTargetChange(targetSliderValue.toDouble())
                    },
                    valueRange = 2.0f..4.0f,
                    steps = 19 // 2.0 to 4.0 in 0.1 increments is 20 points, steps is 20 - 1 = 19
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alarm Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_alarm_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = morningTime,
                        onValueChange = { morningTime = it },
                        label = { Text("Morning") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noonTime,
                        onValueChange = { noonTime = it },
                        label = { Text("Noon") },
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = eveningTime,
                        onValueChange = { eveningTime = it },
                        label = { Text("Evening") },
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nightTime,
                        onValueChange = { nightTime = it },
                        label = { Text("Night") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (isTimeValid) {
                            onAlarmTimesChange(
                                mapOf(
                                    "MORNING" to morningTime.trim(),
                                    "NOON" to noonTime.trim(),
                                    "EVENING" to eveningTime.trim(),
                                    "NIGHT" to nightTime.trim()
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isTimeValid
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alarm Mode Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alarm / Reminder Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                if (state.alarmModeActive) R.string.settings_alarm_mode else R.string.settings_notif_mode
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = state.alarmModeActive,
                        onCheckedChange = { onAlarmModeToggle(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Custom Ringtone Upload
                Text(
                    text = stringResource(R.string.upload_ringtone),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (ringtoneName != null) {
                        stringResource(R.string.custom_ringtone_active, ringtoneName)
                    } else {
                        stringResource(R.string.no_custom_ringtone)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        ringtoneLauncher.launch("audio/mpeg") // Launch picker for MP3 files
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select MP3 Ringtone File")
                }
            }
        }
    }
}

private fun isValidTime(timeStr: String): Boolean {
    return try {
        LocalTime.parse(timeStr)
        true
    } catch (e: Exception) {
        false
    }
}

private fun copyCustomRingtone(context: Context, uri: Uri): Boolean {
    return try {
        // Cap size at 5MB
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
        val sizeBytes = pfd?.statSize ?: 0
        pfd?.close()
        
        if (sizeBytes > 5 * 1024 * 1024) {
            return false // Too large
        }

        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val targetFile = File(context.filesDir, "user_ringtone.mp3")
        targetFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
