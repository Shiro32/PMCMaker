package com.sakuraweb.fotopota.pmcmaker.ui.menu

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.menu_one_card.view.*

class MenuViewHolder(v: View) : RecyclerView.ViewHolder(v){
    var name: TextView ?= null
    var desc: TextView ?= null

    init {
        name = v.oneMenuName
        desc = v.oneMenuDesc
    }
}