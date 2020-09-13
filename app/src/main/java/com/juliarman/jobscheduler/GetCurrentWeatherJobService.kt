package com.juliarman.jobscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.DecimalFormat

class GetCurrentWeatherJobService : JobService() {

    companion object{

        private val TAG = GetCurrentWeatherJobService::class.java.simpleName
        internal const val CITY = "Makassar"
        internal const val APP_ID = "46b29219e2128a38c39b6e7591b302d1"
    }

    override fun onStartJob(params: JobParameters): Boolean {

        Log.d(TAG, "onStartJob()")
        getCurrentWeather(params)

        return true

    }

    private fun getCurrentWeather(job: JobParameters) {

        Log.d(TAG, "getCurrentWeather: Mulai.....")
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$CITY&appid=$APP_ID"
        Log.d(TAG, "getCurrenWeather: $url")
        val client = AsyncHttpClient()

        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {

                val result = String(responseBody)
                Log.d(TAG, result)

                try {
                    val responseObject = JSONObject(result)
                    val currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description")
                    val tempInKelvin = responseObject.getJSONObject("main").getDouble("temp")

                    val tempCelsius = tempInKelvin - 273
                    val temperature = DecimalFormat("##.##").format(tempCelsius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temperature celsius"
                    val notifId = 100

                    showNotification(applicationContext, title, message, notifId)

                    Log.d(TAG, " onSuccess: Selesai.....")
                    jobFinished(job, false)
                }catch (e: Exception){

                    Log.d(TAG, "onSucess: Gagal.....")
                    jobFinished(job, true)
                    e.printStackTrace()
                }

            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {


                Log.d(TAG, "onFailure: Gagal.....")
                jobFinished(job, true)

            }

        })

    }

    private fun showNotification(applicationContext: Context?, title: String, message: String, notifId: Int) {

        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job scheduler channel"

        val notificationManagerCompact = applicationContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.baseline_replay_30_black_18dp)
            .setContentText(message)
            .setColor(ContextCompat.getColor(applicationContext, android.R.color.black))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

            channel.enableVibration(true)
            channel.vibrationPattern = (longArrayOf(1000, 1000, 1000, 1000, 1000))

            builder.setChannelId(CHANNEL_ID)
            notificationManagerCompact.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManagerCompact.notify(notifId, notification)

    }

    override fun onStopJob(params: JobParameters?): Boolean {

        Log.d(TAG, "onStopJob()")
        return true

    }
}