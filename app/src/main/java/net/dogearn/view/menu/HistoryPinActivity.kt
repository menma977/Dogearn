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
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class HistoryPinActivity : AppCompatActivity() {
  private lateinit var container: LinearLayout
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history_pin)

    user = User(this)
    loading = Loading(this)

    container = findViewById(R.id.linearLayoutDataContent)
    container.removeAllViews()

    setView()
  }

  private fun setView() {
    loading.openDialog()
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val descriptionParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val totalPinParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)

    Timer().schedule(1000) {
      response = WebController.Get("pin", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        val dataGrabber = response.getJSONObject("data").getJSONArray("pinLedgers")
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
            if (dataGrabber.getJSONObject(i).getInt("debit") == 0) {
              imageIcon.setImageResource(R.drawable.output)
            } else {
              imageIcon.setImageResource(R.drawable.input)
            }
            containerLinearLayoutSub1.addView(imageIcon)
            //description in sub container 1
            val description = TextView(applicationContext)
            description.layoutParams = descriptionParams
            description.text = dataGrabber.getJSONObject(i).getString("description")
            description.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            description.gravity = Gravity.START
            containerLinearLayoutSub1.addView(description)
            //pin sub container 2
            val pin = TextView(applicationContext)
            pin.layoutParams = totalPinParams
            if (dataGrabber.getJSONObject(i).getInt("debit") == 0) {
              pin.text = "Amount: -${dataGrabber.getJSONObject(i).getString("credit")}"
              pin.setTextColor(ContextCompat.getColor(applicationContext, R.color.Danger))
            } else {
              pin.text = "Amount: +${dataGrabber.getJSONObject(i).getString("debit")}"
              pin.setTextColor(ContextCompat.getColor(applicationContext, R.color.Success))
            }
            pin.gravity = Gravity.CENTER
            containerLinearLayoutSub2.addView(pin)
            //date in sub container 2
            val date = TextView(applicationContext)
            date.layoutParams = dateParams
            date.text = dataGrabber.getJSONObject(i).getString("date")
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