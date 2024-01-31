package com.msl.myexoplayer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNetworkAvailable()) {
            showNoInternetSnackbar()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun showNoInternetSnackbar() {
        val view = findViewById<View>(android.R.id.content)
        Snackbar.make(view, "Oops! No internet connection.", Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                // Optionally, retry network check or actions here
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkAvailable()) {
            showNoInternetSnackbar()
        }
    }
}