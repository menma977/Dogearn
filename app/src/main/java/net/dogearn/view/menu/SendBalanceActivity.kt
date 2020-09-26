package net.dogearn.view.menu

import android.content.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import net.dogearn.R
import net.dogearn.config.BackgroundServiceBalance
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.DogeController
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class SendBalanceActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var response: JSONObject
  private lateinit var frameScanner: FrameLayout
  private lateinit var scannerEngine: ZXingScannerView
  private lateinit var wallet: String
  private lateinit var userBalance: TextView
  private lateinit var balanceText: EditText
  private lateinit var sendDoge: Button
  private lateinit var walletText: EditText
  private lateinit var secondaryPasswordText: EditText
  private lateinit var intentServiceBalance: Intent
  private lateinit var balanceValue: BigDecimal
  private var isHasCode = false
  private var isStart = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_send_balance)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    userBalance = findViewById(R.id.textViewBalance)
    walletText = findViewById(R.id.editTextWallet)
    frameScanner = findViewById(R.id.frameLayoutScanner)
    balanceText = findViewById(R.id.editTextBalance)
    secondaryPasswordText = findViewById(R.id.editTextSecondaryPassword)
    sendDoge = findViewById(R.id.buttonSend)

    initScannerView()

    frameScanner.setOnClickListener {
      if (isStart) {
        scannerEngine.startCamera()
        isStart = false
      }
    }

    sendDoge.setOnClickListener {
      validatePassword()
    }

    userBalance.text = user.getString("balanceText")
    balanceValue = user.getString("balanceValue").toBigDecimal()
  }

  private fun validatePassword() {
    loading.openDialog()
    when {
      balanceText.text.isEmpty() -> {
        Toast.makeText(this, "balance cant not be empty", Toast.LENGTH_SHORT).show()
        loading.closeDialog()
      }
      walletText.text.isEmpty() -> {
        Toast.makeText(this, "Wallet cant not be empty", Toast.LENGTH_SHORT).show()
        loading.closeDialog()
      }
      else -> {
        Timer().schedule(1000) {
          val body = HashMap<String, String>()
          body["Amount"] = bitCoinFormat.dogeToDecimal(balanceText.text.toString().toBigDecimal()).toPlainString()
          body["Address"] = walletText.text.toString()
          body["secondaryPassword"] = secondaryPasswordText.text.toString()
          response = WebController.Post("user.password.validator", user.getString("token"), body).execute().get()
          if (response.getInt("code") == 200) {
            runOnUiThread {
              onSendDoge()
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

  private fun onSendDoge() {
    val doge = balanceText.text.toString().toBigDecimal()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "Withdraw"
      body["s"] = user.getString("key")
      body["Amount"] = bitCoinFormat.dogeToDecimal(doge).toPlainString()
      body["Address"] = walletText.text.toString()
      body["Totp"] = "0"
      body["Currency"] = "doge"
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        runOnUiThread {
          Toast.makeText(applicationContext, "wait until the doge balance is received", Toast.LENGTH_LONG).show()
          loading.closeDialog()
          finish()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun initScannerView() {
    scannerEngine = ZXingScannerView(this)
    scannerEngine.setAutoFocus(true)
    scannerEngine.setResultHandler(this)
    frameScanner.addView(scannerEngine)
  }

  override fun onStart() {
    super.onStart()

    intentServiceBalance = Intent(applicationContext, BackgroundServiceBalance::class.java)
    startService(intentServiceBalance)

    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverDoge, IntentFilter("net.dogearn.doge"))
  }

  override fun onPause() {
    scannerEngine.stopCamera()
    super.onPause()
  }

  override fun handleResult(rawResult: Result?) {
    if (rawResult?.text?.isNotEmpty()!!) {
      isHasCode = true
      wallet = rawResult.text.toString()
      walletText.setText(wallet)
    } else {
      isHasCode = false
    }
  }

  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
      user.setString("balanceValue", balanceValue.toPlainString())
      user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
      userBalance.text = user.getString("balanceText")
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverDoge)
    stopService(intentServiceBalance)
    super.onStop()
  }

  override fun onBackPressed() {
    stopService(intentServiceBalance)
    super.onBackPressed()
  }
}