package com.omoemurao.videoloader

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.omoemurao.videoloader.Constants.BASE_URL
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    var name: String = ""
    var description: String? = null
    val listLiveData: MutableLiveData<List<Video>> = MutableLiveData()
    fun getToken(): String? {
        val sPref = getApplication<Application>().applicationContext.getSharedPreferences("APP",
            MODE_PRIVATE)
        return sPref.getString("token", null)
    }

    fun setToken(token: String) {
        val sPref = getApplication<Application>().applicationContext.getSharedPreferences("APP",
            MODE_PRIVATE)
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putString("token", token)
        ed.apply()
    }

    fun getID(): Int {
        val sPref = getApplication<Application>().applicationContext.getSharedPreferences("APP",
            MODE_PRIVATE)
        return sPref.getInt("id", -1)
    }

    fun setID(id: Int) {
        val sPref = getApplication<Application>().applicationContext.getSharedPreferences("APP",
            MODE_PRIVATE)
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putInt("id", id)
        ed.apply()
    }

    fun addVideo(
        uri: Uri,
        is_private: Boolean? = null
    ) {
        val url = "$BASE_URL/video.save?name=$name" +
                if (description != null) "&description=$description" else {""} +
                "&access_token=${getToken()}&v=${Constants.VER}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val json = JSONObject(response.body!!.string())
                        val resp = json.getJSONObject("response")
                        val i = Intent(getApplication<Application>().applicationContext,
                            DownloadService::class.java)

                        i.putExtra("url", resp.getString("upload_url"))
                        i.putExtra("uri", uri)
                        i.putExtra("video_id", resp.getString("video_id"))
                        i.putExtra("target_id", getID())
                        i.putExtra("token", getToken())

                        DownloadService.enqueueWork(getApplication<Application>().applicationContext,
                            i)
                    }
                }
            }
        })
    }

    fun foregroundLoading() {

    }

    fun backgroundLoading() {

    }

    fun getList() {
        val url = "$BASE_URL/video.get?owner_id=${getID()}" +
                "&access_token=${getToken()}&v=${Constants.VER}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val json = JSONObject(response.body!!.string())
                        val resp = json.getJSONObject("response")
                        val count = resp.getInt("count")
                        val jsonArray: JSONArray = resp.getJSONArray("items")
                        val videoArray: MutableList<Video> = mutableListOf()
                        if (count != 0)
                            for (i in 0 until count) {
                                val item = jsonArray.getJSONObject(i)
                                videoArray.add(Video(
                                    item.getString("title"),
                                    item.getString("description"),
                                    item.getInt("views")
                                ))
                            }

                        listLiveData.postValue(videoArray)

                    }
                }
            }
        })
    }

    fun setVideoData(videoData: Map<String, String>) {
        name = videoData["name"].toString()
        description = videoData["description"].toString()
    }

}

