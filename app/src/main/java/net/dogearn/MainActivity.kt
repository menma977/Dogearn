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
      response = WebController.Get("get/data", user.getString("token")).execute().get()
      goTo = Intent(applicationContext, LoginActivity::class.java)
      if (response.getJSONObject("data").getString("version") == BuildConfig.VERSION_CODE.toString()) {
        if (response.getJSONObject("data").getBoolean("isLogin")) {
          goTo = Intent(applicationContext, NavigationActivity::class.java)
          startActivity(goTo)
          finish()
        } else {
          goTo.putExtra("lock", false)
          goTo.putExtra("isUpdate", false)
          goTo.putExtra("version", response.getJSONObject("data").getString("version"))
          startActivity(goTo)
          finish()
        }
      } else {
        user.clear()
        setting.clear()
        goTo.putExtra("lock", true)
        goTo.putExtra("isUpdate", true)
        goTo.putExtra("version", response.getJSONObject("data").getString("version"))
        startActivity(goTo)
        finish()
      }
    }
  }
}