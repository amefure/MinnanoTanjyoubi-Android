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
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_MESSAGE
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
    public fun setBroadcast(id: Int, month: Int, day: Int, hour: Int, minutes: Int, msg: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        notificationIntent.setType("NotifyIntent：" + id.toString())
        notificationIntent.putExtra(INTENT_KEY_NOTIFY_MESSAGE, msg)
        notificationIntent.putExtra(INTENT_KEY_NOTIFY_ID, id)
        val pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        val c = Calendar.getInstance(timeZone)
        c.timeInMillis = 0
        c.set(Calendar.YEAR, c.get(Calendar.YEAR)) // 当年の年を設定
        c.set(Calendar.MONTH, month - 1)           // 誕生月を設定
        c.set(Calendar.DAY_OF_MONTH, day)          // 誕生月日を設定
        c.set(Calendar.HOUR_OF_DAY, hour)          // 設定時間を設定
        c.set(Calendar.MINUTE, minutes)            // 設定分数を設定
        c.set(Calendar.SECOND, 0)                  // 固定値の秒数を設定(正確ではないため無意味)
        val triggerTime = c.timeInMillis           // 指定した日時のミリ秒表現を取得

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY * 365,
            pendingIntent
        )
    }

    public fun deleteBroadcast(id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        notificationIntent.setType("NotifyIntent：" + id.toString())
        val pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent)
    }

    // 通知を送信(レシーバーから呼ばれる)
    public fun sendNotificationRequest(msg: String, notifyId: Int) {
        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notifyId, notificationIntent,  PendingIntent.FLAG_IMMUTABLE)
        val res = context.resources
        // 通知オブジェクトの作成
        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(res.getString(R.string.app_name))
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 通知の発行
        with(NotificationManagerCompat.from(context)) {
            notify(notifyId, builder.build())
        }
    }
}