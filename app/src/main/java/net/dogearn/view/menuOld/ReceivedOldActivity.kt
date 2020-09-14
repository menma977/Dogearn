package net.dogearn.view.menuOld

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.model.User

class ReceivedOldActivity : AppCompatActivity() {
  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var imageQR: ImageView
  private lateinit var wallet: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_received_old)

    user = User(this)
    loading = Loading(this)

    imageQR = findViewById(R.id.imageViewQR)
    wallet = findViewById(R.id.textViewWallet)
    val barcodeEncoder = BarcodeEncoder()
    val bitmap = barcodeEncoder.encodeBitmap(user.getString("wallet"), BarcodeFormat.QR_CODE, 500, 500)
    imageQR.setImageBitmap(bitmap)
    wallet.text = user.getString("wallet")

    wallet.setOnClickListener {
      clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipData = ClipData.newPlainText("Wallet", wallet.text.toString())
      clipboardManager.setPrimaryClip(clipData)
      Toast.makeText(applicationContext, "Doge wallet has been copied", Toast.LENGTH_LONG).show()
    }
  }
}