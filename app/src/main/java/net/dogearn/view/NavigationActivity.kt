package net.dogearn.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.MainActivity
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
import net.dogearn.view.fragment.SettingFragment
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

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
  private lateinit var goTo: Intent

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

    loading.openDialog()

    setNavigation()
    getBalance()
  }

  override fun onStart() {
    super.onStart()
    Timer().schedule(1000) {
      intentServiceBalance = Intent(applicationContext, BackgroundServiceBalance::class.java)
      startService(intentServiceBalance)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverDoge, IntentFilter("net.dogearn.doge"))

      intentServiceGetDataUser = Intent(applicationContext, BackgroundGetDataUser::class.java)
      startService(intentServiceGetDataUser)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverWebLogout, IntentFilter("net.dogearn.web.logout"))
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverWebLogout)
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

  private var broadcastReceiverWebLogout: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.getBooleanExtra("isLogout", false)) {
        onLogout()
      }
    }
  }
  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
      user.setString("balanceValue", balanceValue.toPlainString())
      user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
    }
  }

  private fun getBalance() {
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
        user.setString("balanceValue", balanceValue.toPlainString())
        user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
        getDataUser()
      } else {
        user.setString("balanceValue", "0")
        user.setString("balanceText", "ERROR 500")
        runOnUiThread {
          loading.closeDialog()
          exitProcess(1)
        }
      }
    }
  }

  private fun getDataUser() {
    Timer().schedule(1000) {
      response = WebController.Get("user.show", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        user.setString("wallet", response.getJSONObject("data").getJSONObject("user").getString("wallet"))
        user.setString("phone", response.getJSONObject("data").getJSONObject("user").getString("phone"))
        user.setString("email", response.getJSONObject("data").getJSONObject("user").getString("email"))
        if (response.getJSONObject("data").getString("grade") == "0" || response.getJSONObject("data").getString("grade") == "null") {
          user.setString("gradeTarget", "0")
          user.setString("gradeLevel", "0")
        } else {
          user.setString("gradeTarget", response.getJSONObject("data").getString("gradeTarget"))
          user.setString("gradeLevel", response.getJSONObject("data").getJSONObject("grade").getString("id"))
        }
        user.setInteger("pin", response.getJSONObject("data").getInt("pin"))
        user.setInteger("isUserWin", response.getJSONObject("data").getInt("isUserWin"))
        user.setString("progressGrade", response.getJSONObject("data").getString("progressGrade"))
        user.setInteger("onQueue", response.getJSONObject("data").getInt("onQueue"))

        runOnUiThread {
          //set Default Fragment
          val fragment = HomeFragment()
          addFragment(fragment)
          loading.closeDialog()
        }
      } else {
        if (response.getString("data").contains("Unauthenticated.")) {
          onLogout()
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }
      }
    }
  }

  private fun onLogout() {
    Timer().schedule(100) {
      response = WebController.Get("user.logout", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        user.clear()
        config.clear()
        goTo = Intent(applicationContext, MainActivity::class.java)
        loading.closeDialog()
        startActivity(goTo)
        finishAffinity()
      } else {
        if (response.getString("data").contains("Unauthenticated.")) {
          user.clear()
          config.clear()
          goTo = Intent(applicationContext, MainActivity::class.java)
          loading.closeDialog()
          startActivity(goTo)
          finishAffinity()
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
      val fragment = SettingFragment()
      addFragment(fragment)
    }

    dogeChain.setOnClickListener {
      val uri = "https://dogechain.info/address/${user.getString("wallet")}"
      goTo = Intent(Intent.ACTION_VIEW)
      goTo.data = Uri.parse(uri)
      startActivity(goTo)
    }
  }

  @SuppressLint("PrivateResource")
  private fun addFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().setCustomAnimations(
      R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out
    ).replace(R.id.contentFragment, fragment, fragment.javaClass.simpleName).addToBackStack("back").commit()
  }
}