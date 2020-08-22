package net.dogearn.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.R
import net.dogearn.model.Setting
import net.dogearn.model.User
import net.dogearn.view.NavigationActivity

class InfoFragment : Fragment() {
  private lateinit var parentActivity: NavigationActivity
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var imageBack: ImageView
  private lateinit var textViewEmail: TextView
  private lateinit var textViewPhone: TextView
  private lateinit var textViewBalance: TextView
  private lateinit var textViewWallet: TextView
  private lateinit var textViewPin: TextView
  private lateinit var textViewGrade: TextView
  private lateinit var textViewSponsor: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_info, container, false)

    parentActivity = activity as NavigationActivity

    user = User(parentActivity)
    setting = Setting(parentActivity)

    imageBack = root.findViewById(R.id.imageViewBack)
    textViewEmail = root.findViewById(R.id.textViewEmail)
    textViewPhone = root.findViewById(R.id.textViewPhone)
    textViewBalance = root.findViewById(R.id.textViewBalance)
    textViewWallet = root.findViewById(R.id.textViewWallet)
    textViewPin = root.findViewById(R.id.textViewPin)
    textViewGrade = root.findViewById(R.id.textViewGrade)
    textViewSponsor = root.findViewById(R.id.textViewSponsor)


    textViewEmail.text = user.getString("email")
    textViewPhone.text = user.getString("phone")
    textViewBalance.text = user.getString("balanceText")
    textViewWallet.text = user.getString("wallet")
    textViewPin.text = user.getInteger("pin").toString()
    textViewGrade.text = user.getString("gradeLevel")
    textViewSponsor.text = user.getString("phoneSponsor")

    imageBack.setOnClickListener {
      parentActivity.supportFragmentManager.popBackStack()
    }

    textViewSponsor.setOnClickListener {
      val url = "https://api.whatsapp.com/send?phone=${user.getString("phoneSponsor")}"
      val goTo = Intent(Intent.ACTION_VIEW)
      goTo.data = Uri.parse(url)
      startActivity(goTo)
    }

    return root
  }

  /** start Broadcast */
  override fun onResume() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverWeb, IntentFilter("net.dogearn.web"))
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverDoge, IntentFilter("net.dogearn.doge"))
    super.onResume()
  }

  /** stop Broadcast */
  override fun onDestroy() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).unregisterReceiver(broadcastReceiverWeb)
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).unregisterReceiver(broadcastReceiverDoge)
    super.onDestroy()
  }

  /** declaration broadcastReceiver */
  private var broadcastReceiverWeb: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      textViewEmail.text = user.getString("email")
      textViewPhone.text = user.getString("phone")
      textViewBalance.text = user.getString("balanceText")
      textViewWallet.text = user.getString("wallet")
      textViewPin.text = user.getInteger("pin").toString()
      textViewGrade.text = user.getString("gradeLevel")
      textViewSponsor.text = user.getString("phoneSponsor")
    }
  }
  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      textViewBalance.text = user.getString("balanceText")
    }
  }
}