package com.example.timednotification
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BroadcastReceiver: BroadcastReceiver() {
    private var idNotification = 0
    private val stackNotif = ArrayList<NotificationItems>()

    companion object{
        private const val CHANNEL_NAME = "my channel"
        private const val GROUP_KEY = "group key"
        private const val REQUEST_CODE = 200
        private const val MAX_STACK = 2
        private const val TIME_FORMAT = "HH:mm"
        private const val EXTRA_MESSAGE = "extra_message"
        private const val EXTRA_REPEAT = 3*60*1000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        showNotification(context, message)
    }

    fun setNotify(context: Context, time: String, message: String) {
        if (isDateInvalid(time, TIME_FORMAT)) return

        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BroadcastReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)

        val timeArray = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, EXTRA_REPEAT, pendingIntent)
    }

    private fun showNotification(context: Context, message: String){
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(context,0,intent,0)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val largeIcon = BitmapFactory.decodeResource(Resources.getSystem() ,R.drawable.ic_launcher_foreground)
        val vibrationPattern = longArrayOf(1000, 500, 0, 0, 500)
        val mBuilder: NotificationCompat.Builder

        //Melakukan pengecekan jika idNotification lebih kecil dari Max Notif
        val channelId = "channel_01"
        if (idNotification < MAX_STACK){
            mBuilder = NotificationCompat.Builder(context, channelId)
                .setContentTitle("This is " + stackNotif[idNotification].id.toString()+" Notification")
                .setContentText(stackNotif[idNotification].message)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setLargeIcon(largeIcon)
                .setGroup(GROUP_KEY)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(vibrationPattern)
        } else{
            val inboxStyle = NotificationCompat.InboxStyle()
                .addLine("This is " + stackNotif[idNotification].id.toString()+" Notification")
                .addLine("This is " + stackNotif[idNotification-1].id.toString()+" Notification")
                .setBigContentTitle("$idNotification new notification")
                .setSummaryText("Stack Notification")

            mBuilder = NotificationCompat.Builder(context, channelId)
                .setContentTitle("$idNotification new notification")
                .setContentText("StackNotification")
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle)
                .setAutoCancel(true)
        }

        /*
    Untuk android Oreo ke atas perlu menambahkan notification channel
    Materi ini akan dibahas lebih lanjut di modul extended
     */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channel = NotificationChannel(channelId,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)
            mBuilder.setChannelId(channelId)
            mNotificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = mBuilder.build()
        mNotificationManager.notify(idNotification, notificationBuilder)
    }

    private fun isDateInvalid(date: String, format: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(date)
            false
        } catch (e: ParseException) {
            true
        }
    }

    fun cancelNotification(context: Context){
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.timednotification.BroadcastReceiver::class.java)
        val requestCode = REQUEST_CODE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        pendingIntent.cancel()

        mAlarmManager.cancel(pendingIntent)
    }
}