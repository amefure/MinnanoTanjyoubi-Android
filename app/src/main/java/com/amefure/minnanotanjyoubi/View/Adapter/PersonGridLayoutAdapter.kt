package com.amefure.minnanotanjyoubi.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.R

class PersonGridLayoutAdapter (personList: List<Person>) :RecyclerView.Adapter<PersonGridLayoutAdapter.MainViewHolder>() {

    private val _personList: MutableList<Person> = personList.toMutableList()
    override fun getItemCount(): Int = _personList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            // XMLレイアウトファイルからViewオブジェクトを作成
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_person_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val user = _personList[position]

        holder.name.text = user.name
        holder.date.text = user.date
        holder.age.text = user.name     // TODO: 計算
        holder.limitDay.text = user.name// TODO: 計算
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.person_name)
        val date: TextView = itemView.findViewById(R.id.person_date)
        val age: TextView = itemView.findViewById(R.id.person_age)
        val limitDay: TextView = itemView.findViewById(R.id.person_limit_day)
    }
}
