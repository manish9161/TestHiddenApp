package com.droidtools.hiddentestapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.VolumeProviderCompat
import androidx.media.session.MediaButtonReceiver
import com.droidtools.hiddentestapp.MainActivity
import com.droidtools.hiddentestapp.R
import com.droidtools.hiddentestapp.pref.PrefsHelper


class VolumeService : Service() {

    private var mediaSessionCompat: MediaSessionCompat? = null

    var clickCounter = 0

    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(this, "VolumeService")
        mediaSessionCompat?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSessionCompat?.setPlaybackState(PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f).build())

        if(PrefsHelper.read(PrefsHelper.APP_ICON_STATUS, "") == "") {
            hideAppIcon()
        }

        val myVolumeProvider: VolumeProviderCompat = object : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, 100,  50) {
            override fun onAdjustVolume(direction: Int) {
                if(direction == -1) {
                    clickCounter++
                    if(clickCounter == 1) {
                        resetClickCounterHandler()
                    }
                    if(clickCounter == 3) {
                        if(PrefsHelper.read(PrefsHelper.APP_ICON_STATUS, "") == "1") {
                            hideAppIcon()
                        } else {
                            showAppIcon()
                        }
                    }
                    Log.e("Volume", "volume $direction ::::::: $clickCounter")
                }

            }
        }
        mediaSessionCompat?.setPlaybackToRemote(myVolumeProvider)

        mediaSessionCompat?.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)


        var CHANNEL_ID = "volume-checker"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CHANNEL_ID = createNotificationChannel()
        }
        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setChannelId(CHANNEL_ID)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(System.currentTimeMillis() + 500)
            .setOngoing(true)
            .setAutoCancel(false)

        val notification = mBuilder.build()
        startForeground(2, notification)



        return START_STICKY
    }

    private fun createNotificationChannel(): String {
        val channelId = "launcher_icon_status"
        val channelName = "App Icon Visibility"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            service.createNotificationChannel(chan)
        }
        return channelId
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionCompat!!.release()
    }

    fun resetClickCounterHandler() {
        Handler(Looper.getMainLooper()).postDelayed( {
          clickCounter = 0
        }, 600)
    }


    fun hideAppIcon() {
        val p = packageManager
        val componentName = ComponentName(this, MainActivity::class.java)
        p.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        PrefsHelper.write(PrefsHelper.APP_ICON_STATUS, "0")

    }

    fun showAppIcon() {
        val p = packageManager
        val componentName = ComponentName(this, MainActivity::class.java)
        p.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        PrefsHelper.write(PrefsHelper.APP_ICON_STATUS, "1")
    }


//        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
}