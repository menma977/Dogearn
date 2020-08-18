package net.dogearn.controller

import android.os.AsyncTask
import net.dogearn.config.MapToJson
import net.dogearn.model.Url
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class WebController {
  class Post(private var targetUrl: String, private var token: String, private var body: HashMap<String, String>) : AsyncTask<Void, Void, JSONObject>() {
    override fun doInBackground(vararg params: Void?): JSONObject {
      return try {
        val client = OkHttpClient.Builder().build()
        val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = MapToJson().map(body).toRequestBody(mediaType)
        val request = Request.Builder()
        request.url(Url.web() + targetUrl)
        request.post(body)
        if (token.isNotEmpty()) {
          request.addHeader("Authorization", "Bearer $token")
        }
        request.addHeader("X-Requested-With", "XMLHttpRequest")
        val response: Response = client.newCall(request.build()).execute()
        val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        val inputData: String = input.readLine()
        val convertJSON = JSONObject(inputData)
        return when {
          response.isSuccessful -> {
            JSONObject().put("code", 200).put("data", convertJSON)
          }
          else -> {
            JSONObject().put("code", 500).put("data", convertJSON)
          }
        }
      } catch (e: Exception) {
        JSONObject().put("code", 500).put("data", e.message)
      }
    }
  }

  class Get(private var targetUrl: String, private var token: String) : AsyncTask<Void, Void, JSONObject>() {
    override fun doInBackground(vararg p0: Void?): JSONObject {
      val client = OkHttpClient().newBuilder().build()
      val request = Request.Builder()
      request.url(Url.web() + targetUrl)
      request.method("GET", null)
      if (token.isNotEmpty()) {
        request.addHeader("Authorization", "Bearer $token")
      }
      request.addHeader("X-Requested-With", "XMLHttpRequest")
      val response = client.newCall(request.build()).execute()
      val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
      val inputData: String = input.readLine()
      val convertJSON = JSONObject(inputData)
      return try {
        if (response.isSuccessful) {
          JSONObject().put("code", 200).put("data", convertJSON)
        } else {
          JSONObject().put("code", 500).put("data", convertJSON)
        }
      } catch (e: Exception) {
        JSONObject().put("code", 500).put("data", e.message)
      }
    }
  }
}