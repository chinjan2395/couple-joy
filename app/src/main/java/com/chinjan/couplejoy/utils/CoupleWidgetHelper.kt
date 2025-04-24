package com.chinjan.couplejoy.utils

import com.chinjan.couplejoy.CoupleWidgetProvider
import com.chinjan.couplejoy.R
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chinjan.couplejoy.data.FirebaseRepository
import com.chinjan.couplejoy.data.prefs.PreferenceManager

object CoupleWidgetHelper {

    fun updateWidgetContent(context: Context) {
        val prefs by lazy { PreferenceManager(context) }
        val coupleId = prefs.getCoupleId()
        val role = prefs.getRole()
        val views = RemoteViews(context.packageName, R.layout.couple_widget)

        if (coupleId != null) {
            val oppositeRole = if (role == Constants.PARTNER_A) Constants.PARTNER_B else Constants.PARTNER_A
            val repository = FirebaseRepository(context)

            Log.d("CoupleWidgetProvider", "updateWidgetContent role:$role oppositeRole:$oppositeRole ")

            repository.getMessage(coupleId, oppositeRole) { message ->
                val displayMessage = message ?: "No message yet"
                Log.d("CoupleWidgetProvider", "getMessage->message:$message")
                updateWidgetWithMessage(context, displayMessage)
            }
        } else {
            views.setTextViewText(R.id.widgetMessage, "Please set couple ID and role.")
//            appWidgetManager.updateAppWidget(componentName, views)
        }
    }

    fun scheduleWidgetRefresh(context: Context) {
//        Log.d("CoupleWidgetProvider", "scheduleWidgetRefresh")
        val intent = Intent(context, CoupleWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val permissionIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                permissionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(permissionIntent)
                return
            }
        }

        val intervalMillis = 50 * 1000L // ⏱ 15 seconds

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            intervalMillis,
            pendingIntent
        )
    }

    fun updateWidgetWithMessage(context: Context, message: String) {
        val prefs by lazy { PreferenceManager(context) }

        // 👇 Set default if not already set
        if (!prefs.hasLastDisplayedMessage()) {
            prefs.setLastDisplayedMessage(context.getString(R.string.widget_placeholder))
        }

        val previousMessage = prefs.getLastDisplayedMessage()
        val role = prefs.getRole()
        val oppositeRole = if (role == Constants.PARTNER_A) Constants.PARTNER_B else Constants.PARTNER_A

        Log.d("CoupleWidgetProvider", "updateWidgetWithMessage role $role :message $message")

        // Only update UI and notify if message is new
        Log.d("CoupleWidgetProvider", "updateWidgetWithMessage:previousMessage $previousMessage")
        if (message != previousMessage) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val views = RemoteViews(context.packageName, R.layout.couple_widget)
            val initial = oppositeRole.last().uppercaseChar().toString()
            views.setTextViewText(R.id.widgetMessage, message)
            views.setTextViewText(R.id.widgetUserInitial, initial)

            val widget = ComponentName(context, CoupleWidgetProvider::class.java)
            appWidgetManager.updateAppWidget(widget, views)

            // ✅ Show notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showNewMessageNotification(context, message)
            }

            // Save new message
            prefs.setLastDisplayedMessage(message)

        }
    }

    fun showNewMessageNotification(context: Context, message: String) {
        val channelId = "couple_widget_channel"

        // Permission check (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("CoupleWidgetProvider", "Notification permission not granted")
                return
            }
        }

        // Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Couple Widget Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows new partner messages"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Build and show the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_couple_notification) // make sure it's a valid icon
            .setContentTitle("New message from your partner")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
}