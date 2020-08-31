package net.dogearn.view.menu.bot.manual

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import net.dogearn.MainActivity
import net.dogearn.R
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.DogeController
import net.dogearn.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class BotManualActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
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
  private lateinit var balance: BigDecimal
  private lateinit var payIn: BigDecimal
  private lateinit var profit: BigDecimal
  private var high = BigDecimal(30)
  private var maxRow = 10
  private var seed = (0..99999).random().toString()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_manual)

    user = User(this)
    bitCoinFormat = BitCoinFormat()
    loading = Loading(this)

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

    highText.text = "Possibility: ${high.toPlainString()}%"
    balanceText.text = intent.getStringExtra("balanceView")
    balance = intent.getStringExtra("balance")!!.toBigDecimal()

    stakeButton.setOnClickListener {
      if (inputBalance.text.isEmpty()) {
        Toast.makeText(this, "Amount cant not be empty", Toast.LENGTH_SHORT).show()
      } else {
        payIn = bitCoinFormat.dogeToDecimal(inputBalance.text.toString().toBigDecimal())
        onBetting()
      }
    }

    highSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        var getProgress = progress
        if (progress == 0) {
          highSeekBar.progress = 1
          getProgress = 1
        }
        high = getProgress.toBigDecimal().multiply(BigDecimal(10))
        highText.text = "Possibility: ${getProgress * 10}%"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    setDefaultView()
  }

  override fun onBackPressed() {
    finishAffinity()
    val goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
  }

  private fun onBetting() {
    loading.openDialog()
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "PlaceBet"
      body["s"] = user.getString("key")
      body["Low"] = "0"
      body["High"] = high.multiply(BigDecimal(10000)).toPlainString()
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
          setView("${bitCoinFormat.decimalToDoge(balance).toPlainString()} DOGE", fundLinearLayout, false, winBot)

          balance = balanceRemaining
          balanceText.text = "${bitCoinFormat.decimalToDoge(balanceRemaining).toPlainString()} DOGE"

          user.setString("balanceValue", balance.toPlainString())
          user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balance).toPlainString()} DOGE")

          setView(highText.text.toString(), highLinearLayout, false, winBot)
          setView("${BitCoinFormat().decimalToDoge(balance).toPlainString()} DOGE", resultLinearLayout, false, winBot)
          if (winBot) {
            stakeButton.visibility = Button.GONE
            statusText.text = "WIN"
            statusText.setTextColor(getColor(R.color.Success))

            inputBalance.isEnabled = false
          } else {
            statusText.text = "LOSE"
            statusText.setTextColor(getColor(R.color.Danger))

            inputBalance.isEnabled = true
          }
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
    setView("Fund", fundLinearLayout, isNew = true, isWin = false)
    setView("Possibility", highLinearLayout, isNew = true, isWin = false)
    setView("Result", resultLinearLayout, isNew = true, isWin = false)
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
}