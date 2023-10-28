package com.amefure.minnanotanjyoubi.Domain

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_ID
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_PERSON
import com.amefure.minnanotanjyoubi.R
import java.util.Calendar
import java.util.TimeZone

class NotificationRequestManager(private var context: Context) {

    // チャンネルID
    private val CHANNEL_ID = "BIRTHDAY_NOTIFY"
    private val CHANNEL_NAME = "誕生日お知らせ通知"
    private val CHANNEL_DESC = "登録した友達のお誕生日を通知としてお知らせするためのチャンネルです。"

    // チャンネルを作成
    public fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_DESC
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // チャンネルをシステムに登録
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 通知リクエストを送信
    public fun setBroadcast() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, ReceivedActivity::class.java)

        notificationIntent.putExtra(INTENT_KEY_PERSON, "NAME")
        notificationIntent.putExtra(INTENT_KEY_NOTIFY_ID, "ID")
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        val calendar = Calendar.getInstance(timeZone)
        calendar.timeInMillis = 0
        calendar.set(Calendar.YEAR, 2023)               // 任意の年を設定
        calendar.set(Calendar.MONTH, Calendar.OCTOBER)  // 任意の月を設定
        calendar.set(Calendar.DAY_OF_MONTH, 23)         // 任意の日を設定
        calendar.set(Calendar.HOUR_OF_DAY, 21)          // 任意の時を設定
        calendar.set(Calendar.MINUTE, 35)               // 任意の分を設定
        calendar.set(Calendar.SECOND, 0)                // 任意の秒を設定
        val triggerTime = calendar.timeInMillis         // 指定した日時のミリ秒表現を取得

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    public fun sendNotificationRequest(person: String,notifyId: Int) {
        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,  PendingIntent.FLAG_IMMUTABLE)
        val res = context.resources
        // 通知オブジェクトの作成
        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(res.getString(R.string.app_name))
            .setContentText("今日は" + person + "さんの誕生日")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 通知の発行
        with(NotificationManagerCompat.from(context)) {
            notify(notifyId, builder.build())
        }
    }
}