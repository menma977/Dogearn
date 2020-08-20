package net.dogearn.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.MainActivity
import net.dogearn.R
import net.dogearn.config.BackgroundGetDataUser
import net.dogearn.config.BackgroundServiceBalance
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class UpgradeAccountActivity : AppCompatActivity() {
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var loading: Loading
  private lateinit var user: User
  private lateinit var response: JSONObject
  private lateinit var goTo: Intent
  private lateinit var balanceText: TextView
  private lateinit var upgradeTo: TextView
  private lateinit var pinText: TextView
  private lateinit var gradeText: TextView
  private lateinit var gradePrice: TextView
  private lateinit var requestPin: TextView
  private lateinit var syncData: LinearLayout
  private lateinit var buy: Button
  private lateinit var balanceValue: BigDecimal
  private var idValueServer: Int = 0
  private var gradeValue: Int = 1
  private var gradeValueMax: Int = 1
  private var pinValue: Int = 0
  private var pinValueServer: Int = 0
  private lateinit var intentServiceGetDataUser: Intent
  private lateinit var intentServiceBalance: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_upgrade_account)

    balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
    pinValue = (intent.getSerializableExtra("pin") as String).toInt()
    gradeValue = (intent.getSerializableExtra("gradeLevel") as String).toInt()

    loading = Loading(this)
    user = User(this)
    bitCoinFormat = BitCoinFormat()

    buy = findViewById(R.id.buttonBuy)
    upgradeTo = findViewById(R.id.textViewUpgradeTo)
    pinText = findViewById(R.id.textViewTotalPin)
    gradeText = findViewById(R.id.textViewGrade)
    gradePrice = findViewById(R.id.textViewGradePrice)
    requestPin = findViewById(R.id.textViewRequestPin)
    syncData = findViewById(R.id.linearLayoutSyncData)
    balanceText = findViewById(R.id.textViewBalance)

    balanceText.text = user.getString("balanceText")
    pinText.text = pinValue.toString()
    gradeText.text = gradeValue.toString()

    buy.setOnClickListener {
      upgradeSet(1)
    }

    syncData.setOnClickListener {
      upgradeSet(0)
    }

    upgradeSet(0)
  }

  private fun onBuy() {
    Timer().schedule(2500) {
      when {
        gradeValue >= gradeValueMax -> {
          runOnUiThread {
            Toast.makeText(applicationContext, "You are in max upgrade", Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }
        pinValue < pinValueServer -> {
          runOnUiThread {
            Toast.makeText(applicationContext, "your pins are less than demand", Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }
        else -> {
          val body = HashMap<String, String>()
          body["grade"] = idValueServer.toString()
          body["balance"] = balanceValue.toPlainString()
          response = WebController.Post("grade.store", user.getString("token"), body).execute().get()
          if (response.getInt("code") == 200) {
            runOnUiThread {
              user.setInteger("onQueue", 100)
              Toast.makeText(applicationContext, response.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show()
              loading.closeDialog()
              goTo = Intent(applicationContext, MainActivity::class.java)
              finishAffinity()
              startActivity(goTo)
            }
          } else {
            runOnUiThread {
              Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
              loading.closeDialog()
            }
          }
        }
      }
    }
  }

  private fun upgradeSet(type: Int) {
    loading.openDialog()
    Timer().schedule(1000) {
      response = WebController.Get("grade.create", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        val idValue = response.getJSONObject("data").getJSONObject("grade").getString("id")
        val gradeValue = response.getJSONObject("data").getJSONObject("grade").getString("price")
        val pinValue = response.getJSONObject("data").getJSONObject("grade").getString("pin")

        idValueServer = idValue.toInt()
        pinValueServer = pinValue.toInt()
        gradeValueMax = idValue.toInt()

        runOnUiThread {
          upgradeTo.text = "Upgrade To Level $idValue"
          gradePrice.text = "Request DOGE : ${bitCoinFormat.decimalToDoge(BigDecimal(gradeValue)).toInt()}"
          requestPin.text = "Request Pin : $pinValue"
          if (type == 0) {
            loading.closeDialog()
          } else {
            onBuy()
          }
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, "error when opening data. open the menu again to proceed", Toast.LENGTH_LONG).show()
          loading.closeDialog()
          finish()
        }
      }
    }
  }

  override fun onDestroy() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverDoge)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverWeb)
    stopService(intentServiceBalance)
    stopService(intentServiceGetDataUser)
    super.onDestroy()
  }

  override fun onBackPressed() {
    stopService(intentServiceBalance)
    stopService(intentServiceGetDataUser)
    finish()
    super.onBackPressed()
  }

  override fun onStart() {
    super.onStart()
    Timer().schedule(1000) {
      intentServiceBalance = Intent(applicationContext, BackgroundServiceBalance::class.java)
      startService(intentServiceBalance)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverDoge, IntentFilter("net.dogearn.doge"))

      intentServiceGetDataUser = Intent(applicationContext, BackgroundGetDataUser::class.java)
      startService(intentServiceGetDataUser)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverWeb, IntentFilter("net.dogearn.web"))
    }
  }

  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
      user.setString("balanceValue", balanceValue.toPlainString())
      user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
      balanceText.text = user.getString("balanceText")
    }
  }
  private var broadcastReceiverWeb: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      pinValue = user.getInteger("pin")
      gradeValue = user.getString("gradeLevel").toInt()

      pinText.text = pinValue.toString()
      gradeText.text = gradeValue.toString()
    }
  }
}