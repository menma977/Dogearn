package net.dogearn.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
  private lateinit var balanceText: EditText
  private lateinit var sendDoge: Button
  private lateinit var walletText: TextView
  private lateinit var intentServiceBalance: Intent
  private lateinit var balanceValue: BigDecimal
  private var isHasCode = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_send_balance)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    walletText = findViewById(R.id.textViewWallet)
    frameScanner = findViewById(R.id.frameLayoutScanner)
    balanceText = findViewById(R.id.editTextBalance)
    sendDoge = findViewById(R.id.buttonSend)

    initScannerView()

    frameScanner.setOnClickListener {
      wallet = ""
      walletText.text = ""
      balanceText.setText("")
      sendDoge.visibility = Button.GONE
      scannerEngine.resumeCameraPreview(this)
    }

    sendDoge.setOnClickListener {
      onSendDoge()
    }

    sendDoge.visibility = Button.GONE
    balanceText.hint = user.getString("balanceText")
    balanceValue = user.getString("balanceValue").toBigDecimal()
  }

  private fun onSendDoge() {
    val balanceToSend = balanceText.text.toString().replace(".", "").replace(",", "")
    if (balanceToSend.toBigDecimal() > balanceValue) {
      Toast.makeText(this, "your balance is insufficient", Toast.LENGTH_SHORT).show()
    } else {
      loading.openDialog()
      Timer().schedule(1000) {
        val body = HashMap<String, String>()
        body["a"] = "Withdraw"
        body["s"] = user.getString("key")
        body["Amount"] = balanceToSend
        body["Address"] = wallet
        body["Totp"] = "0"
        body["Currency"] = "doge"
        response = DogeController(body).execute().get()
        if (response.getInt("code") == 200) {
          runOnUiThread {
            try {
              Toast.makeText(applicationContext, "wait until the doge balance is received", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
              Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
            wallet = ""
            walletText.text = ""
            balanceText.setText("")
            sendDoge.visibility = Button.GONE
            loading.closeDialog()
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

  private fun initScannerView() {
    scannerEngine = ZXingScannerView(this)
    scannerEngine.setAutoFocus(true)
    scannerEngine.setResultHandler(this)
    frameScanner.addView(scannerEngine)
  }

  override fun onStart() {
    scannerEngine.startCamera()
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
      walletText.text = wallet
      sendDoge.visibility = Button.VISIBLE
    } else {
      isHasCode = false
      sendDoge.visibility = Button.GONE
    }
  }

  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
      user.setString("balanceValue", balanceValue.toPlainString())
      user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
      balanceText.hint = user.getString("balanceText")
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