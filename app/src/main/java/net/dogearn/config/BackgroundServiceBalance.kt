package net.dogearn.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.controller.DogeController
import net.dogearn.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext

/**
 * class BackgroundServiceBalance
 * @property response JSONObject
 * @property balanceValue BigDecimal
 * @property user User
 * @property bitCoinFormat BitCoinFormat
 * @property startBackgroundService Boolean
 * @property limitDepositDefault (java.math.BigDecimal..java.math.BigDecimal?)
 */
class BackgroundServiceBalance : IntentService("BackgroundServiceBalance") {
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var user: User
  private lateinit var bitCoinFormat: BitCoinFormat
  private var startBackgroundService: Boolean = false
  private var limitDepositDefault = BigDecimal(0.000000000, MathContext.DECIMAL32).setScale(8, BigDecimal.ROUND_HALF_DOWN)

  override fun onHandleIntent(intent: Intent?) {
    user = User(this)
    bitCoinFormat = BitCoinFormat()
    val body = HashMap<String, String>()
    body["a"] = "GetBalance"
    body["s"] = user.getString("key")
    body["Currency"] = "doge"
    body["Referrals"] = "0"
    body["Stats"] = "0"
    var time = System.currentTimeMillis()
    val trigger = Object()

    synchronized(trigger) {
      startBackgroundService = true
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            response = DogeController(body).execute().get()
            if (response.getInt("code") == 200) {
              balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
              privateIntent.putExtra("balanceValue", balanceValue)
              privateIntent.putExtra("balance", "${bitCoinFormat.decimalToDoge(balanceValue).toPlainString()} DOGE")

              privateIntent.action = "net.dogearn.doge"
              LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
            } else {
              trigger.wait(60000)
            }
          } else {
            break
          }
        }
      }
    }
  }

  override fun onDestroy() {
    startBackgroundService = false
    super.onDestroy()
  }
}