package com.thrd.testlucene

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class SearchResultAdapter(var results: List<String>) : RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val textView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_result, parent, false) as TextView
        return ResultViewHolder(textView)
    }

    override fun getItemCount() = results.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.textView.text = results[position]
    }

    class ResultViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}
