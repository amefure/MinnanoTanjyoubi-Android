package com.amefure.minnanotanjyoubi.View.Activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.Domain.NotificationRequestManager
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.View.Adapter.PersonGridLayoutAdapter
import com.amefure.minnanotanjyoubi.View.Fragment.InputPersonFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 許可ダイアログを表示
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)


        // グリッドレイアウトの実装
        val recyclerView: RecyclerView = findViewById(R.id.main_list)
        recyclerView.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        recyclerView.adapter = PersonGridLayoutAdapter(Person.getDemoData())

        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, InputPersonFragment())
                addToBackStack(null)
                commit()
            }
        }


        val notificationRequestManager = NotificationRequestManager(this)

        // チャンネルの生成
        notificationRequestManager.createNotificationChannel()

        val buttonNotification: Button = findViewById(R.id.delete_button)
        buttonNotification.setOnClickListener {
            //  通知発行用のブロードキャストをセット
            notificationRequestManager.setBroadcast()
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            // ダイアログの結果で処理を分岐
            if (result) {
                Toast.makeText(this, "許可されました", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "否認されました", Toast.LENGTH_SHORT)
                    .show()
            }
        }
}

