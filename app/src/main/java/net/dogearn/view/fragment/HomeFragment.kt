package net.dogearn.view.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.dogearn.R
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.model.User
import net.dogearn.view.NavigationActivity
import net.dogearn.view.menu.*
import java.math.BigDecimal

class HomeFragment : Fragment() {
  private lateinit var loading: Loading
  private lateinit var parentActivity: NavigationActivity
  private lateinit var user: User
  private lateinit var goTo: Intent
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var imageDoge: ImageView
  private lateinit var balance: TextView
  private lateinit var pin: TextView
  private lateinit var grade: TextView
  private lateinit var balanceRemaining: TextView
  private lateinit var targetBalance: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var balanceValue: BigDecimal
  private lateinit var sendBalance: ImageButton
  private lateinit var upgradeAccount: LinearLayout
  private lateinit var registerAccount: LinearLayout
  private lateinit var sendPin: LinearLayout
  private lateinit var network: LinearLayout
  private lateinit var historyPin: LinearLayout
  private lateinit var historyGrade: LinearLayout
  private lateinit var historyDoge: LinearLayout
  private lateinit var manualBot: LinearLayout
  private lateinit var autoBot: LinearLayout
  private var isOnQueue: Boolean = true

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)

    imageDoge = root.findViewById(R.id.imageViewLogoDoge)
    balance = root.findViewById(R.id.textViewBalance)
    pin = root.findViewById(R.id.textViewTotalPin)
    grade = root.findViewById(R.id.textViewGrade)
    balanceRemaining = root.findViewById(R.id.textViewRemainingBalance)
    targetBalance = root.findViewById(R.id.textViewTargetBalance)
    progressBar = root.findViewById(R.id.progressBar)
    sendBalance = root.findViewById(R.id.imageButtonSend)
    upgradeAccount = root.findViewById(R.id.linearLayoutUpgradeAccount)
    registerAccount = root.findViewById(R.id.linearLayoutRegisterAccount)
    sendPin = root.findViewById(R.id.linearLayoutSendPin)
    network = root.findViewById(R.id.linearLayoutNetwork)
    historyPin = root.findViewById(R.id.linearLayoutHistoryPin)
    historyGrade = root.findViewById(R.id.linearLayoutHistoryGrade)
    historyDoge = root.findViewById(R.id.linearLayoutHistoryDoge)
    manualBot = root.findViewById(R.id.linearLayoutManualStake)
    autoBot = root.findViewById(R.id.linearLayoutAutomaticStake)

    parentActivity = activity as NavigationActivity

    loading = Loading(parentActivity)
    user = User(parentActivity)
    bitCoinFormat = BitCoinFormat()
    var gradeValue: BigDecimal
    var gradeProgressValue: BigDecimal
    try {
      isOnQueue = user.getInteger("onQueue") > 0
      gradeValue = bitCoinFormat.decimalToDoge(user.getString("gradeTarget").toBigDecimal())
      gradeProgressValue = bitCoinFormat.decimalToDoge(user.getString("progressGrade").toBigDecimal())
    } catch (e: Exception) {
      isOnQueue = true
      gradeValue = bitCoinFormat.decimalToDoge(BigDecimal(0))
      gradeProgressValue = bitCoinFormat.decimalToDoge(BigDecimal(0))
    }

    progressBar.max = gradeValue.toInt()
    progressBar.progress = gradeProgressValue.toInt()
    balance.text = user.getString("balanceText")
    balanceRemaining.text = "${gradeProgressValue.toPlainString()} DOGE"
    targetBalance.text = "${gradeValue.toPlainString()} DOGE"
    pin.text = user.getInteger("pin").toString()
    grade.text = user.getString("gradeLevel")
    balanceValue = user.getString("balanceValue").toBigDecimal()

    sendBalance.setOnClickListener {
      goTo = Intent(parentActivity, SendBalanceActivity::class.java)
      startActivity(goTo)
    }

    sendPin.setOnClickListener {
      goTo = Intent(parentActivity, SendPinActivity::class.java)
      startActivity(goTo)
    }

    imageDoge.setOnClickListener {
      goTo = Intent(parentActivity, ReceivedActivity::class.java)
      startActivity(goTo)
    }

    balance.setOnClickListener {
      goTo = Intent(parentActivity, ReceivedActivity::class.java)
      startActivity(goTo)
    }

    upgradeAccount.setOnClickListener {
      goTo = Intent(parentActivity, UpgradeAccountActivity::class.java)
      goTo.putExtra("balanceValue", balanceValue)
      goTo.putExtra("pin", pin.text.toString())
      goTo.putExtra("gradeLevel", grade.text.toString())
      startActivity(goTo)
    }

    registerAccount.setOnClickListener {
      goTo = Intent(parentActivity, RegisterActivity::class.java)
      startActivity(goTo)
    }

    network.setOnClickListener {
      goTo = Intent(parentActivity, NetworkActivity::class.java)
      startActivity(goTo)
    }

    historyPin.setOnClickListener {
      goTo = Intent(parentActivity, HistoryPinActivity::class.java)
      startActivity(goTo)
    }

    historyGrade.setOnClickListener {
      goTo = Intent(parentActivity, HistoryGradeActivity::class.java)
      startActivity(goTo)
    }

    historyDoge.setOnClickListener {
      goTo = Intent(parentActivity, HistoryDogeActivity::class.java)
      startActivity(goTo)
    }

    manualBot.setOnClickListener {
      Toast.makeText(parentActivity, "under Constructor", Toast.LENGTH_SHORT).show()
    }

    autoBot.setOnClickListener {
      Toast.makeText(parentActivity, "under Constructor", Toast.LENGTH_SHORT).show()
    }

    validateQueue()

    return root
  }

  private fun validateQueue() {
    if (isOnQueue) {
      sendBalance.visibility = ImageButton.GONE
      upgradeAccount.visibility = LinearLayout.GONE
    } else {
      sendBalance.visibility = ImageButton.VISIBLE
      upgradeAccount.visibility = LinearLayout.VISIBLE
    }
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
    @SuppressLint("SetTextI18n")
    override fun onReceive(context: Context, intent: Intent) {
      val pinText = user.getInteger("pin")
      val gradeText = user.getString("gradeLevel")
      pin.text = pinText.toString()
      grade.text = gradeText
      val gradeProgressValue = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("progressGrade")))
      val gradeValue = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("gradeTarget")))

      isOnQueue = user.getInteger("onQueue") > 0

      balanceRemaining.text = "${gradeProgressValue.toPlainString()} DOGE"
      targetBalance.text = "${gradeValue.toPlainString()} DOGE"
      progressBar.max = gradeValue.toInt()
      progressBar.progress = gradeProgressValue.toInt()

      validateQueue()
    }
  }
  private var broadcastReceiverDoge: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance.text = user.getString("balanceText")
      balanceValue = intent.getSerializableExtra("balanceValue") as BigDecimal
    }
  }
}