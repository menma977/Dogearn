package net.dogearn.view.menu

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class EditPasswordActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var user: User
  private lateinit var response: JSONObject
  private lateinit var secondaryPassword: EditText
  private lateinit var passwordText: EditText
  private lateinit var passwordConfirmText: EditText
  private lateinit var update: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_password)

    loading = Loading(this)
    user = User(this)

    secondaryPassword = findViewById(R.id.editTextSecondaryPassword)
    passwordText = findViewById(R.id.editTextPassword)
    passwordConfirmText = findViewById(R.id.editTextPasswordConfirmation)
    update = findViewById(R.id.buttonUpdate)

    update.setOnClickListener {
      onUpdatePassword()
    }
  }

  private fun onUpdatePassword() {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["password"] = passwordText.text.toString()
      body["password_confirmation"] = passwordConfirmText.text.toString()
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