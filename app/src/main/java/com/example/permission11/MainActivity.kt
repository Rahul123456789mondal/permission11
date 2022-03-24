package com.example.permission11

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.permission11.databinding.ActivityMainBinding
import com.vladan.networkchecker.InternetManager
import com.vladan.networkchecker.NetworkLiveData




class MainActivity : AppCompatActivity() {

    private lateinit var activityBinding : ActivityMainBinding
    private lateinit var connectionHelper: NetworkStatusHelper
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    var internetManager: InternetManager? = null
    private var isConnected = false
    private val networkLiveData = NetworkLiveData.get()

    private var isReadPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isRecordPermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isRecordPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: isRecordPermissionGranted
        }


        connectionHelper = NetworkStatusHelper(applicationContext)
        connectionHelper.observe(this) {
            val isInternetAvailable = it
            if(isInternetAvailable){
                activityBinding.textV.text = "Connected to Internet"
                activityBinding.textV.setTextColor(Color.BLACK)
            }else{
                activityBinding.textV.text = "No Internet Connection"
                activityBinding.textV.setTextColor(Color.RED)
            }
        }

       /* // Using The library
        internetManager = InternetManager.getInternetManager(this);
        internetManager!!.registerInternetMonitor()

        networkLiveData.observe(this@MainActivity) { networkState ->
            isConnected = networkState.isConnected
        }

        if (isConnected){
            activityBinding.textV.text = "Connected to Internet"
            activityBinding.textV.setTextColor(Color.BLACK)
        }else{
            activityBinding.textV.text = "No Internet Connection"
            activityBinding.textV.setTextColor(Color.RED)
        }*/



        requestPermission()
    }

    // Request Permission
    private fun requestPermission(){

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isRecordPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest : MutableList<String> = ArrayList()

        if (!isReadPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!isLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isRecordPermissionGranted){
            permissionRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }


    //activityBinding.textV.text = isOnline(this).toString()
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }


}