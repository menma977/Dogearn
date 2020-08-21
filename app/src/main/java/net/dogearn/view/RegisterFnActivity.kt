package net.dogearn.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class RegisterFnActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var response: JSONObject
  private lateinit var loading: Loading
  private lateinit var sponsor: EditText
  private lateinit var phone: EditText
  private lateinit var email: EditText
  private lateinit var password: EditText
  private lateinit var passwordConfirmation: EditText
  private lateinit var transactionPassword: EditText
  private lateinit var confirmTransactionPassword: EditText
  private lateinit var register: Button
  private lateinit var message: TextView
  private lateinit var login: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register_fn)

    loading = Loading(this)

    sponsor = findViewById(R.id.editTextTextSponsor)
    phone = findViewById(R.id.editTextTextPhone)
    email = findViewById(R.id.editTextTextEmail)
    password = findViewById(R.id.editTextPassword)
    passwordConfirmation = findViewById(R.id.editTextPasswordConfirmation)
    transactionPassword = findViewById(R.id.editTextTextTransactionPassword)
    confirmTransactionPassword = findViewById(R.id.editTextTextConfirmTransactionPassword)
    register = findViewById(R.id.buttonRegister)
    message = findViewById(R.id.textViewMessage)
    login = findViewById(R.id.textViewLogin)

    register.setOnClickListener {
      onRegister()
    }

    login.setOnClickListener {
      goTo = Intent(this, LoginActivity::class.java)
      startActivity(goTo)
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }

  private fun onRegister() {
    loading.openDialog()
    if (sponsor.text.isEmpty()) {
      Toast.makeText(this, "sponsors cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      sponsor.requestFocus()
    } else if (phone.text.isEmpty()) {
      Toast.makeText(this, "whatsapp number cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      phone.requestFocus()
    } else if (phone.text.toString().contains("+") || phone.text.toString().contains(".") || phone.text.toString().contains(",") || phone.text.toString().contains("-")) {
      Toast.makeText(this, "whatsapp numbers must not use symbols", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      phone.requestFocus()
    } else if (password.text.isEmpty()) {
      Toast.makeText(this, "password cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      password.requestFocus()
    } else if (passwordConfirmation.text.isEmpty()) {
      Toast.makeText(this, "password confirmation cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      passwordConfirmation.requestFocus()
    } else if (passwordConfirmation.text.toString() != password.text.toString()) {
      Toast.makeText(this, "password you entered does not match", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      passwordConfirmation.requestFocus()
    } else if (transactionPassword.text.isEmpty()) {
      Toast.makeText(this, "transaction password cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      transactionPassword.requestFocus()
    } else if (confirmTransactionPassword.text.isEmpty()) {
      Toast.makeText(this, "confirm transaction password confirmation cannot be empty", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      confirmTransactionPassword.requestFocus()
    } else if (transactionPassword.text.toString() != confirmTransactionPassword.text.toString()) {
      Toast.makeText(this, "transaction password you entered does not match", Toast.LENGTH_SHORT).show()
      loading.closeDialog()
      transactionPassword.requestFocus()
    } else {
      Timer().schedule(100) {
        val body = HashMap<String, String>()
        body["sponsor"] = sponsor.text.toString()
        body["email"] = email.text.toString()
        body["phone"] = phone.text.toString()
        body["password"] = password.text.toString()
        body["password_confirmation"] = passwordConfirmation.text.toString()
        body["transaction_password"] = transactionPassword.text.toString()
        body["transaction_password_confirmation"] = confirmTransactionPassword.text.toString()
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
}