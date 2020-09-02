package net.dogearn.view.menu.bot.manual

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
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
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class BotManualActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var config: Setting
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var fundText: TextView
  private lateinit var balanceText: TextView
  private lateinit var gradeText: TextView
  private lateinit var highText: TextView
  private lateinit var statusText: TextView
  private lateinit var highSeekBar: SeekBar
  private lateinit var inputBalance: EditText
  private lateinit var stakeButton: Button
  private lateinit var fundLinearLayout: LinearLayout
  private lateinit var highLinearLayout: LinearLayout
  private lateinit var resultLinearLayout: LinearLayout
  private lateinit var statusLinearLayout: LinearLayout
  private lateinit var balance: BigDecimal
  private lateinit var payIn: BigDecimal
  private lateinit var profit: BigDecimal
  private lateinit var startBalance: BigDecimal
  private lateinit var intentServiceGetDataUser: Intent
  private lateinit var intentServiceBalance: Intent
  private lateinit var goTo: Intent
  private var high = BigDecimal(5)
  private var maxRow = 10
  private var seed = (0..99999).random().toString()
  private lateinit var percentTable: ArrayList<Double>
  private var defaultMaximum = 0.01
  private var percent = 0.01
  private var maxBalance = BigDecimal(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_manual)

    user = User(this)
    config = Setting(this)
    bitCoinFormat = BitCoinFormat()
    loading = Loading(this)
    percentTable = ArrayList()

    fundText = findViewById(R.id.textViewFund)
    balanceText = findViewById(R.id.textViewBalance)
    gradeText = findViewById(R.id.textViewGrade)
    highText = findViewById(R.id.textViewHigh)
    statusText = findViewById(R.id.textViewStatus)
    inputBalance = findViewById(R.id.editTextInputBalance)
    highSeekBar = findViewById(R.id.seekBarHigh)
    stakeButton = findViewById(R.id.buttonStake)
    fundLinearLayout = findViewById(R.id.linearLayoutFund)
    highLinearLayout = findViewById(R.id.linearLayoutHigh)
    resultLinearLayout = findViewById(R.id.linearLayoutResult)
    statusLinearLayout = findViewById(R.id.linearLayoutStatus)

    setListTargetMaximum()

    highText.text = "Possibility: ${high * BigDecimal(10)}%"
    balanceText.text = intent.getStringExtra("balanceView")
    balance = intent.getStringExtra("balance")!!.toBigDecimal()
    gradeText.text = intent.getStringExtra("grade")
    startBalance = balance
    maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()))
    fundText.text = "Maximum : ${bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()).toPlainString()}"

    stakeButton.setOnClickListener {
      if (inputBalance.text.isEmpty()) {
        Toast.makeText(this, "Amount cant not be empty", Toast.LENGTH_SHORT).show()
      } else {
        payIn = bitCoinFormat.dogeToDecimal(inputBalance.text.toString().toBigDecimal())
        if (payIn > maxBalance) {
          Toast.makeText(this, "Doge you can input should not be more than ${bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()).toPlainString()}", Toast.LENGTH_LONG).show()
        } else {
          onBetting()
        }
      }
    }

    highSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        var getProgress = progress
        if (progress == 0) {
          highSeekBar.progress = 1
          getProgress = 1
        }
        if (progress == 10) {
          highSeekBar.progress = 9
          getProgress = 9
        }
        if (progress >= 0 || progress <= 10) {
          percent = defaultMaximum * percentTable[getProgress - 1]
        }
        maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()))
        fundText.text = "Maximum : ${bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()).toPlainString()}"
        high = getProgress.toBigDecimal()
        highText.text = "Possibility: ${getProgress * 10}%"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    setDefaultView()
  }

  override fun onBackPressed() {
    stopService(intentServiceBalance)
    stopService(intentServiceGetDataUser)
    finishAffinity()
    goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverWebLogout)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverDoge)
    stopService(intentServiceBalance)
    stopService(intentServiceGetDataUser)
    super.onStop()
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

  private var broadcastReceiverWebLogout: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.getBooleanExtra("isLogout", false)) {
        onLogout()
      }
    }
  }
  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance = intent.getSerializableExtra("balanceValue") as BigDecimal
      user.setString("balanceValue", balance.toPlainString())
      user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balance).toPlainString()} DOGE")
      balanceText.text = user.getString("balanceText")
      maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()))
      val textFund = "Maximum : ${bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()).toPlainString()}"
      fundText.text = textFund
    }
  }

  private fun onBetting() {
    loading.openDialog()
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "PlaceBet"
      body["s"] = user.getString("key")
      body["Low"] = "0"
      body["High"] = (high.multiply(BigDecimal(10)).multiply(BigDecimal(10000)) - BigDecimal(600)).toPlainString()
      body["PayIn"] = payIn.toPlainString()
      body["ProtocolVersion"] = "2"
      body["ClientSeed"] = seed
      body["Currency"] = "doge"
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        seed = response.getJSONObject("data")["Next"].toString()
        val puyOut = response.getJSONObject("data")["PayOut"].toString().toBigDecimal()
        var balanceRemaining = response.getJSONObject("data")["StartingBalance"].toString().toBigDecimal()

        profit = puyOut - payIn
        balanceRemaining += profit
        val winBot = profit > BigDecimal(0)

        runOnUiThread {
          balance = balanceRemaining
          balanceText.text = "${bitCoinFormat.decimalToDoge(balanceRemaining).toPlainString()} DOGE"

          user.setString("balanceValue", balance.toPlainString())
          user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balance).toPlainString()} DOGE")

          setView(bitCoinFormat.decimalToDoge(payIn).toPlainString(), fundLinearLayout, false, winBot)
          setView("${high.multiply(BigDecimal(10))}%", highLinearLayout, false, winBot)
          setView(BitCoinFormat().decimalToDoge(puyOut).toPlainString(), resultLinearLayout, false, winBot)
          if (winBot) {
            setView("WIN", statusLinearLayout, false, winBot)
            stakeButton.visibility = Button.GONE
            statusText.text = "WIN"
            statusText.setTextColor(getColor(R.color.Success))

            body["start_balance"] = startBalance.toPlainString()
            body["end_balance"] = balance.toPlainString()
            response = WebController.Post("doge.store", user.getString("token"), body).execute().get()

            inputBalance.isEnabled = false
          } else {
            setView("LOSE", statusLinearLayout, false, winBot)
            statusText.text = "LOSE"
            statusText.setTextColor(getColor(R.color.Danger))

            defaultMaximum *= 2
            percent = defaultMaximum
            highSeekBar.progress = 5

            maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()))
            fundText.text = "Maximum : ${bitCoinFormat.decimalToDoge(balance).multiply(percent.toBigDecimal()).toPlainString()}"

            inputBalance.isEnabled = true
          }

          inputBalance.setText("")
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun setDefaultView() {
    setView("Fund DOGE", fundLinearLayout, isNew = true, isWin = false)
    setView("Possibility", highLinearLayout, isNew = true, isWin = false)
    setView("Result", resultLinearLayout, isNew = true, isWin = false)
    setView("Status", statusLinearLayout, isNew = true, isWin = false)

    for (i in 0 until maxRow) {
      setView("", fundLinearLayout, isNew = true, isWin = false)
      setView("", highLinearLayout, isNew = true, isWin = false)
      setView("", resultLinearLayout, isNew = true, isWin = false)
      setView("", statusLinearLayout, isNew = true, isWin = false)
    }
  }

  private fun setView(value: String, linearLayout: LinearLayout, isNew: Boolean, isWin: Boolean) {
    val template = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val valueView = TextView(applicationContext)
    valueView.text = value
    valueView.gravity = Gravity.CENTER
    valueView.layoutParams = template
    if (isNew) {
      valueView.setTextColor(getColor(R.color.colorAccent))
    } else {
      if (isWin) {
        valueView.setTextColor(getColor(R.color.Success))
        stakeButton.visibility = Button.GONE
      } else {
        valueView.setTextColor(getColor(R.color.Danger))
      }
    }

    if ((linearLayout.childCount - 1) == maxRow) {
      linearLayout.removeViewAt(linearLayout.childCount - 1)
      linearLayout.addView(valueView, 1)
    } else {
      linearLayout.addView(valueView)
    }
  }

  private fun setListTargetMaximum() {
    percentTable.add(0.2)
    percentTable.add(0.3)
    percentTable.add(0.4)
    percentTable.add(0.5)
    percentTable.add(1.0)
    percentTable.add(2.0)
    percentTable.add(3.0)
    percentTable.add(4.0)
    percentTable.add(5.0)
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
}