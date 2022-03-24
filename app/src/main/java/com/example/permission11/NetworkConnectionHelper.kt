package com.example.permission11

import android.content.Context
import android.net.*
import android.os.Build
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NetworkConnectionHelper(context: Context) : LiveData<NetworkStatus>() {

    val valideNetworkConnections: ArrayList<Network> = ArrayList()
    var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    // Update LiveData NetworkStatus
    fun announceStatus() {
        if (valideNetworkConnections.isNotEmpty()) {
            postValue(NetworkStatus.Available)
        } else {
            postValue(NetworkStatus.Unavailable)
        }
    }

    // Establish a connection to system through ConnectivityManager
    private fun getConnectivityManagerCallback() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    val networkCapability = connectivityManager.getNetworkCapabilities(network)
                    val hasNetworkConnection =
                        networkCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            ?: false
                    if (hasNetworkConnection) {
                        determineInternetAccess(network)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    valideNetworkConnections.remove(network)
                    announceStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        determineInternetAccess(network)
                    } else {
                        valideNetworkConnections.remove(network)
                    }
                    announceStatus()
                }
            }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }

    private fun determineInternetAccess(network: Network) {
        CoroutineScope(Dispatchers.IO).launch {
            if (InternetAvailability.check()) {
                withContext(Dispatchers.Main) {
                    // Check if the network already exits in valideNetworkConnections.
                    // If it already exists, then don't add it to list
                    // This will avoid duplications and abnormal functionality.
                    if(!valideNetworkConnections.contains(network))
                        valideNetworkConnections.add(network)
                    announceStatus()
                }
            }
        }
    }

    override fun onActive() {
        super.onActive()
        connectivityManagerCallback = getConnectivityManagerCallback()
        val networkRequest =
            NetworkRequest
                .Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        connectivityManager.registerNetworkCallback(networkRequest, connectivityManagerCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    }

}

/*class NetworkConnectionHelper(private val connectivityManager: ConnectivityManager): LiveData<Boolean>() {

    constructor(application: Application) : this(application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    private val networkCallback= @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActive() {
        super.onActive()
        val builder=NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(),networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

}*/

/*
*
* private lateinit var networkCallback : ConnectivityManager.NetworkCallback
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkRequest(){
        val requestBuilder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        connectivityManager.registerNetworkCallback(
            requestBuilder.build(),
            connectivityManagerCallback()
        )
    }

    private fun connectivityManagerCallback() : ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    postValue(false)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    postValue(false)
                }

            }
            return networkCallback
        }else{
            throw IllegalAccessError("Error")
        }
    }

    private val networkReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            updateConnection()
        }
    }

    private fun updateConnection(){
        val activeNetwork : NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnected == true)
    }

    override fun onActive() {
        super.onActive()
        updateConnection()
        when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                lollipopNetworkRequest()
            }
            else -> {
                context.registerReceiver(
                    networkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }


        }
    }

    override fun onInactive() {
        super.onInactive()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback())
        }else{
            context.unregisterReceiver(networkReceiver)
        }
    }
* */