package com.mirecargas

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_qr.*

class QR : AppCompatActivity() {

    private var multiFormatWriter = MultiFormatWriter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        setQR()
        setNumber()

    }

    private fun setNumber() {
        val number = intent.getStringExtra("number")
        text_number_qr.text = number
    }

    private fun setQR() {
        val customer = intent.getStringExtra("customer")

        val bitMatrix: BitMatrix = multiFormatWriter.encode(customer, BarcodeFormat.QR_CODE, 550, 550)
        val barcode = BarcodeEncoder()
        val bitMap = barcode.createBitmap(bitMatrix)
        image_qr.setImageBitmap(bitMap)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                val intent = Intent(this@QR, Inicio::class.java)
                intent.putExtra("customer", "")
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@QR, Inicio::class.java)
        intent.putExtra("customer", "")
        startActivity(intent)
        finish()
    }
}
