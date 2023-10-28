package com.amefure.minnanotanjyoubi.View.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import com.amefure.minnanotanjyoubi.Domain.CalcPersonInfoManager
import com.amefure.minnanotanjyoubi.R

private const val ARG_ID_KEY = "ARG_ID_KEY"
private const val ARG_NAME_KEY = "ARG_NAME_KEY"
private const val ARG_RUBY_KEY = "ARG_RUBY_KEY"
private const val ARG_DATE_KEY = "ARG_DATE_KEY"
private const val ARG_RELATION_KEY = "ARG_RELATION_KEY"
private const val ARG_NOTIFY_KEY = "ARG_NOTIFY_KEY"
private const val ARG_MEMO_KEY = "ARG_MEMO_KEY"

class DetailPersonFragment : Fragment() {

    private var id: Int = 0
    private var name: String = ""
    private var ruby: String = ""
    private var date: String = ""
    private var relation: String = ""
    private var notify: Boolean = false
    private var memo:String = ""

    private val calcPersonInfoManager = CalcPersonInfoManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            id = it.getInt(ARG_ID_KEY,0)
            name = it.getString(ARG_NAME_KEY,"")
            ruby = it.getString(ARG_RUBY_KEY,"")
            date = it.getString(ARG_DATE_KEY,"2023/10/1")
            relation = it.getString(ARG_RELATION_KEY,"2023/10/1")
            notify = it.getBoolean(ARG_NOTIFY_KEY,true)
            memo = it.getString(ARG_MEMO_KEY,"")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)

        val relationLabel: TextView = view.findViewById(R.id.relation_text)
        val daysLaterLabel: TextView = view.findViewById(R.id.days_later_text)
        val nameLabel: TextView = view.findViewById(R.id.name_text)
        val rubyLabel: TextView = view.findViewById(R.id.ruby_text)
        val dateLabel: TextView = view.findViewById(R.id.date_text)
        val ageLabel: TextView = view.findViewById(R.id.age_text)
        val signOgZodiacLabel: TextView = view.findViewById(R.id.sign_of_zodiac_text)
        val zodiacLabel: TextView = view.findViewById(R.id.zodiac_text)
        val memoLabel: TextView = view.findViewById(R.id.memo_text)
        val notifySwitch: Switch = view.findViewById(R.id.notify_edit_button)

        // 戻るボタン
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        relationLabel.text = relation
        daysLaterLabel.text = "あと" + calcPersonInfoManager.daysLater(date).toString() + "日"
        nameLabel.text = name
        rubyLabel.text = ruby
        dateLabel.text = date + "（" + calcPersonInfoManager.japaneseEraName(date) + "）"
        ageLabel.text = calcPersonInfoManager.currentAge(date).toString() + "歳"
        signOgZodiacLabel.text = "水瓶ざ"
        zodiacLabel.text = "いのしし年"
        memoLabel.text = memo
        notifySwitch.isChecked = notify
    }


    companion object{
        @JvmStatic
        fun newInstance(id: Int, name: String, ruby: String, date: String, relation: String, notify: Boolean, memo: String) =
            DetailPersonFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID_KEY, id)
                    putString(ARG_NAME_KEY, name)
                    putString(ARG_RUBY_KEY, ruby)
                    putString(ARG_DATE_KEY, date)
                    putString(ARG_RELATION_KEY, relation)
                    putBoolean(ARG_NOTIFY_KEY, notify)
                    putString(ARG_MEMO_KEY,memo)
                }
            }
    }
}