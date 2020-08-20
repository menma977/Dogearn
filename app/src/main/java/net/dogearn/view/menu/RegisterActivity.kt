package net.dogearn.view.menu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class RegisterActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var response: JSONObject
  private lateinit var loading: Loading
  private lateinit var phone: EditText
  private lateinit var email: EditText
  private lateinit var register: Button
  private lateinit var message: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)

    user = User(this)
    loading = Loading(this)

    phone = findViewById(R.id.editTextTextPhone)
    email = findViewById(R.id.editTextTextEmail)
    register = findViewById(R.id.buttonRegister)
    message = findViewById(R.id.textViewMessage)

    phone.setText("6282257960078")
    email.setText("putra.lvika@gmail.com")

    register.setOnClickListener {
      onRegister()
    }
  }

  private fun onRegister() {
    loading.openDialog()
    if (phone.text.isEmpty()) {
      Toast.makeText(this, "whatsapp number cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      phone.requestFocus()
    } else if (phone.text.toString().contains("+") || phone.text.toString().contains(".") || phone.text.toString().contains(",") || phone.text.toString().contains("-")) {
      Toast.makeText(this, "whatsapp numbers must not use symbols", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      phone.requestFocus()
    } else {
      Timer().schedule(100) {
        val passwordValue = randomPassword(7)
        val passwordTransactionValue = (1000..9999).random().toString()
        val body = HashMap<String, String>()
        body["sponsor"] = user.getString("phone")
        body["email"] = email.text.toString()
        body["phone"] = phone.text.toString()
        body["password"] = passwordValue
        body["password_confirmation"] = passwordValue
        body["transaction_password"] = passwordTransactionValue
        body["transaction_password_confirmation"] = passwordTransactionValue
        response = WebController.Post("store", "", body).execute().get()
        if (response.getInt("code") == 200) {
          runOnUiThread {
            Toast.makeText(applicationContext, response.getJSONObject("data").getString("message"), Toast.LENGTH_LONG).show()
            message.text = response.getJSONObject("data").getString("message")
            loading.closeDialog()
            finish()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
            message.text = ""
            loading.closeDialog()
          }
        }
      }
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }

  private fun randomPassword(size: Int): String {
    val source = "0123456789dogearn"
    val random = Random()
    val stringBuilder = StringBuilder(size)
    for (i in 0 until size) {
      stringBuilder.append(source[random.nextInt(source.length)])
    }
    return stringBuilder.toString()
  }
}