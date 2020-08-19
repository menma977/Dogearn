package net.dogearn.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.R
import net.dogearn.config.BackgroundGetDataUser
import net.dogearn.config.BackgroundServiceBalance
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.DogeController
import net.dogearn.controller.WebController
import net.dogearn.model.Setting
import net.dogearn.model.User
import net.dogearn.view.fragment.HomeFragment
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class NavigationActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var config: Setting
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var home: ImageButton
  private lateinit var dogeChain: ImageButton
  private lateinit var setting: ImageButton
  private lateinit var intentServiceGetDataUser: Intent
  private lateinit var intentServiceBalance: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    loading = Loading(this)
    user = User(this)
    config = Setting(this)
    bitCoinFormat = BitCoinFormat()
    //navigation
    home = findViewById(R.id.buttonHome)
    dogeChain = findViewById(R.id.buttonDogeChain)
    setting = findViewById(R.id.buttonSetting)
    //set Default Fragment
    val fragment = HomeFragment()
    addFragment(fragment)

    setNavigation()
    getBalance()
    getDataUser()
  }

  override fun onStart() {
    super.onStart()
    Timer().schedule(100) {
      intentServiceBalance = Intent(applicationContext, BackgroundServiceBalance::class.java)
      startService(intentServiceBalance)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverDoge, IntentFilter("net.dogearn.doge"))

      intentServiceGetDataUser = Intent(applicationContext, BackgroundGetDataUser::class.java)
      startService(intentServiceGetDataUser)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverWeb, IntentFilter("net.dogearn.web"))
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverWeb)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverDoge)
    stopService(intentServiceBalance)
    stopService(intentServiceGetDataUser)
    super.onStop()
  }

  override fun onBackPressed() {
    stopService(intentServiceBalance)
    stopService(intentServiceGetDataUser)
    super.onBackPressed()
  }

  private var broadcastReceiverWeb: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      user.setString("wallet", intent.getSerializableExtra("wallet") as String)
      user.setString("gradeLevel", intent.getStringExtra("gradeLevel") as String)
      user.setInteger("pin", intent.getIntExtra("pin", 0))
      user.setInteger("isUserWin", intent.getIntExtra("isWin", 0))
      user.setString("progressGrade", intent.getStringExtra("progressGrade") as String)
    }
  }
  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
    }
  }

  private fun getBalance() {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "GetBalance"
      body["s"] = user.getString("key")
      body["Currency"] = "doge"
      body["Referrals"] = "0"
      body["Stats"] = "0"
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
        user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
        runOnUiThread {
          loading.closeDialog()
        }
      } else {
        user.setString("balanceText", "ERROR 404")
        runOnUiThread {
          loading.closeDialog()
        }
      }
    }
  }

  private fun getDataUser() {
    loading.openDialog()
    Timer().schedule(1000) {
      response = WebController.Get("user.show", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        user.setString("wallet", response.getJSONObject("data").getJSONObject("user").getString("wallet"))
        if (response.getJSONObject("data").getString("grade") == "null") {
          user.setString("grade", "0")
          user.setString("gradeLevel", "Grade 0")
        } else {
          user.setString("grade", response.getJSONObject("data").getJSONObject("grade").getString("price"))
          user.setInteger("gradeLevel", response.getJSONObject("data").getJSONObject("grade").getInt("id"))
        }
        user.setInteger("pin", response.getJSONObject("data").getInt("pin"))
        user.setInteger("isUserWin", response.getJSONObject("data").getInt("isUserWin"))
        user.setString("progressGrade", response.getJSONObject("data").getString("progressGrade"))
        runOnUiThread {
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          loading.closeDialog()
        }
      }
    }
  }

  private fun setNavigation() {
    home.setOnClickListener {
      val fragment = HomeFragment()
      addFragment(fragment)
    }

    setting.setOnClickListener {
      val fragment = HomeFragment()
      addFragment(fragment)
    }
  }

  @SuppressLint("PrivateResource")
  private fun addFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().setCustomAnimations(
      R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out
    ).replace(R.id.contentFragment, fragment, fragment.javaClass.simpleName).commit()
  }
}