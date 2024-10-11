package com.airhomestays.app.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ItemLanguageBinding
import com.airhomestays.app.vo.Languages

class DialogAdapter(val items : ArrayList<Languages>, val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<LanguagesViewHolder>() {

    var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguagesViewHolder {
        return LanguagesViewHolder(ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LanguagesViewHolder, position: Int) =  holder.bind(items[position], position, selectedPosition, this, items)

    override fun getItemCount(): Int =  items.size
}

class LanguagesViewHolder(itemBinding: ItemLanguageBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemBinding.root) {

    val heading: TextView = TextView(itemBinding.root.context)
    val radioButton = itemBinding.radiobtnLanguages
    val root = itemBinding.rlRoot

    fun bind(item: Languages, position: Int, selectedPosition: Int, dialogAdapter: DialogAdapter, items: ArrayList<Languages>) {
        root.tag = position.toString()
        heading.text = item.languagesText
        if (item.isChecked) {
            radioButton.isChecked = item.isChecked
        } else {
            radioButton.isChecked = false
        }
        root.setOnClickListener {
            var selectedPosition = -1
            items.forEachIndexed{ index, languages ->
                if (index == position) {
                    languages.isChecked = true
                } else {
                    languages.isChecked = false
                }
            }
            dialogAdapter.notifyDataSetChanged()
        }
    }
    }