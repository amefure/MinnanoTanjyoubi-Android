package com.amefure.minnanotanjyoubi.Domain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_ID
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_MESSAGE
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_TIME
import com.amefure.minnanotanjyoubi.R
import java.util.Calendar
import java.util.TimeZone

class NotificationRequestManager(private var context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeZone = TimeZone.getTimeZone("Asia/Tokyo")

    // チャンネルID
    private val CHANNEL_ID = "BIRTHDAY_NOTIFY"
    private val CHANNEL_NAME = "誕生日お知らせ通知"
    private val CHANNEL_DESC = "登録した友達のお誕生日を通知としてお知らせするためのチャンネルです。"

    /** 通知チャンネルを作成 */
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

    /** 通知リクエストを送信 */
    public fun setBroadcast(id: Int, month: Int, day: Int, hour: Int, minutes: Int, msg: String) {
        val c = Calendar.getInstance(timeZone)
        var year = c.get(Calendar.YEAR)

        // 登録された日付が今日より過去なら登録時に通知が発行されてしまうので来年からにする
        val nowMonth = c.get(Calendar.MONTH) + 1
        val nowDay = c.get(Calendar.DAY_OF_MONTH)
        val nowHour = c.get(Calendar.HOUR_OF_DAY)
        val nowMinute= c.get(Calendar.MINUTE)

        // 以下の条件を満たすなら翌年にする
        if (month < nowMonth) {
            // 今月以前
            year += 1
        } else if (nowMonth == month && day < nowDay) {
            // 今月の日付が今日より古い
            year += 1
        } else if (nowMonth == month && day == nowDay && hour == nowHour && minutes == nowMinute ) {
            // 現在時刻と同じ
            year += 1
        }
        c.timeInMillis = 0
        c.set(Calendar.YEAR, year)                 // 当年or来年の年を設定
        c.set(Calendar.MONTH, month - 1)           // 誕生月を設定
        c.set(Calendar.DAY_OF_MONTH, day)          // 誕生月日を設定
        c.set(Calendar.HOUR_OF_DAY, hour)          // 設定時間を設定
        c.set(Calendar.MINUTE, minutes)            // 設定分数を設定
        c.set(Calendar.SECOND, 0)                  // 固定値の秒数を設定(正確ではないため無意味)
        val triggerTime = c.timeInMillis           // 指定した日時のミリ秒表現を取得

        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        notificationIntent.setType("NotifyIntent：" + id.toString())
        notificationIntent.putExtra(INTENT_KEY_NOTIFY_MESSAGE, msg)
        notificationIntent.putExtra(INTENT_KEY_NOTIFY_ID, id)
        notificationIntent.putExtra(INTENT_KEY_NOTIFY_TIME, triggerTime)
        val pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        Log.d("通知送信日", "${year}/${month} ${hour}：${minutes}")

        // 正確な時間で通知をセット
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        // リピートさせると閏年の考慮ができないので通知到着時に翌年の通知をセットする
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            triggerTime,
//            AlarmManager.INTERVAL_DAY * 365,
//            pendingIntent
//        )
    }

    public fun deleteBroadcast(id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        notificationIntent.setType("NotifyIntent：" + id.toString())
        val pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent)
    }


    /**
     * 通知を送信(レシーバーから呼ばれる)
     * 通知を発火後に来年の通知をセットする
     */
    @SuppressLint("MissingPermission")
    public fun sendNotificationRequest(msg: String, notifyId: Int, notifyTime: Long) {
        val notificationIntent = Intent(context, ReceivedActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notifyId, notificationIntent,  PendingIntent.FLAG_IMMUTABLE)
        val res = context.resources
        // 通知オブジェクトの作成
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify_icon)
            .setColor(res.getColor(R.color.thema_gray_dark))
            .setContentTitle(res.getString(R.string.app_name))
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 通知の発行
        with(NotificationManagerCompat.from(context)) {
            notify(notifyId, builder.build())
        }

        // 来年の通知をセットし直す
        val c = Calendar.getInstance(timeZone)
        c.timeInMillis = notifyTime
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute= c.get(Calendar.MINUTE)
        setBroadcast(notifyId, month, day, hour, minute, msg)
    }
}