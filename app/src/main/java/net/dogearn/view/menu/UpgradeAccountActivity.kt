package net.dogearn.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
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
  private lateinit var secondaryPassword: TextView
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
  private lateinit var container: LinearLayout

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
    secondaryPassword = findViewById(R.id.editTextSecondaryPassword)
    syncData = findViewById(R.id.linearLayoutSyncData)
    balanceText = findViewById(R.id.textViewBalance)
    container = findViewById(R.id.linearLayoutDataContent)

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
        secondaryPassword.text.isEmpty() -> {
          runOnUiThread {
            Toast.makeText(applicationContext, "Secondary Password is required", Toast.LENGTH_SHORT).show()
            secondaryPassword.requestFocus()
            loading.closeDialog()
          }
        }
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
          body["secondaryPassword"] = secondaryPassword.text.toString()
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
              Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
              loading.closeDialog()
            }
          }
        }
      }
    }
  }

  private fun upgradeSet(type: Int) {
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val descriptionParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)

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
          if (type == 0) {
            val dataGrabber = response.getJSONObject("data").getJSONArray("dataQueue")
            for (i in 0 until dataGrabber.length()) {
              runOnUiThread {
                //body container
                val containerLinearLayout = LinearLayout(applicationContext)
                containerLinearLayout.layoutParams = linearLayoutParams
                containerLinearLayout.gravity = Gravity.CENTER
                containerLinearLayout.orientation = LinearLayout.VERTICAL
                containerLinearLayout.setBackgroundResource(R.drawable.card_default)
                containerLinearLayout.setPadding(10, 10, 10, 10)
                containerLinearLayout.elevation = 20F
                //description in sub container 1
                val user = TextView(applicationContext)
                user.layoutParams = descriptionParams
                user.text = dataGrabber.getJSONObject(i).getString("user")
                user.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                user.gravity = Gravity.CENTER
                containerLinearLayout.addView(user)
                val value = TextView(applicationContext)
                value.layoutParams = descriptionParams
                value.text = "${bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("value").toBigDecimal()).toPlainString()} DOGE"
                value.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                value.gravity = Gravity.CENTER
                containerLinearLayout.addView(value)
                //set container to parent container
                container.addView(containerLinearLayout)
                val wrapLine = View(applicationContext)
                wrapLine.layoutParams = line
                wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.Dark))
                container.addView(wrapLine)
              }
            }

            upgradeTo.text = "Upgrade To Level $idValue"
            gradePrice.text = "Request DOGE : ${bitCoinFormat.decimalToDoge(BigDecimal(gradeValue)).toInt()}"
            requestPin.text = "Request Pin : $pinValue"
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