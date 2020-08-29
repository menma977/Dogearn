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
import net.dogearn.config.BackgroundGetDataUser
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class SendPinActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var response: JSONObject
  private lateinit var responseArray: JSONArray
  private lateinit var frameScanner: FrameLayout
  private lateinit var scannerEngine: ZXingScannerView
  private lateinit var wallet: String
  private lateinit var pinTextView: TextView
  private lateinit var sendDoge: Button
  private lateinit var walletText: EditText
  private lateinit var pinAmountText: EditText
  private lateinit var intentServiceGetDataUser: Intent
  private lateinit var secondaryPassword: TextView
  private var isHasCode = false
  private var isStart = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_send_pin)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    pinTextView = findViewById(R.id.textViewPin)
    walletText = findViewById(R.id.editTextWallet)
    frameScanner = findViewById(R.id.frameLayoutScanner)
    sendDoge = findViewById(R.id.buttonSend)
    secondaryPassword = findViewById(R.id.editTextSecondaryPassword)
    pinAmountText = findViewById(R.id.editTextAmount)

    initScannerView()

    frameScanner.setOnClickListener {
      if (isStart) {
        scannerEngine.startCamera()
        isStart = false
      }
    }

    sendDoge.setOnClickListener {
      onSendPin()
    }

    pinTextView.text = user.getInteger("pin").toString()
  }

  private fun getPin() {
    Timer().schedule(1000) {
      response = WebController.Get("pin.create", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        runOnUiThread {
          responseArray = response.getJSONObject("data").getJSONArray("walletList")
          println(responseArray)
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
          finish()
        }
      }
    }
  }

  private fun onSendPin() {
    when {
      !responseArray.toString().contains(walletText.text.toString()) -> {
        Toast.makeText(this, "cross line is not allowed", Toast.LENGTH_SHORT).show()
      }
      pinTextView.text.isEmpty() -> {
        Toast.makeText(this, "pin cant not be empty", Toast.LENGTH_SHORT).show()
      }
      else -> {
        loading.openDialog()
        Timer().schedule(1000) {
          val body = HashMap<String, String>()
          body["wallet"] = walletText.text.toString()
          body["pin"] = pinAmountText.text.toString()
          body["secondaryPassword"] = secondaryPassword.text.toString()
          response = WebController.Post("pin.store", user.getString("token"), body).execute().get()
          if (response.getInt("code") == 200) {
            runOnUiThread {
              Toast.makeText(applicationContext, "Transfer Pin success", Toast.LENGTH_LONG).show()
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
    loading.openDialog()
    intentServiceGetDataUser = Intent(applicationContext, BackgroundGetDataUser::class.java)
    startService(intentServiceGetDataUser)

    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverWeb, IntentFilter("net.dogearn.web"))

    getPin()
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

  private var broadcastReceiverWeb: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      pinTextView.text = user.getInteger("pin").toString()
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverWeb)
    stopService(intentServiceGetDataUser)
    super.onStop()
  }

  override fun onBackPressed() {
    stopService(intentServiceGetDataUser)
    super.onBackPressed()
  }
}