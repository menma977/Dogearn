package net.dogearn.view.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import net.dogearn.R
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.DogeController
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class HistoryOutActivity : AppCompatActivity() {
  private lateinit var container: LinearLayout
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history_out)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    container = findViewById(R.id.linearLayoutDataContent)
    container.removeAllViews()

    setView()
  }

  private fun setView() {
    loading.openDialog()
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val addressParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val balanceParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)

    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "GetWithdrawals"
      body["s"] = user.getString("key")
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        val dataGrabber = response.getJSONObject("data").getJSONArray("Transfers")
        val length = if (dataGrabber.length() > 50) {
          50
        } else {
          dataGrabber.length() - 1
        }
        for (i in length downTo 0) {
          runOnUiThread {
            //body container
            val containerLinearLayout = LinearLayout(applicationContext)
            containerLinearLayout.layoutParams = linearLayoutParams
            containerLinearLayout.gravity = Gravity.CENTER
            containerLinearLayout.orientation = LinearLayout.VERTICAL
            containerLinearLayout.setBackgroundResource(R.drawable.card_default)
            containerLinearLayout.setPadding(10, 10, 10, 10)
            containerLinearLayout.elevation = 20F
            //sub container 1
            val containerLinearLayoutSub1 = LinearLayout(applicationContext)
            containerLinearLayoutSub1.layoutParams = linearLayoutParams
            containerLinearLayoutSub1.gravity = Gravity.CENTER
            containerLinearLayoutSub1.orientation = LinearLayout.HORIZONTAL
            //sub container 2
            val containerLinearLayoutSub2 = LinearLayout(applicationContext)
            containerLinearLayoutSub2.layoutParams = linearLayoutParams
            containerLinearLayoutSub2.gravity = Gravity.CENTER
            containerLinearLayoutSub2.orientation = LinearLayout.HORIZONTAL
            //image in sub container 1
            val imageIcon = ImageView(applicationContext)
            imageIcon.layoutParams = iconImageParams
            imageIcon.setImageResource(R.drawable.input)
            containerLinearLayoutSub1.addView(imageIcon)
            //description in sub container 1
            val address = TextView(applicationContext)
            address.layoutParams = addressParams
            address.text = dataGrabber.getJSONObject(i).getString("Address").replace("XFER", "Internal EARN")
            address.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            address.gravity = Gravity.START
            containerLinearLayoutSub1.addView(address)
            //pin sub container 2
            val balance = TextView(applicationContext)
            balance.layoutParams = balanceParams
            balance.text = "Amount: -${bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("Value").toBigDecimal()).toPlainString()} DOGE"
            balance.setTextColor(ContextCompat.getColor(applicationContext, R.color.Danger))
            balance.gravity = Gravity.CENTER
            containerLinearLayoutSub2.addView(balance)
            //date in sub container 2
            val date = TextView(applicationContext)
            date.layoutParams = dateParams
            date.text = dataGrabber.getJSONObject(i).getString("Completed")
            date.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            date.gravity = Gravity.CENTER
            containerLinearLayoutSub2.addView(date)
            //set sub container in main container
            containerLinearLayout.addView(containerLinearLayoutSub1)
            containerLinearLayout.addView(containerLinearLayoutSub2)
            //set container to parent container
            container.addView(containerLinearLayout)
            val wrapLine = View(applicationContext)
            wrapLine.layoutParams = line
            wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.Dark))
            container.addView(wrapLine)
          }
        }
        loading.closeDialog()
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }
}