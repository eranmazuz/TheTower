package com.example.thetower.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.thetower.MainActivity
import com.example.thetower.data.DefaultDataRepository
import com.example.thetower.data.engine.SlotTransitionEngine
import com.example.thetower.data.model.GameState
import com.example.thetower.service.AlarmFullScreenActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class SlotAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repository = DefaultDataRepository.getInstance(context)
        
        // 1. Run the transition engine synchronously to evaluate missed slots
        val updatedState = repository.updateState { currentState ->
            SlotTransitionEngine.evaluateTransitions(currentState, LocalDateTime.now())
        }

        // 2. Schedule the next alarm
        scheduleNextAlarm(context, updatedState)

        // 3. Trigger notification or full-screen alarm activity
        showTransitionAlert(context, updatedState)
    }

    private fun showTransitionAlert(context: Context, state: GameState) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "slot_transition_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Time Slot Transitions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a new habit slot begins"
                enableVibration(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activeSlot = state.lastProcessedSlot ?: "MORNING"
        val activeDate = state.lastProcessedDate ?: LocalDate.now().toString()
        val localDate = LocalDate.parse(activeDate)
        val dayOfWeek = localDate.dayOfWeek.value

        val activeQuestsCount = state.questDefinitions.count {
            it.slot == activeSlot && it.activeDays.contains(dayOfWeek)
        }

        // Title and description strings localized at runtime
        val titleText = when (activeSlot) {
            "MORNING" -> "Morning Quest Slot Started"
            "NOON" -> "Noon Quest Slot Started"
            "EVENING" -> "Evening Quest Slot Started"
            "NIGHT" -> "Night Quest Slot Started"
            else -> "New Quest Slot Started"
        }
        val descText = "$activeQuestsCount active quests for this slot. Complete them before the next deadline!"

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (state.alarmModeActive) {
            // Alarm Mode: full-screen intent activity over lock screen
            val alarmIntent = Intent(context, AlarmFullScreenActivity::class.java).apply {
                putExtra("SLOT_NAME", activeSlot)
                putExtra("QUEST_COUNT", activeQuestsCount)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
            val alarmPendingIntent = PendingIntent.getActivity(
                context,
                1,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(titleText)
                .setContentText(descText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(alarmPendingIntent, true)
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } else {
            // Standard Notification mode
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(titleText)
                .setContentText(descText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun scheduleNextAlarm(context: Context, state: GameState) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Get current slot based on time
            val now = LocalDateTime.now()
            val (currentSlot, currentDateStr) = SlotTransitionEngine.getActiveSlotAndDate(now, state.alarmTimes)
            
            // The alarm triggers at the transition to the NEXT slot
            val (nextSlot, nextDateStr) = SlotTransitionEngine.getNextSlotAndDate(currentSlot, currentDateStr)
            val nextTimeStr = state.alarmTimes[nextSlot] ?: when (nextSlot) {
                "MORNING" -> "09:00"
                "NOON" -> "12:00"
                "EVENING" -> "18:00"
                "NIGHT" -> "21:00"
                else -> "09:00"
            }
            
            val nextDate = LocalDate.parse(nextDateStr)
            val nextTime = LocalTime.parse(nextTimeStr)
            val nextDateTime = LocalDateTime.of(nextDate, nextTime)

            val triggerMs = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val intent = Intent(context, SlotAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel any old alarms
            alarmManager.cancel(pendingIntent)

            // Set new alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMs, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
        }
    }
}
