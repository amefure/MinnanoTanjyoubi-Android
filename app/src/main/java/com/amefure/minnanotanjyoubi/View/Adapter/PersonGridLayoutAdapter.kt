package com.amefure.minnanotanjyoubi.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.R

class PersonGridLayoutAdapter (personList: List<Person>) :RecyclerView.Adapter<PersonGridLayoutAdapter.MainViewHolder>() {

    private val _personList: MutableList<Person> = personList.toMutableList()
    override fun getItemCount(): Int = _personList.size

    private val calcPersonInfoManager = CalcDateInfoManager()


    private var isSelectableMode = false
    // 削除対象のIDを保持
    private val selectedPersonIds = mutableSetOf<Int>()

    private lateinit var listener: OnBookCellClickListener
    // 2. インターフェースを作成
    interface OnBookCellClickListener {
        fun onItemClick(person: Person)
    }

    // セレクトモード活性
    public fun activeSelectMode(){
        isSelectableMode = true
        notifyDataSetChanged()
    }

    // セレクトモード非活性
    public fun inactiveSelectMode(){
        selectedPersonIds.removeAll(selectedPersonIds)
        isSelectableMode = false
        notifyDataSetChanged()
    }

    // 3. リスナーをセット
    fun setOnBookCellClickListener(listener: OnBookCellClickListener) {
        // 定義した変数listenerに実行したい処理を引数で渡す（BookListFragmentで渡している）
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            // XMLレイアウトファイルからViewオブジェクトを作成
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_person_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val person = _personList[position]

        if (isSelectableMode) {
            holder.selectWrapper.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                if (selectedPersonIds.contains(person.id)) {
                    // 追加済みなら削除
                    selectedPersonIds.remove(person.id)
                    notifyDataSetChanged()
                } else {
                    // 追加
                    selectedPersonIds.add(person.id)
                    notifyDataSetChanged()
                }
            }
        } else {
            holder.selectWrapper.visibility = View.GONE
            holder.itemView.setOnClickListener {
                // セルがクリックされた時にインターフェースの処理が実行される
                listener.onItemClick(person)
            }
        }

        if (selectedPersonIds.contains(person.id)) {
            holder.selectButton.setImageResource(R.drawable.ic_card_check_selected)
        } else {
            holder.selectButton.setImageResource(R.drawable.ic_card_check_dis_select)
        }


        val age = calcPersonInfoManager.currentAge(person.date)
        val daysLater = calcPersonInfoManager.daysLater(person.date)
        holder.name.text = person.name
        holder.date.text = person.date
        holder.age.text = age.toString()
        holder.daysLater.text = daysLater.toString()
    }

    fun getSelectedPersonIds() : Set<Int> {
        return selectedPersonIds.toSet()
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.person_name)
        val date: TextView = itemView.findViewById(R.id.person_date)
        val age: TextView = itemView.findViewById(R.id.person_age)
        val daysLater: TextView = itemView.findViewById(R.id.person_days_later)
        val selectWrapper: ConstraintLayout = itemView.findViewById(R.id.select_wrapper)
        val selectButton: ImageView = itemView.findViewById(R.id.select_button)
    }

}
