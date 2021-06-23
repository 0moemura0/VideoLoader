package com.omoemurao.videoloader


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit


open class DownloadService : JobIntentService() {
    val byteCount = 8192
    private lateinit var client: OkHttpClient
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var videoId: String? = null
    private var token: String? = null
    private var targetId: Int = -1

    override fun onHandleWork(intent: Intent) {
        Log.d("DEBUG", "ImageDownloadService triggered")

        val videoUrl: String? = intent.getStringExtra("url")
        val videoUri: Uri? = intent.getParcelableExtra("uri")
        videoId = intent.getStringExtra("video_id")
        targetId = intent.getIntExtra("target_id", -1)
        token = intent.getStringExtra("token")
        if (videoUrl != null && videoUri != null) {
            createNotification()
            postVideo(videoUrl, videoUri)
        }
    }

    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = "CHANNEL"
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Download")
            .setContentText("Download in progress")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setProgress(100, 0, false)

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "CHANNEL",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    fun postVideo(mainUrl: String, path: Uri) {

        val contentResolver: ContentResolver = application.contentResolver
        val contentType = contentResolver.getType(path)
        val fd = contentResolver.openAssetFileDescriptor(path, "r")
            ?: throw FileNotFoundException("could not open file descriptor")

        val videoFile: RequestBody = object : RequestBody() {
            override fun contentLength(): Long {
                return fd.declaredLength
            }

            override fun contentType(): MediaType? {
                return contentType?.toMediaType()
            }

            override fun writeTo(sink: BufferedSink) {
                var fileLength: Long = contentLength()
                val buffer = ByteArray(byteCount)
                var uploaded = 0
                var read = 0
                try {
                    val stream = fd.createInputStream()
                    read = stream.read(buffer)
                    while (read != -1) {
//TODO очистка памяти
                        uploaded += read
                        sink.write(buffer, 0, read)
                        Log.d("write: ", "bytes: $buffer")
                        Log.e("writeTo: ", uploaded.toString())

                        notificationBuilder.setProgress(100,
                            (100 * uploaded / fd.length).toInt(),
                            false)
                        notificationManager.notify(0, notificationBuilder.build())
                        read = stream.read(buffer)
                    }
                    stream.close()
                } catch (e: java.lang.Exception) {
                    Log.e("file_upload", "Exception thrown while uploading", e)
                }
            }
        }

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("video_file", "fname", videoFile)
            .build()
        val request: Request = Request.Builder()
            .url(mainUrl)
            .post(requestBody)
            //.addHeader("Range", "bytes=$downloadedSize-")
            .build()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)

        val client = OkHttpClient().newBuilder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                try {
                    fd.close()
                    notificationManager.cancel(0)
                } catch (ex: java.lang.Exception) {

                }
                Log.e("TAG", "failed", e)
                notAddVideoToast()
            }

            override fun onResponse(call: Call, response: Response) {

                fd.close()
                notificationManager.cancel(0)
                Log.e("TAG", "onResponse")
                addVideoToList()
            }
        })

    }

    private fun addVideoToList() {
        val url = "${Constants.BASE_URL}/video.add?" +
                "target_id=${targetId}" +
                "&video_id=${videoId}" +
                "&owner_id=${targetId}" +
                "&access_token=${token}" +
                "&v=${Constants.VER}"

        val request = Request.Builder()
            .url(url)
            .build()
        client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notAddVideoToast()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        addVideoToast()
                    } catch (e: Exception) {

                    }
                }

            }
        })
    }

    companion object {
        private const val JOB_ID = 101
        const val NOTIF_ID = 82
        fun enqueueWork(context: Context?, work: Intent?) {
            enqueueWork(context!!,
                DownloadService::class.java, JOB_ID, work!!)
        }
    }

    fun addVideoToast() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(this@DownloadService.applicationContext,
                "Added!",
                Toast.LENGTH_SHORT).show()
        }
    }

    fun notAddVideoToast() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(this@DownloadService.applicationContext,
                "Did not Added!",
                Toast.LENGTH_SHORT).show()
        }
    }
}