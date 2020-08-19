package net.dogearn.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject

class BackgroundGetDataUser : IntentService("BackgroundGetDataUser") {
  private lateinit var response: JSONObject
  private lateinit var user: User
  private var startBackgroundService: Boolean = false

  override fun onHandleIntent(p0: Intent?) {
    user = User(this)
    var time = System.currentTimeMillis()
    val trigger = Object()

    synchronized(trigger) {
      startBackgroundService = true
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            response = WebController.Get("user.show", user.getString("token")).execute().get()
            println(response)
            if (response.getInt("code") == 200) {
              privateIntent.putExtra("wallet", response.getJSONObject("data").getJSONObject("user").getString("wallet"))
              if (response.getJSONObject("data").getString("grade") == "null") {
                privateIntent.putExtra("grade", "0")
                privateIntent.putExtra("gradeLevel", "0")
              } else {
                privateIntent.putExtra("grade", response.getJSONObject("data").getInt("progressGrade"))
                privateIntent.putExtra("gradeLevel", response.getJSONObject("data").getJSONObject("grade").getInt("id"))
              }
              privateIntent.putExtra("pin", response.getJSONObject("data").getInt("pin"))
              privateIntent.putExtra("isWin", response.getJSONObject("data").getInt("isUserWin"))
              privateIntent.putExtra("progressGrade", response.getJSONObject("data").getString("progressGrade"))

              privateIntent.action = "net.dogearn.web"
              LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
            } else {
              trigger.wait(5000)
            }
          } else {
            break
          }
        }
      }
    }
  }

  override fun onDestroy() {
    startBackgroundService = false
    super.onDestroy()
  }
}