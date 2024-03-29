package net.dogearn.view.fragmentOld

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.MainActivity
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.controller.WebController
import net.dogearn.model.Setting
import net.dogearn.model.User
import net.dogearn.view.NavigationOldActivity
import net.dogearn.view.menu.EditPasswordActivity
import net.dogearn.view.menu.EditPhoneActivity
import net.dogearn.view.menu.EditSecondaryPasswordActivity
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class SettingOldFragment : Fragment() {
  private lateinit var loading: Loading
  private lateinit var parentActivity: NavigationOldActivity
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var goTo: Intent
  private lateinit var imageBack: ImageView
  private lateinit var logout: LinearLayout
  private lateinit var response: JSONObject
  private lateinit var editPassword: LinearLayout
  private lateinit var editSecondaryPassword: LinearLayout
  private lateinit var editPhone: LinearLayout

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_setting_old, container, false)

    parentActivity = activity as NavigationOldActivity

    loading = Loading(parentActivity)
    user = User(parentActivity)
    setting = Setting(parentActivity)

    imageBack = root.findViewById(R.id.imageViewBack)
    logout = root.findViewById(R.id.linearLayoutLogout)
    editPassword = root.findViewById(R.id.linearLayoutEditPassword)
    editSecondaryPassword = root.findViewById(R.id.linearLayoutEditSecondaryPassword)
    editPhone = root.findViewById(R.id.linearLayoutEditPhone)

    editPassword.setOnClickListener {
      goTo = Intent(parentActivity, EditPasswordActivity::class.java)
      startActivity(goTo)
    }

    editSecondaryPassword.setOnClickListener {
      goTo = Intent(parentActivity, EditSecondaryPasswordActivity::class.java)
      startActivity(goTo)
    }

    editPhone.setOnClickListener {
      goTo = Intent(parentActivity, EditPhoneActivity::class.java)
      startActivity(goTo)
    }

    logout.setOnClickListener {
      onLogout()
    }

    imageBack.setOnClickListener {
      parentActivity.supportFragmentManager.popBackStack()
    }

    return root
  }

  private fun onLogout() {
    loading.openDialog()
    Timer().schedule(100) {
      response = WebController.Get("user.logout", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        user.clear()
        setting.clear()
        goTo = Intent(parentActivity.applicationContext, MainActivity::class.java)
        loading.closeDialog()
        startActivity(goTo)
        parentActivity.finishAffinity()
      } else {
        parentActivity.runOnUiThread {
          Toast.makeText(parentActivity.applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  /** start Broadcast */
  override fun onResume() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverWeb, IntentFilter("net.dogearn.web"))
    super.onResume()
  }

  /** stop Broadcast */
  override fun onDestroy() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).unregisterReceiver(broadcastReceiverWeb)
    super.onDestroy()
  }

  /** declaration broadcastReceiver */
  private var broadcastReceiverWeb: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

    }
  }
}