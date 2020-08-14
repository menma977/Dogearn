package net.dogearn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.dogearn.view.NavigationActivity

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val goTo = Intent(this, NavigationActivity::class.java)

    startActivity(goTo)
  }
}