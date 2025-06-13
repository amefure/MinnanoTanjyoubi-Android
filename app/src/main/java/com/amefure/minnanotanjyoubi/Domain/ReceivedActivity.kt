package com.amefure.minnanotanjyoubi.Domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_ID
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_MESSAGE
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_TIME

/** ブロードキャストを受け取り処理を実行するクラス */
class ReceivedActivity : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val msg = intent.getStringExtra(INTENT_KEY_NOTIFY_MESSAGE) ?: return
        val notifyId = intent.getIntExtra(INTENT_KEY_NOTIFY_ID, 0)
        val notifyTime = intent.getLongExtra(INTENT_KEY_NOTIFY_TIME, 0)
        val notificationRequestManager = NotificationRequestManager(context)
        notificationRequestManager.sendNotificationRequest(msg, notifyId, notifyTime)
    }
}
