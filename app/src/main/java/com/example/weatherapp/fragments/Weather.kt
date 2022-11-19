package com.example.weatherapp.fragments

data class Weather(
    val city: String,
    val last_upd: String,
    val icon: String,
    val curTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
)
