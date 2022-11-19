package com.example.weatherapp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.squareup.picasso.Picasso

class WeatherAdapter2(private val inf: List<Weather>) : RecyclerView.Adapter<WeatherAdapter2.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.week_date)
        val temp: TextView = itemView.findViewById(R.id.week_temp)
        val icon: ImageView = itemView.findViewById(R.id.week_im)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_week, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val temp = "${inf[position].minTemp}°C / ${inf[position].maxTemp}°C"
        val hour = inf[position].last_upd
        holder.date.text = hour
        holder.temp.text = temp
        Picasso.get().load("http:" + inf[position].icon).into(holder.icon)
    }

    override fun getItemCount() = inf.size
}

