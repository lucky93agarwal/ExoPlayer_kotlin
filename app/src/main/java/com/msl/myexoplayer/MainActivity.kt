package com.msl.myexoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class MainActivity : BaseActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var exoPlayerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fullscreenIcon: ImageView
    private var isFullscreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        exoPlayerView = findViewById(R.id.exoPlayerView)
        progressBar = findViewById(R.id.progress_bar)
        fullscreenIcon = findViewById(R.id.fullscreen_button)
        initializePlayer()


    }

    private fun initializePlayer() {
        // Create a SimpleExoPlayer
        player = SimpleExoPlayer.Builder(this).build()

        // Set the ExoPlayer to the PlayerView
        exoPlayerView.player = player


        val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaSource =
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(hlsUrl))

        // Add the MediaItem to the player
        player.setMediaSource(mediaSource)

        // Prepare the player
        player.prepare()
        player.play()

        // Update progress bar based on player position
        player.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(reason: Int) {
                updateProgressBar()
            }
        })

        fullscreenIcon.setOnClickListener {
            toggleFullscreen()
        }

        // Update progress bar periodically
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                if (player.isPlaying) {
                    updateProgressBar()
                }
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(runnable)

    }
    private fun updateProgressBar() {
        // Choose either current position or remaining time display:
        // progressBar.progress = player.currentPosition.toInt()
        val remainingTime = player.duration - player.currentPosition
        val progress = ((100 * remainingTime) / player.duration).toInt()
        progressBar.progress = progressBar.max - progress // Invert for left-to-right
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen

        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            updateFullscreenIcon(R.drawable.ic_fullscreen_portrait)
            exoPlayerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            updateFullscreenIcon(R.drawable.ic_fullscreen_landscape)
            exoPlayerView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
    private fun updateFullscreenIcon(resourceId: Int? = null) {
        val icon = resourceId ?: when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> R.drawable.ic_fullscreen_landscape
            Configuration.ORIENTATION_LANDSCAPE -> R.drawable.ic_fullscreen_portrait
            else -> R.drawable.ic_fullscreen_landscape // Add your default icon drawable here
        }
        fullscreenIcon.setImageResource(icon)
    }
    override fun onBackPressed() {
        if (exoPlayerView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0) {
            exoPlayerView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player.release()
    }


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            // Handle no internet scenario
            showNoInternetSnackbar()
        }
    }

    override fun onStart() {
        super.onStart()
        player.play()
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


}