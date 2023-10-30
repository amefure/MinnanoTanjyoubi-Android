package com.amefure.minnanotanjyoubi.View.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Domain.NotificationRequestManager
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.Model.Relation
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.View.Adapter.PersonGridLayoutAdapter
import com.amefure.minnanotanjyoubi.View.Fragment.DetailPersonFragment
import com.amefure.minnanotanjyoubi.View.Fragment.InputPersonFragment
import com.amefure.minnanotanjyoubi.View.Fragment.SettingFragment
import com.amefure.minnanotanjyoubi.ViewModel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var notificationRequestManager: NotificationRequestManager

    private lateinit var adapter: PersonGridLayoutAdapter
    private lateinit var recyclerView: RecyclerView

    private val calcPersonInfoManager = CalcDateInfoManager()

    private var isFilter = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val registerButton: ImageButton = findViewById(R.id.register_button)
        val fillterButton: ImageButton = findViewById(R.id.filter_button)
        val deleteButton: ImageButton = findViewById(R.id.delete_button)
        val settingButton: ImageButton = findViewById(R.id.setting_button)

        viewModel.fetchAllPerson()

        // 許可ダイアログを表示
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)

        recyclerView = findViewById(R.id.main_list)
        observedPersonData()

        registerButton.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, InputPersonFragment())
                addToBackStack(null)
                commit()
            }
        }

        notificationRequestManager = NotificationRequestManager(this)

        // チャンネルの生成
        notificationRequestManager.createNotificationChannel()


        fillterButton.setOnClickListener {
            if (isFilter) {
                viewModel.fetchAllPerson()
                isFilter = false
                fillterButton.imageTintList = ContextCompat.getColorStateList(this,R.color.white)
            } else {
                var list = mutableListOf<String>()
                Relation.values().forEach {
                    list += it.value()
                }
                AlertDialog
                    .Builder(this)
                    .setTitle("フィルターカテゴリ")
                    .setSingleChoiceItems(list.toTypedArray(), 0, { dialog, which ->
                        viewModel.fetchFilterPerson(Relation.getRelation(which.toString()))
                        fillterButton.imageTintList = ContextCompat.getColorStateList(this,R.color.thema_red)
                        isFilter = true
                        dialog.dismiss()
                    })
                    .show()
            }
        }


        deleteButton.setOnClickListener {
            //  通知発行用のブロードキャストをセット
        }


        settingButton.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, SettingFragment())
                addToBackStack(null)
                commit()
            }
        }
    }

    // DBの観測とリサイクルビューへの紐付け
    private fun observedPersonData() {
        recyclerView.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        viewModel.personList.observe(this) {
            adapter = PersonGridLayoutAdapter(it.sortedBy { calcPersonInfoManager.daysLater(it.date) })
            adapter.setOnBookCellClickListener(
                object : PersonGridLayoutAdapter.OnBookCellClickListener {
                    override fun onItemClick(person: Person) {

                        val fragment = DetailPersonFragment.newInstance(
                            id = person.id,
                            name = person.name,
                            ruby = person.ruby,
                            date = person.date,
                            relation = person.relation,
                            notify = person.alert,
                            memo = person.memo
                        )
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_frame, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            )
//            adapter = PersonGridLayoutAdapter(Person.getDemoData())
            recyclerView.adapter = adapter
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

