package com.example.weatherapp.fragments

import  android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject


class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    val model: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ):
            View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        getResult("Moscow")
        updCurCard()

        val recyclerView1: RecyclerView = binding.rvHours
        recyclerView1.layoutManager = LinearLayoutManager(context)
        recyclerView1.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val recyclerView2: RecyclerView = binding.rvWeek
        recyclerView2.layoutManager = LinearLayoutManager(context)

        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        model.liveCur.observe(viewLifecycleOwner) {
            recyclerView1.adapter = WeatherAdapter1(getHoursList(it))
        }
        model.liveList.observe(viewLifecycleOwner) {
            recyclerView2.adapter = WeatherAdapter2(it)
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding){
        imageButton.setOnClickListener {
            if (imageButton.isSelected) {
                checkLocation()
            }
        }
        imageButton2.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> getResult(it1) }
                }
            })
        }
    }

    private fun checkLocation() {
        if(isLocationEnabled()){
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun getLocation(){
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener{
                getResult("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun updCurCard() = with(binding) {
        model.liveCur.observe(viewLifecycleOwner) {
            val temp = "${it.curTemp}°C"
            textCity.text = it.city
            curTemp.text = temp
            curDate.text = it.last_upd
            Picasso.get().load("http:" + it.icon).into(curCond)
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getResult(city: String) {
        val url = "http://api.weatherapi.com/v1/forecast.json?" +
                "key=049c9fb75a5943a2a6e83841221611&q=$city&days=7&aqi=no&alerts=no"
        val inf = Volley.newRequestQueue(context)
        val ask = StringRequest(com.android.volley.Request.Method.GET, url, {
                response -> parser(response)
        },
            { error -> Log.d("My", "Error: $error") })
        inf.add(ask)
    }

    private fun parser(result: String) {
        val inf = JSONObject(result)
        val list = parserWeek(inf)
        parsercurrent(inf, list, 0)
    }

    private fun parserWeek(inf: JSONObject): List<Weather> {
        val list = ArrayList<Weather>()
        val weekArray = inf.getJSONObject("forecast").getJSONArray("forecastday")
        val name = inf.getJSONObject("location").getString("name")
        for (i in 0 until weekArray.length()) {
            val day = weekArray[i] as JSONObject
            val item = Weather(
                name, day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "", day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveList.value = list
        return list
    }

    private fun parsercurrent(inf: JSONObject, list: List<Weather>, index: Int) {
        val item = Weather(inf.getJSONObject("location").getString("name"),
            inf.getJSONObject("current").getString("last_updated"),
            inf.getJSONObject("current").getJSONObject("condition").getString("icon"),
            inf.getJSONObject("current").getString("temp_c"),
            list[index].maxTemp,  list[index].minTemp, list[index].hours)
        model.liveCur.value = item
    }

    private fun getHoursList(wItem: Weather): List<Weather> {
        val Hours = JSONArray(wItem.hours)
        Log.d("My", "$wItem")
        val list = ArrayList<Weather>()
        for ( i in 0 until Hours.length()) {
            val item = Weather(wItem.city,
                (Hours[i] as JSONObject).getString("time"),
                (Hours[i] as JSONObject).getJSONObject("condition").getString("icon"),
                (Hours[i] as JSONObject).getString("temp_c"), "", "", "")
            list.add(item)
        }
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}