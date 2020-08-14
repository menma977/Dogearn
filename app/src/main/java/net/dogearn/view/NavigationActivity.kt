package net.dogearn.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.dogearn.R
import net.dogearn.view.fragment.HomeFragment

class NavigationActivity : AppCompatActivity() {
  private lateinit var navView: BottomNavigationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    navView = findViewById(R.id.nav_view)
    navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    val fragment = HomeFragment()
    addFragment(fragment)
  }

  private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
    when (item.itemId) {
      R.id.navigation_home -> {
        val fragment = HomeFragment()
        addFragment(fragment)
        return@OnNavigationItemSelectedListener true
      }
    }
    false
  }

  @SuppressLint("PrivateResource")
  private fun addFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().setCustomAnimations(
      R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out
    ).replace(R.id.contentFragment, fragment, fragment.javaClass.simpleName).commit()
  }
}