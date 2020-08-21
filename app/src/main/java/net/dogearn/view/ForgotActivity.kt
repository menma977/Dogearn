package net.dogearn.view

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

class ForgotActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var number1: TextView
  private lateinit var number2: TextView
  private lateinit var result: EditText
  private lateinit var email: EditText
  private lateinit var send: Button
  private var numberOne = 0
  private var numberTow = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forgot)

    loading = Loading(this)

    number1 = findViewById(R.id.textViewNumber1)
    number2 = findViewById(R.id.textViewNumber2)
    result = findViewById(R.id.editTextCount)
    email = findViewById(R.id.editTextTextEmailAddress)
    send = findViewById(R.id.buttonSend)

    numberOne = (1..9).random()
    numberTow = (1..9).random()

    number1.text = numberOne.toString()
    number2.text = numberTow.toString()

    send.setOnClickListener {
      val total = numberOne + numberTow
      if (total.toString() == result.text.toString()) {
        sendEmail()
      } else {
        Toast.makeText(applicationContext, "calculation you entered is wrong", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun sendEmail() {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["email"] = email.text.toString()
      response = WebController.Post("send.email", "", body).execute().get()
      if (response.getInt("code") == 200) {
        runOnUiThread {
          Toast.makeText(applicationContext, "wait a few moments to receive the email", Toast.LENGTH_LONG).show()

          numberOne = (1..9).random()
          numberTow = (1..9).random()

          number1.text = numberOne.toString()
          number2.text = numberTow.toString()

          result.setText("")
          email.setText("")
          result.requestFocus()
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }
}