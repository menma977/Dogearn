package net.dogearn.view.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import net.dogearn.R
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.controller.DogeController
import net.dogearn.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class HistoryInActivity : AppCompatActivity() {
  private lateinit var containerExternal: LinearLayout
  private lateinit var containerInternal: LinearLayout
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history_in)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    containerExternal = findViewById(R.id.linearLayoutDataContentExternal)
    containerExternal.removeAllViews()
    containerInternal = findViewById(R.id.linearLayoutDataContent)
    containerInternal.removeAllViews()

    setView()
  }

  private fun setView() {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "GetDeposits"
      body["s"] = user.getString("key")
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        val dataGrabberExternal = response.getJSONObject("data").getJSONArray("Deposits")
        val dataGrabberInternal = response.getJSONObject("data").getJSONArray("Transfers")
        val lengthExternal = if (dataGrabberExternal.length() > 50) {
          50
        } else {
          dataGrabberExternal.length() - 1
        }
        val lengthInternal = if (dataGrabberInternal.length() > 50) {
          50
        } else {
          dataGrabberInternal.length() - 1
        }

        setExternalView(lengthExternal, dataGrabberExternal)
        setInternalView(lengthInternal, dataGrabberInternal)

        loading.closeDialog()
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun setInternalView(length: Int, dataGrabberInternal: JSONArray) {
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val addressParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val balanceParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)

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
        address.text = dataGrabberInternal.getJSONObject(i).getString("Address").replace("XFER", "Internal EARN")
        address.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        address.gravity = Gravity.START
        containerLinearLayoutSub1.addView(address)
        //pin sub container 2
        val balance = TextView(applicationContext)
        balance.layoutParams = balanceParams
        balance.text = "Amount: +${bitCoinFormat.decimalToDoge(dataGrabberInternal.getJSONObject(i).getString("Value").toBigDecimal()).toPlainString()} DOGE"
        balance.setTextColor(ContextCompat.getColor(applicationContext, R.color.Success))
        balance.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(balance)
        //date in sub container 2
        val date = TextView(applicationContext)
        date.layoutParams = dateParams
        date.text = dataGrabberInternal.getJSONObject(i).getString("Date")
        date.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        date.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(date)
        //set sub container in main container
        containerLinearLayout.addView(containerLinearLayoutSub1)
        containerLinearLayout.addView(containerLinearLayoutSub2)
        //set container to parent container
        containerInternal.addView(containerLinearLayout)
        val wrapLine = View(applicationContext)
        wrapLine.layoutParams = line
        wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.Dark))
        containerInternal.addView(wrapLine)
      }
    }
  }

  private fun setExternalView(length: Int, dataGrabberInternal: JSONArray) {
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val addressParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val balanceParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)
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
        address.text = dataGrabberInternal.getJSONObject(i).getString("Address") + " | " + dataGrabberInternal.getJSONObject(i).getString("TransactionHash")
        address.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        address.gravity = Gravity.START
        address.setOnClickListener {
          val uri = "https://dogechain.info/tx/${dataGrabberInternal.getJSONObject(i).getString("TransactionHash")}"
          val goTo = Intent(Intent.ACTION_VIEW)
          goTo.data = Uri.parse(uri)
          startActivity(goTo)
        }
        containerLinearLayoutSub1.addView(address)
        //pin sub container 2
        val balance = TextView(applicationContext)
        balance.layoutParams = balanceParams
        balance.text = "Amount: +${
          bitCoinFormat.decimalToDoge(dataGrabberInternal.getJSONObject(i).getString("Value").toBigDecimal()).toPlainString()
        } DOGE"
        balance.setTextColor(ContextCompat.getColor(applicationContext, R.color.Success))
        balance.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(balance)
        //date in sub container 2
        val date = TextView(applicationContext)
        date.layoutParams = dateParams
        date.text = dataGrabberInternal.getJSONObject(i).getString("Date")
        date.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        date.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(date)
        //set sub container in main container
        containerLinearLayout.addView(containerLinearLayoutSub1)
        containerLinearLayout.addView(containerLinearLayoutSub2)
        //set container to parent container
        containerExternal.addView(containerLinearLayout)
        val wrapLine = View(applicationContext)
        wrapLine.layoutParams = line
        wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.Dark))
        containerExternal.addView(wrapLine)
      }
    }
  }
}