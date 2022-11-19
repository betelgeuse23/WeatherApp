package com.example.weatherapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.fragments.Weather

class MainViewModel : ViewModel() {
    val liveCur = MutableLiveData<Weather>()
    val liveList = MutableLiveData<List<Weather>>()
}