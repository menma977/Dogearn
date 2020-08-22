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
  private lateinit var secondaryPassword: EditText
  private lateinit var phoneText: EditText
  private lateinit var phoneConfirmText: EditText
  private lateinit var update: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_phone)

    loading = Loading(this)
    user = User(this)

    secondaryPassword = findViewById(R.id.editTextSecondaryPassword)
    phoneText = findViewById(R.id.editTextPhone)
    phoneConfirmText = findViewById(R.id.editTextPhoneConfirmation)
    update = findViewById(R.id.buttonUpdate)

    update.setOnClickListener {
      onUpdatePhone()
    }
  }

  private fun onUpdatePhone() {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["phone"] = phoneText.text.toString()
      body["phoneConfirm"] = phoneConfirmText.text.toString()
      body["secondaryPassword"] = secondaryPassword.text.toString()
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