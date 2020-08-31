package net.dogearn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.dogearn.config.BitCoinFormat
import net.dogearn.controller.WebController
import net.dogearn.model.Setting
import net.dogearn.model.User
import net.dogearn.view.LoginActivity
import net.dogearn.view.NavigationActivity
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    user = User(this)
    setting = Setting(this)
    bitCoinFormat = BitCoinFormat()

    getData()
  }

  private fun getData() {
    Timer().schedule(100) {
      response = if (user.getString("token").isEmpty()) {
        WebController.Get("get.data", user.getString("token")).execute().get()
      } else {
        WebController.Get("get.data.login", user.getString("token")).execute().get()
      }
      goTo = Intent(applicationContext, LoginActivity::class.java)
      if (response.getInt("code") == 200) {
        if (response.getJSONObject("data").getString("version") == BuildConfig.VERSION_CODE.toString()) {
          if (response.getJSONObject("data").getBoolean("isLogin")) {
            user.setInteger("isWin", response.getJSONObject("data").getInt("isUserWin"))
            isLogin()
          } else {
            isNotLogin()
          }
        } else {
          isUpdate()
        }
      } else {
        isError()
      }
    }
  }

  private fun isLogin() {
    goTo = Intent(applicationContext, NavigationActivity::class.java)
    startActivity(goTo)
    finish()
  }

  private fun isNotLogin() {
    goTo.putExtra("lock", false)
    goTo.putExtra("isUpdate", false)
    goTo.putExtra("version", "Version ${BuildConfig.VERSION_CODE}")
    startActivity(goTo)
    finish()
  }

  private fun isUpdate() {
    user.clear()
    setting.clear()
    goTo.putExtra("lock", true)
    goTo.putExtra("isUpdate", true)
    goTo.putExtra("version", "New Version ${response.getJSONObject("data").getString("version")}")
    startActivity(goTo)
    finish()
  }

  private fun isError() {
    user.clear()
    setting.clear()
    goTo.putExtra("lock", true)
    goTo.putExtra("isUpdate", false)
    goTo.putExtra("version", "problematic connection. please close the app and reopen it to continue")
    startActivity(goTo)
    finish()
  }
}