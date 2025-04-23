package com.chinjan.couplejoy

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.edit
import com.chinjan.couplejoy.utils.CoupleWidgetHelper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CoupleWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Firebase.firestore.clearPersistence()

        val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit { putString("last_displayed_message", "Your partner's message") }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        CoupleWidgetHelper.scheduleWidgetRefresh(context) // 🔁 Keep the refresh cycle going
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action) {

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)

            onUpdate(context, appWidgetManager, appWidgetIds)

            // 🔁 Re-schedule again after 15s
            CoupleWidgetHelper.scheduleWidgetRefresh(context)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val prefs = context.getSharedPreferences("CoupleWidgetPrefs", Context.MODE_PRIVATE)
        val coupleId = prefs.getString("couple_id", "") ?: ""
        val partnerRole = prefs.getString("partner_role", "") ?: ""

        val views = RemoteViews(context.packageName, R.layout.couple_widget)

        // ✅ Create intent to launch MainActivity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Set tap on entire widget to open app
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // 🔁 Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)

        if (coupleId.isNotEmpty() && partnerRole.isNotEmpty()) {
            CoupleWidgetHelper.updateWidgetContent(context)
        }
    }
}