package com.example.kilam.inscadeapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class AdapterResult
    : RecyclerView.Adapter<AdapterResult.viewHolder>() {
    var listResult = mutableListOf<Result>()

    override fun onBindViewHolder(viewHolder: viewHolder, position: Int) {
        val resultViewHolder= viewHolder
        resultViewHolder.bindView(listResult[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = viewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false))

    override fun getItemCount() = listResult.size

    fun addItem(model : Result){
        listResult.add(model)
        notifyDataSetChanged()
    }

    fun removeAll(){
        listResult.clear()
        notifyDataSetChanged()
    }

    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private var tv_minute = itemView.findViewById<TextView>(R.id.tv_timer_minute)
//        private var tv_second =itemView.findViewById<TextView>(R.id.tv_timer_second)
        private var tv_result = itemView.findViewById<TextView>(R.id.tv_result)

        fun bindView(resultModel: Result){
//            tv_minute.text = resultModel.minute
//            tv_second.text = resultModel.second
            tv_result.text = resultModel.predict
        }
    }
}