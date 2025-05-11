package com.amefure.minnanotanjyoubi.View.Activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.BuildConfig
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Domain.NotificationRequestManager
import com.amefure.minnanotanjyoubi.Model.Capacity
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.Model.Relation
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.View.Adapter.PersonGridLayoutAdapter
import com.amefure.minnanotanjyoubi.View.Fragment.DetailPersonFragment
import com.amefure.minnanotanjyoubi.View.Fragment.InputPersonFragment
import com.amefure.minnanotanjyoubi.View.Fragment.SettingFragment
import com.amefure.minnanotanjyoubi.ViewModel.MainViewModel
import com.amefure.minnanotanjyoubi.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var notificationRequestManager: NotificationRequestManager
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var adapter: PersonGridLayoutAdapter

    private val calcPersonInfoManager = CalcDateInfoManager()

    private var limitCapacity: Int = Capacity.initialCapacity
    private var isUnlockStorage: Boolean = false
    private var isFilter = false
    private var isSelectMode = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // AdMob 初期化
        MobileAds.initialize(this)

        // ローカルデータ管理クラス
        dataStoreManager = DataStoreManager(this)
        observeLocalData()

        // バナーの追加と読み込み
        addAdBannerView()


        // 誕生日情報を全て取得
        viewModel.fetchAllPerson()

        // 通知許可ダイアログを表示(Android13以降から許可要求が必要のため)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 誕生日情報を観測
        observedPersonData()

        binding.registerButton.setOnClickListener {
            // 容量解放ずみ または 容量上限に達していないなら
            if (isUnlockStorage || limitCapacity > viewModel.personList.value!!.count()) {
                supportFragmentManager.beginTransaction().apply {
                    add(R.id.main_frame, InputPersonFragment())
                    addToBackStack(null)
                    commit()
                }
            } else {
                AlertDialog.Builder(this)
                    .setTitle("保存容量が上限に達しました...")
                    .setMessage("設定から広告を視聴すると\n保存容量を増やすことができます。")
                    .setPositiveButton("OK", { _, _ -> })
                    .show()
            }
        }

        notificationRequestManager = NotificationRequestManager(this)

        // チャンネルの生成
        notificationRequestManager.createNotificationChannel()


        binding.filterButton.setOnClickListener {
            if (isFilter) {
                viewModel.fetchAllPerson()
                isFilter = false
                binding.filterButton.imageTintList = ContextCompat.getColorStateList(this,R.color.white)
            } else {
                val list = mutableListOf<String>()
                Relation.entries.forEach {
                    list += it.value()
                }
                AlertDialog
                    .Builder(this)
                    .setTitle("フィルターカテゴリ")
                    .setSingleChoiceItems(list.toTypedArray(), 0, { dialog, which ->
                        viewModel.fetchFilterPerson(Relation.getRelation(which.toString()))
                        binding.filterButton.imageTintList = ContextCompat.getColorStateList(this,R.color.thema_red)
                        isFilter = true
                        dialog.dismiss()
                    })
                    .show()
            }
        }


        binding.deleteButton.setOnClickListener {
            if (isSelectMode) {

                val idSet = adapter.getSelectedPersonIds()

                if (idSet.isEmpty()) {
                    adapter.inactiveSelectMode()
                    isSelectMode = false
                } else {
                    AlertDialog.Builder(this)
                        .setMessage("選択された人物を削除しますか？")
                        .setPositiveButton("OK", { _, _ ->
                            // 削除
                            for (id in idSet) {
                                viewModel.deletePerson(id)
                            }
                            adapter.inactiveSelectMode()
                            isSelectMode = false
                        })
                        .setNegativeButton("キャンセル", { dialog, _ ->
                            dialog.dismiss()
                            adapter.inactiveSelectMode()
                            isSelectMode = false
                        }).show()

                }
            } else {
                adapter.activeSelectMode()
                isSelectMode = true
            }
        }


        binding.settingButton.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, SettingFragment())
                addToBackStack(null)
                commit()
            }
        }
    }

    /** RoomDBの観測とリサイクルビューへの紐付け */
    private fun observedPersonData() {
        binding.mainList.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        viewModel.personList.observe(this) {
            adapter = PersonGridLayoutAdapter(it.sortedBy { calcPersonInfoManager.daysLater(it.date) })
//            adapter = PersonGridLayoutAdapter(Person.getDemoData())
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
            binding.mainList.adapter = adapter
        }
    }

    /** ローカルデータ観測 */
    private fun observeLocalData() {
        lifecycleScope.launch {
            dataStoreManager.observeLimitCapacity().collect {
                if (it != null) {
                    // アプリ容量を取得
                    limitCapacity = it
                } else {
                    // 初期値格納
                    dataStoreManager.saveLimitCapacity(Capacity.initialCapacity)
                }
            }
        }

        lifecycleScope.launch {
            // 容量解放購入済みかどうか
            dataStoreManager.observeInAppUnlockStorage().collect {
                isUnlockStorage = it
            }
        }
    }

    /** 許可申請ランチャー */
    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // ダイアログの結果で処理を分岐
//            if (result) {
//                Toast.makeText(this, "許可されました", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                Toast.makeText(this, "否認されました", Toast.LENGTH_SHORT)
//                    .show()
//            }
        }

    /**
     *  AdMob バナー広告の追加と読み込み
     *  adUnitIdを動的に変更するためにはViewをコードで追加する必要がある
     */
    private fun addAdBannerView() {
        lifecycleScope.launch {
            // 広告削除購入済みなら追加しない
            dataStoreManager.observeInAppRemoveAds().collect {
                if (!it) {
                    // 全てのビューをリセット
                    // アプリ初回インストール時のみ、通知許可ダイアログがらみ?で2回呼ばれるため
                    binding.adViewLayout.removeAllViewsInLayout()

                    // AdViewを生成して設定
                    val adView = AdView(this@MainActivity).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = if (BuildConfig.DEBUG) {
                            BuildConfig.ADMOB_BANNER_ID_TEST
                        } else {
                            BuildConfig.ADMOB_BANNER_ID_PROD
                        }
                        // 広告の読み込み
                        loadAd(AdRequest.Builder().build())
                    }

                    // レイアウトパラメータを指定（横幅 match_parent、高さ wrap_content）
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    adView.layoutParams = layoutParams
                    // adViewLayout に AdView を追加
                    binding.adViewLayout.addView(adView)
                } else {
                    // 広告削除フラグがONなら既に表示している場合もあるので削除
                    binding.adViewLayout.removeAllViewsInLayout()
                }
            }
        }
    }

    override fun onDestroy() {
        binding.mainList.adapter = null
        super.onDestroy()
    }
}

