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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.thetower.theme.RpgBorder
import com.example.thetower.theme.RpgButtonDark
import com.example.thetower.theme.RpgCardSurface
import com.example.thetower.theme.RpgCyan
import com.example.thetower.theme.RpgEmerald
import com.example.thetower.theme.RpgGold
import com.example.thetower.theme.RpgRuby
import com.example.thetower.theme.RpgSlotSurface
import com.example.thetower.theme.RpgTextPrimary
import com.example.thetower.theme.RpgTextSecondary
import java.io.File
import java.time.LocalTime

@Composable
fun SettingsScreen(
    state: GameState,
    onLanguageChange: (String) -> Unit,
    onAlarmTimesChange: (Map<String, String>) -> Unit,
    onAlarmModeToggle: (Boolean) -> Unit,
    onHydrationTargetChange: (Double) -> Unit,
    onRingtoneUploaded: () -> Unit,
    onResetProgress: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Alarm inputs local state
    var morningTime by remember { mutableStateOf(state.alarmTimes["MORNING"] ?: "09:00") }
    var noonTime by remember { mutableStateOf(state.alarmTimes["NOON"] ?: "12:00") }
    var eveningTime by remember { mutableStateOf(state.alarmTimes["EVENING"] ?: "18:00") }
    var nightTime by remember { mutableStateOf(state.alarmTimes["NIGHT"] ?: "21:00") }
    var isTimeValid by remember { mutableStateOf(true) }

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

    // Reset Confirmation Dialog State
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = RpgRuby
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_desc),
                    color = RpgTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RpgRuby)
                ) {
                    Text(
                        stringResource(R.string.settings_reset_confirm_button),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel), color = RpgTextSecondary)
                }
            },
            containerColor = RpgCardSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(scrollState)
    ) {
        // --- 1. SYSTEM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.settings_title).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = RpgCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "System Configuration & Guild Codex",
                    fontSize = 12.sp,
                    color = RpgTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RpgSlotSurface)
                    .border(1.dp, RpgGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "v1.0.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RpgGold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Permission warnings
        if (showExactAlarmWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgRuby.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, RpgRuby, RoundedCornerShape(16.dp)),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.exact_alarm_perm_warning),
                    color = RpgRuby,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (showNotifWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RpgRuby.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, RpgRuby, RoundedCornerShape(16.dp)),
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
                    color = RpgRuby,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // --- 2. HYDRATION TARGET CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.water_tracker_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RpgSlotSurface)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", targetSliderValue)}L",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RpgCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = targetSliderValue,
                    onValueChange = {
                        targetSliderValue = (Math.round(it * 10) / 10.0f)
                    },
                    onValueChangeFinished = {
                        onHydrationTargetChange(targetSliderValue.toDouble())
                    },
                    valueRange = 1.0f..5.0f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = RpgCyan,
                        activeTrackColor = RpgCyan,
                        inactiveTrackColor = RpgSlotSurface
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.settings_water_desc),
                    fontSize = 12.sp,
                    color = RpgTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. SYSTEM LANGUAGE CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isEnglish = state.appLanguage == "en"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isEnglish) RpgGold else RpgSlotSurface)
                            .clickable { onLanguageChange("en") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_lang_en),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isEnglish) Color(0xFF13121D) else Color.White
                        )
                    }

                    val isHebrew = state.appLanguage == "he"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isHebrew) RpgGold else RpgSlotSurface)
                            .clickable { onLanguageChange("he") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_lang_he),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isHebrew) Color(0xFF13121D) else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 4. GAME RULES & MECHANICS GUIDE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📜", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_rules_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = RpgBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                RuleBulletItem(text = stringResource(R.string.settings_rules_quests), color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                RuleBulletItem(text = stringResource(R.string.settings_rules_missed), color = RpgRuby)
                Spacer(modifier = Modifier.height(8.dp))
                RuleBulletItem(text = stringResource(R.string.settings_rules_temptation), color = RpgRuby)
                Spacer(modifier = Modifier.height(8.dp))
                RuleBulletItem(text = stringResource(R.string.settings_rules_fainting), color = RpgTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                RuleBulletItem(text = stringResource(R.string.settings_rules_merchant), color = RpgGold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 5. TACTICAL COMBAT TIPS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_tips_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = RpgBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.settings_tips_1),
                    fontSize = 13.sp,
                    color = RpgTextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.settings_tips_2),
                    fontSize = 13.sp,
                    color = RpgTextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.settings_tips_3),
                    fontSize = 13.sp,
                    color = RpgTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 6. REMINDER TIME SLOTS & ALARM MODE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RpgCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔔", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RPG Habit Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                if (state.alarmModeActive) R.string.settings_alarm_mode else R.string.settings_notif_mode
                            ),
                            fontSize = 11.sp,
                            color = RpgTextSecondary
                        )
                    }

                    Switch(
                        checked = state.alarmModeActive,
                        onCheckedChange = { onAlarmModeToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RpgGold,
                            checkedTrackColor = RpgSlotSurface,
                            uncheckedThumbColor = RpgTextSecondary,
                            uncheckedTrackColor = RpgButtonDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = RpgBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.settings_alarm_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RpgGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeSlotField("Morning", morningTime, Modifier.weight(1f)) { morningTime = it }
                    TimeSlotField("Noon", noonTime, Modifier.weight(1f)) { noonTime = it }
                    TimeSlotField("Evening", eveningTime, Modifier.weight(1f)) { eveningTime = it }
                    TimeSlotField("Night", nightTime, Modifier.weight(1f)) { nightTime = it }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RpgGold),
                    enabled = isTimeValid
                ) {
                    Text(
                        stringResource(R.string.save),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF13121D)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Ringtone Upload
                Text(
                    text = stringResource(R.string.upload_ringtone),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (ringtoneName != null) {
                        stringResource(R.string.custom_ringtone_active, ringtoneName)
                    } else {
                        stringResource(R.string.no_custom_ringtone)
                    },
                    fontSize = 11.sp,
                    color = RpgTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        ringtoneLauncher.launch("audio/mpeg")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RpgButtonDark)
                ) {
                    Text("Select MP3 Ringtone File", fontSize = 13.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 7. RESET PROGRESS BUTTON ---
        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RpgRuby)
        ) {
            Text(
                text = "⚠️ " + stringResource(R.string.settings_reset_progress),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RuleBulletItem(text: String, color: Color) {
    Text(
        text = "• $text",
        fontSize = 13.sp,
        color = color,
        lineHeight = 18.sp
    )
}

@Composable
fun TimeSlotField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = RpgTextSecondary,
            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RpgSlotSurface,
                unfocusedContainerColor = RpgSlotSurface,
                focusedBorderColor = RpgGold,
                unfocusedBorderColor = RpgBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
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
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
        val sizeBytes = pfd?.statSize ?: 0
        pfd?.close()

        if (sizeBytes > 5 * 1024 * 1024) {
            return false // Max 5MB
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
