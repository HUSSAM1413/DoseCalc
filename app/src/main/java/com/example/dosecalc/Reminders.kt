package com.example.dosecalc

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/** Schedules "next dose" reminders and remembers them so they survive a reboot. */
object Reminders {
    const val CHANNEL = "dose_reminders"
    private const val PREFS = "reminders"

    fun ensureChannel(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    c.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun pending(c: Context, id: String, title: String, text: String): PendingIntent {
        val i = Intent(c, ReminderReceiver::class.java)
            .putExtra("id", id)
            .putExtra("title", title)
            .putExtra("text", text)
        return PendingIntent.getBroadcast(
            c, id.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canScheduleExact(c: Context): Boolean {
        val am = c.getSystemService(AlarmManager::class.java)
        return android.os.Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
    }

    private fun setAlarm(c: Context, id: String, at: Long, title: String, text: String) {
        val am = c.getSystemService(AlarmManager::class.java)
        val pi = pending(c, id, title, text)
        if (canScheduleExact(c)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    fun schedule(c: Context, id: String, at: Long, title: String, text: String) {
        if (at <= System.currentTimeMillis()) {
            cancel(c, id)
            return
        }
        prefs(c).edit().putString(id, "$at\n$title\n$text").apply()
        setAlarm(c, id, at, title, text)
    }

    fun cancel(c: Context, id: String) {
        prefs(c).edit().remove(id).apply()
        c.getSystemService(AlarmManager::class.java).cancel(pending(c, id, "", ""))
    }

    fun cancelAll(c: Context) {
        prefs(c).all.keys.toList().forEach { cancel(c, it) }
    }

    fun forget(c: Context, id: String) {
        prefs(c).edit().remove(id).apply()
    }

    /** Called after a reboot: alarms are lost, so set them again from saved data. */
    fun rescheduleAll(c: Context) {
        val now = System.currentTimeMillis()
        for ((id, v) in prefs(c).all) {
            val parts = (v as? String)?.split("\n", limit = 3) ?: continue
            if (parts.size < 3) continue
            val at = parts[0].toLongOrNull() ?: continue
            setAlarm(c, id, maxOf(at, now + 5000), parts[1], parts[2])
        }
    }
}
