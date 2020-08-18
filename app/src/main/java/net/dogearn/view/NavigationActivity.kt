package net.dogearn.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.dogearn.R
import net.dogearn.view.fragment.HomeFragment

class NavigationActivity : AppCompatActivity() {
  private lateinit var home: ImageButton
  private lateinit var dogeChain: ImageButton
  private lateinit var setting: ImageButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)
    //navigation
    home = findViewById(R.id.buttonHome)
    dogeChain = findViewById(R.id.buttonDogeChain)
    setting = findViewById(R.id.buttonSetting)
    //set Default Fragment
    val fragment = HomeFragment()
    addFragment(fragment)

    setNavigation()
  }

  private fun setNavigation() {
    home.setOnClickListener {
      val fragment = HomeFragment()
      addFragment(fragment)
    }

    setting.setOnClickListener {
      val fragment = HomeFragment()
      addFragment(fragment)
    }
  }

  @SuppressLint("PrivateResource")
  private fun addFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().setCustomAnimations(
      R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out
    ).replace(R.id.contentFragment, fragment, fragment.javaClass.simpleName).commit()
  }
}