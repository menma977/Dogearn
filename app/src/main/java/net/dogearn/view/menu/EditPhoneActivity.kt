package net.dogearn.view.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class EditPhoneActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var user: User
  private lateinit var response: JSONObject
  private lateinit var code: String
  private lateinit var waitTimeText: TextView
  private lateinit var sendCodeToEmail: ImageButton
  private lateinit var codeText: EditText
  private lateinit var phoneText: EditText
  private lateinit var phoneConfirmText: EditText
  private lateinit var update: Button
  private var time: Long = 0
  private var delta: Long = 0
  private var waitTarget: Long = 30000
  private var switch = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_phone)

    loading = Loading(this)
    user = User(this)
    time = System.currentTimeMillis()

    waitTimeText = findViewById(R.id.textViewWaitTime)
    sendCodeToEmail = findViewById(R.id.imageButtonSendCodeToEmail)
    codeText = findViewById(R.id.editTextCode)
    phoneText = findViewById(R.id.editTextPhone)
    phoneConfirmText = findViewById(R.id.editTextPhoneConfirmation)
    update = findViewById(R.id.buttonUpdate)

    update.visibility = Button.GONE

    sendCodeToEmail.setOnClickListener {
      onSendEmail()
    }

    update.setOnClickListener {
      onUpdatePhone()
    }

    Timer().schedule(0, 1000) {
      delta = if (switch) {
        System.currentTimeMillis() - time
      } else {
        System.currentTimeMillis()
      }

      if (delta > waitTarget) {
        runOnUiThread {
          sendCodeToEmail.visibility = ImageButton.VISIBLE
          waitTimeText.text = ""
        }
      } else {
        runOnUiThread {
          sendCodeToEmail.visibility = ImageButton.GONE
          waitTimeText.text = "wait ${delta / 1000} seconds to 30 seconds"
        }
      }
    }
  }

  private fun onSendEmail() {
    if (delta > waitTarget) {
      switch = true
      loading.openDialog()
      Timer().schedule(1000) {
        time = System.currentTimeMillis()
        response = WebController.Get("user.edit", user.getString("token")).execute().get()
        runOnUiThread {
          if (response.getInt("code") == 200) {
            try {
              code = response.getJSONObject("data").getString("code")
              runOnUiThread {
                update.visibility = Button.VISIBLE
              }
            } catch (e: Exception) {
              Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
          } else {
            Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          }
          loading.closeDialog()
        }
      }
    } else {
      Toast.makeText(this, "wait up to ${delta / 1000} seconds", Toast.LENGTH_SHORT).show()
    }
  }

  private fun onUpdatePhone() {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["phone"] = phoneText.text.toString()
      body["phoneConfirm"] = phoneConfirmText.text.toString()
      response = WebController.Post("user.update", user.getString("token"), body).execute().get()
      if (response.getInt("code") == 200) {
        try {
          runOnUiThread {
            Toast.makeText(applicationContext, response.getJSONObject("data").getString("message"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
            finish()
          }
        } catch (e: Exception) {
          runOnUiThread {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
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