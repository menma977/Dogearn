package net.dogearn.view.menu

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.Url
import net.dogearn.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class NetworkActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var user: User
  private lateinit var response: JSONObject
  private lateinit var webView: WebView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_network)

    loading = Loading(this)
    user = User(this)

    webView = findViewById(R.id.webViewContent)

    println(user.getString("token"))

    loadHtml()
  }

  private fun loadHtml() {
    loading.openDialog()
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["Authorization"] = "Bearer ${user.getString("token")}"
      response = WebController.PostWebView("binary.api", user.getString("token"), body).execute().get()
      if (response.getInt("code") == 200) {
        runOnUiThread {
          webView.settings.javaScriptEnabled = true
          webView.loadData(response.getString("data"), "text/html", "UTF-8")
          webView.loadDataWithBaseURL(Url.web()+"binary/api", response.getString("data"), "text/html", "UTF-8", null)
          loading.closeDialog()
        }
      }
    }
  }
}