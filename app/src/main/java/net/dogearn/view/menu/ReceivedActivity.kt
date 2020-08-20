package net.dogearn.view.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import net.dogearn.R
import net.dogearn.config.Loading
import net.dogearn.model.User

class ReceivedActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var imageQR: ImageView
  private lateinit var wallet: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_received)

    user = User(this)
    loading = Loading(this)

    imageQR = findViewById(R.id.imageViewQR)
    wallet = findViewById(R.id.textViewWallet)
    val barcodeEncoder = BarcodeEncoder()
    val bitmap = barcodeEncoder.encodeBitmap(user.getString("wallet"), BarcodeFormat.QR_CODE, 500, 500)
    imageQR.setImageBitmap(bitmap)
    wallet.text = user.getString("wallet")
  }
}