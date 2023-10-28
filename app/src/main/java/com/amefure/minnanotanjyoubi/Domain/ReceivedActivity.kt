package com.amefure.minnanotanjyoubi.Domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_ID
import com.amefure.minnanotanjyoubi.Model.Keys.INTENT_KEY_NOTIFY_MESSAGE

// ブロードキャストを受け取り処理を実行するクラス
class ReceivedActivity : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val msg = intent.getStringExtra(INTENT_KEY_NOTIFY_MESSAGE)
        val notifyId = intent.getIntExtra(INTENT_KEY_NOTIFY_ID,0)
        Log.e("~~~~~~~~~~~~~~~",msg.toString())
        Log.e("~~~~~~~~~~~~~~~", notifyId.toString())
        val notificationRequestManager = NotificationRequestManager(context)
        notificationRequestManager.sendNotificationRequest(msg!!,notifyId)
    }
}