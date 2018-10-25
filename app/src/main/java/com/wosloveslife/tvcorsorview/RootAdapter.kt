package com.wosloveslife.tvcorsorview

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class RootAdapter(val data: ArrayList<String>) : RecyclerView.Adapter<RootHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RootHolder {
        return RootHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_card, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RootHolder, position: Int) {
        holder.tv.text = data[position]
    }

}

class RootHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv: TextView = itemView.findViewById(R.id.tv)
}