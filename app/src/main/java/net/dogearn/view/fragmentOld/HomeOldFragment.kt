package net.dogearn.view.fragmentOld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import net.dogearn.R
import net.dogearn.config.BitCoinFormat
import net.dogearn.config.Loading
import net.dogearn.model.User
import net.dogearn.view.NavigationOldActivity
import net.dogearn.view.menu.*
import net.dogearn.view.menu.bot.manual.BotManualActivity
import net.dogearn.view.menuOld.ReceivedOldActivity
import java.math.BigDecimal

class HomeOldFragment : Fragment() {
  private lateinit var loading: Loading
  private lateinit var parentActivity: NavigationOldActivity
  private lateinit var user: User
  private lateinit var goTo: Intent
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var imageDoge: ImageView
  private lateinit var balance: TextView
  private lateinit var dollar: TextView
  private lateinit var pin: TextView
  private lateinit var grade: TextView
  private lateinit var balanceRemaining: TextView
  private lateinit var targetBalance: TextView
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal
  private lateinit var sendBalance: ImageButton
  private lateinit var upgradeAccount: LinearLayout
  private lateinit var registerAccount: LinearLayout
  private lateinit var sendPin: LinearLayout
  private lateinit var network: LinearLayout
  private lateinit var historyPin: LinearLayout
  private lateinit var historyGrade: LinearLayout
  private lateinit var historyDoge: LinearLayout
  private lateinit var historyDogeIn: LinearLayout
  private lateinit var historyDogeOut: LinearLayout
  private lateinit var manualBot: LinearLayout
  private lateinit var autoBot: LinearLayout
  private var isOnQueue: Boolean = true

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home_old, container, false)

    imageDoge = root.findViewById(R.id.imageViewLogoDoge)
    dollar = root.findViewById(R.id.textViewDollar)
    balance = root.findViewById(R.id.textViewBalance)
    pin = root.findViewById(R.id.textViewTotalPin)
    grade = root.findViewById(R.id.textViewGrade)
    balanceRemaining = root.findViewById(R.id.textViewRemainingBalance)
    targetBalance = root.findViewById(R.id.textViewTargetBalance)
    sendBalance = root.findViewById(R.id.imageButtonSend)
    upgradeAccount = root.findViewById(R.id.linearLayoutUpgradeAccount)
    registerAccount = root.findViewById(R.id.linearLayoutRegisterAccount)
    sendPin = root.findViewById(R.id.linearLayoutSendPin)
    network = root.findViewById(R.id.linearLayoutNetwork)
    historyPin = root.findViewById(R.id.linearLayoutHistoryPin)
    historyGrade = root.findViewById(R.id.linearLayoutHistoryGrade)
    historyDoge = root.findViewById(R.id.linearLayoutHistoryDoge)
    historyDogeIn = root.findViewById(R.id.linearLayoutHistoryDogeIn)
    historyDogeOut = root.findViewById(R.id.linearLayoutHistoryDogeOut)
    manualBot = root.findViewById(R.id.linearLayoutManualStake)
    autoBot = root.findViewById(R.id.linearLayoutAutomaticStake)

    parentActivity = activity as NavigationOldActivity

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

    balance.text = user.getString("balanceText")
    balanceRemaining.text = gradeProgressValue.toPlainString()
    targetBalance.text = gradeValue.toPlainString()
    pin.text = user.getInteger("pin").toString()
    grade.text = user.getString("gradeLevel")
    balanceValue = user.getString("balanceValue").toBigDecimal()
    dollarValue = user.getString("dollar").toBigDecimal()
    val totalDollar = bitCoinFormat.decimalToDoge(balanceValue) * dollarValue
    dollar.text = bitCoinFormat.toDollar(totalDollar).toPlainString()

    sendBalance.setOnClickListener {
      goTo = Intent(parentActivity, SendBalanceActivity::class.java)
      startActivity(goTo)
    }

    sendPin.setOnClickListener {
      goTo = Intent(parentActivity, SendPinActivity::class.java)
      startActivity(goTo)
    }

    imageDoge.setOnClickListener {
      goTo = Intent(parentActivity, ReceivedOldActivity::class.java)
      startActivity(goTo)
    }

    balance.setOnClickListener {
      goTo = Intent(parentActivity, ReceivedOldActivity::class.java)
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

    historyDogeIn.setOnClickListener {
      goTo = Intent(parentActivity, HistoryInActivity::class.java)
      startActivity(goTo)
    }

    historyDogeOut.setOnClickListener {
      goTo = Intent(parentActivity, HistoryOutActivity::class.java)
      startActivity(goTo)
    }

    manualBot.setOnClickListener {
      when {
        user.getBoolean("isUserWin") -> {
          Toast.makeText(parentActivity, "Playing stake can only be once a day", Toast.LENGTH_SHORT).show()
        }
        user.getString("gradeLevel").toInt() < user.getInteger("lot") -> {
          Toast.makeText(parentActivity, "Minimum lot to stake is LOT ${user.getInteger("lot")}", Toast.LENGTH_SHORT).show()
        }
        else -> {
          goTo = Intent(parentActivity, BotManualActivity::class.java)
          goTo.putExtra("balanceView", user.getString("balanceText"))
          goTo.putExtra("balance", user.getString("balanceValue"))
          goTo.putExtra("grade", grade.text.toString())
          startActivity(goTo)
        }
      }
    }

    autoBot.setOnClickListener {
      Toast.makeText(parentActivity, "Under Constructor", Toast.LENGTH_SHORT).show()
    }

    validateQueue()

    return root
  }

  private fun validateQueue() {
    if (isOnQueue) {
      sendBalance.visibility = ImageButton.GONE
      upgradeAccount.visibility = LinearLayout.GONE
      manualBot.visibility = ImageButton.GONE
      autoBot.visibility = LinearLayout.GONE
    } else {
      sendBalance.visibility = ImageButton.VISIBLE
      upgradeAccount.visibility = LinearLayout.VISIBLE
      manualBot.visibility = ImageButton.VISIBLE
      autoBot.visibility = LinearLayout.VISIBLE
    }
  }
}