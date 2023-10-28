package com.amefure.minnanotanjyoubi.View.Adapter

import android.app.Person
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


// 不要
class CardItemTouchListener: RecyclerView.SimpleOnItemTouchListener() {
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // タッチイベントの処理
        if (e.action == MotionEvent.ACTION_DOWN) {
            // タッチされた位置のViewを取得
            val childView: View? = rv.findChildViewUnder(e.x, e.y)
            if (childView != null) {
                // 要素番号を取得
                val position = rv.getChildAdapterPosition(childView)
                if (position != RecyclerView.NO_POSITION) {
                    val adapter = rv.adapter
                    if (adapter is PersonGridLayoutAdapter) {
//                        val tappedItem: Person? = adapter.getItemAtPosition(position)
//                        if (tappedItem != null) {
//                            //
//                        }
                    }
                }
            }
        }
        return false // 通常のタッチイベント処理を維持
    }
}