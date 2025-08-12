package com.amefure.minnanotanjyoubi.View.Activity

import android.app.Application
import android.os.StrictMode
import com.amefure.minnanotanjyoubi.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // スレッドポリシー
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    // メインスレッド（UIスレッド）でのディスク読み込みを検出
                    // → SQLiteクエリやファイル読み込みなど
                    .detectDiskReads()
                    // メインスレッドでのディスク書き込みを検出
                    // → ファイル保存、SharedPreferences書き込みなど
                    .detectDiskWrites()
                    // メインスレッドでのネットワークアクセスを検出
                    // → HttpURLConnection, OkHttpの同期通信など
                    .detectNetwork()
                    // 違反検出時にLogcatへ出力（警告ログ）
                    .penaltyLog()
                    // 違反検出時にアプリを即クラッシュさせる
                    // → 見逃し防止（デバッグ時のみ推奨）
                    // .penaltyDeath()
                    // ポリシーをビルドして適用
                    .build()
            )

            // VMポリシー
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    // SQLite オブジェクト（Cursorなど）のclose忘れを検出
                    .detectLeakedSqlLiteObjects()
                    // Closeable（InputStream, OutputStreamなど）のclose忘れを検出
                    .detectLeakedClosableObjects()
                    // Activity のメモリリークを検出
                    // → 破棄されるべきActivityがGCされない場合に警告
                    .detectActivityLeaks()
                    // 違反検出時にLogcatへ出力
                    .penaltyLog()
                    // ポリシーをビルドして適用
                    .build()
            )
        }
    }
}
