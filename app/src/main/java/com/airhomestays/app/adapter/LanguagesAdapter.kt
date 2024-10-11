package com.airhomestays.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.airhomestays.app.UserPreferredLanguagesQuery
import com.airhomestays.app.databinding.ItemLanguageBinding
import com.airhomestays.app.util.onClick

class LanguagesAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<LanguagesAdapter.ViewHolder>() {

    var items = emptyList<UserPreferredLanguagesQuery.Result?>()
    var selectedItem = String()
    var languageName = String()
    var checkedPosition = -1

    fun setData(data: List<UserPreferredLanguagesQuery.Result?>) {
        items = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLanguageBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: LanguagesAdapter.ViewHolder, position: Int) = holder.bind(items[position]!!, position)

    inner class ViewHolder(val binding: ItemLanguageBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserPreferredLanguagesQuery.Result, position: Int) {
            try {
                with(binding) {
                    text = item.label
                    if (selectedItem == item.value) {
                        radiobtnLanguages.isChecked = true
                        checkedPosition = position
                        languageName = item.label!!
                    } else {
                        radiobtnLanguages.isChecked = false
                    }
                    radiobtnLanguages.onClick {
                        selectedItem = item.value!!
                        languageName = item.label!!
                        notifyItemChanged(checkedPosition)
                        checkedPosition = position
                    }
                    executePendingBindings()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

}
