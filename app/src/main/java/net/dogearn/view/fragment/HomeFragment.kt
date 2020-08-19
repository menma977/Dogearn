package net.dogearn.view.fragment

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.R
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.model.User
import net.dogearn.view.NavigationActivity
import java.math.BigDecimal

class HomeFragment : Fragment() {
  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var loading: Loading
  private lateinit var parentActivity: NavigationActivity
  private lateinit var user: User
  private lateinit var goTo: Intent
  private lateinit var intentService: Intent
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var balance: TextView
  private lateinit var pin: TextView
  private lateinit var grade: TextView
  private lateinit var balanceRemaining: TextView
  private lateinit var targetBalance: TextView
  private lateinit var progressBar: ProgressBar

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)

    balance = root.findViewById(R.id.textViewBalance)
    pin = root.findViewById(R.id.textViewTotalPin)
    grade = root.findViewById(R.id.textViewGrade)
    balanceRemaining = root.findViewById(R.id.textViewRemainingBalance)
    targetBalance = root.findViewById(R.id.textViewTargetBalance)
    progressBar = root.findViewById(R.id.progressBar)

    parentActivity = activity as NavigationActivity

    loading = Loading(parentActivity)
    user = User(parentActivity)
    bitCoinFormat = BitCoinFormat()
    val gradeValue = bitCoinFormat.decimalToDoge(user.getString("grade").toBigDecimal())
    val gradeProgressValue = bitCoinFormat.decimalToDoge(user.getString("progressGrade").toBigDecimal())

    balanceRemaining.text = gradeProgressValue.toPlainString()
    balanceRemaining.text = gradeValue.toPlainString()

    progressBar.max = gradeValue.toInt() - gradeProgressValue.toInt()
    progressBar.progress = gradeProgressValue.toInt()
    balance.text = user.getString("balanceText")
    pin.text = user.getInteger("pin").toString()
    grade.text = user.getString("gradeLevel")

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
      val pinText = intent.getIntExtra("pin", 0)
      val gradeText = intent.getStringExtra("gradeLevel")
      pin.text = pinText.toString()
      grade.text = gradeText
    }
  }
  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance.text = intent.getSerializableExtra("balance") as String
    }
  }
}