package net.dogearn.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.DogeController
import net.dogearn.controller.WebController
import net.dogearn.model.Setting
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var response: JSONObject
  private lateinit var loading: Loading
  private lateinit var version: TextView
  private lateinit var username: EditText
  private lateinit var password: EditText
  private lateinit var login: Button
  private lateinit var register: TextView
  private lateinit var forgotPassword: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    user = User(this)
    setting = Setting(this)
    loading = Loading(this)

    version = findViewById(R.id.textViewVersion)
    username = findViewById(R.id.editTextTextUsername)
    password = findViewById(R.id.editTextPassword)
    login = findViewById(R.id.buttonLogin)
    register = findViewById(R.id.textViewRegister)
    forgotPassword = findViewById(R.id.textViewForgotPassword)

    login.setOnClickListener {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
          this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
      ) {
        onLogin()
      } else {
        doRequestPermission()
      }
    }

    register.setOnClickListener {
      goTo = Intent(this, RegisterFnActivity::class.java)
      startActivity(goTo)
    }

    forgotPassword.setOnClickListener {
      goTo = Intent(this, ForgotActivity::class.java)
      startActivity(goTo)
    }
  }

  private fun onLogin() {
    loading.openDialog()
    when {
      username.text.isEmpty() -> {
        loading.closeDialog()
        Toast.makeText(this, "the whatsapp number or e-mail that you enter cannot be empty", Toast.LENGTH_SHORT).show()
      }
      password.text.isEmpty() -> {
        loading.closeDialog()
        Toast.makeText(this, "password cannot be empty", Toast.LENGTH_SHORT).show()
      }
      else -> {
        Timer().schedule(100) {
          val body = HashMap<String, String>()
          body["phone"] = username.text.toString()
          body["password"] = password.text.toString()
          response = WebController.Post("login", "", body).execute().get()
          if (response.getInt("code") == 200) {
            user.setString("token", response.getJSONObject("data").getString("token"))
            user.setString("wallet", response.getJSONObject("data").getString("wallet"))
            user.setString("account_cookie", response.getJSONObject("data").getString("account_cookie"))
            user.setString("phone", response.getJSONObject("data").getString("phone"))
            user.setString("usernameDoge", response.getJSONObject("data").getString("username"))
            user.setString("passwordDoge", response.getJSONObject("data").getString("password"))
            loginDoge()
          } else {
            runOnUiThread {
              loading.closeDialog()
              Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }
  }

  private fun loginDoge() {
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "Login"
      body["key"] = "1b4755ced78e4d91bce9128b9a053cad"
      body["username"] = user.getString("usernameDoge")
      body["password"] = user.getString("passwordDoge")
      body["Totp"] = "''"
      Timer().schedule(100) {
        response = DogeController(body).execute().get()
        if (response["code"] == 200) {
          user.setString("key", response.getJSONObject("data")["SessionCookie"].toString())
          goTo = Intent(applicationContext, NavigationActivity::class.java)
          runOnUiThread {
            startActivity(goTo)
            finishAffinity()
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }
      }
    }
  }

  private fun doRequestPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 100)
      }
    }
  }
}