package com.example.permission11

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.Network
import android.net.NetworkCapabilities.*
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private val TAG = "C-Manager"

class NetworkStatusHelper(private val context: Context) : LiveData<Boolean>() {

    var  intentFilter = IntentFilter(CONNECTIVITY_ACTION)
    private var  connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback : NetworkCallback = NetworkCallback(this)

    override fun onActive() {
        super.onActive()
        updateConnection()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> connectivityManager.registerDefaultNetworkCallback(networkCallback)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val builder = NetworkRequest.Builder().addTransportType(TRANSPORT_CELLULAR).addTransportType(TRANSPORT_WIFI)
                connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
            }
            else -> {
                context.registerReceiver(networkReceiver, intentFilter)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateConnection()
        }
    }

    fun updateConnection() {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnectedOrConnecting == true)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class NetworkCallback(private val liveData : NetworkStatusHelper) : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            liveData.postValue(true)
        }

        override fun onLost(network: Network) {
            liveData.postValue(false)
        }
    }
}

































/*private lateinit var networkCallback: ConnectivityManager.NetworkCallback
   private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
   private val validNetworks: MutableSet<Network> = HashSet()

   private fun checkValidNetworks() {
       postValue(validNetworks.size > 0)
   }

   override fun onActive() {
       networkCallback = createNetworkCallback()
       val networkRequest = NetworkRequest.Builder()
           .addCapability(NET_CAPABILITY_INTERNET)
           .build()
       cm.registerNetworkCallback(networkRequest, networkCallback)
   }

   override fun onInactive() {
       cm.unregisterNetworkCallback(networkCallback)
   }

   private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

       *//*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         *//*
        override fun onAvailable(network: Network) {
            Log.d(TAG, "onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Log.d(TAG, "onAvailable: ${network}, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if(hasInternet){
                        withContext(Dispatchers.Main){
                            Log.d(TAG, "onAvailable: adding network. $network")
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        *//*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         *//*
        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }

    }
*/
