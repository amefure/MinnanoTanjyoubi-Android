package com.amefure.minnanotanjyoubi.Domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amefure.minnanotanjyoubi.Model.INTENT_KEY_NOTIFY_ID
import com.amefure.minnanotanjyoubi.Model.INTENT_KEY_PERSON

// ブロードキャストを受け取り処理を実行するクラス
class ReceivedActivity : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val person = intent.getStringExtra(INTENT_KEY_PERSON)
        val notifyId = intent.getIntExtra(INTENT_KEY_NOTIFY_ID,0)
        val notificationRequestManager = NotificationRequestManager(context)
        notificationRequestManager.sendNotificationRequest(person!!,notifyId)
    }
}