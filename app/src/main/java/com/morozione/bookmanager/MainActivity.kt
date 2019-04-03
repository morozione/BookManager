package com.morozione.bookmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var mResultTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mResultTextView = findViewById(R.id.result) as TextView
    }

    fun requestQRCodeScan(v: View) {
        val qrScanIntent = Intent(this, QRActivity::class.java)
        startActivityForResult(qrScanIntent, QR_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == QR_REQUEST) {
            val result: String
            if (resultCode == RESULT_OK) {
                result = data?.getStringExtra(QRActivity.EXTRA_QR_RESULT)!!
            } else {
                result = "Error"
            }
            mResultTextView!!.text = result
            mResultTextView!!.visibility = View.VISIBLE
        }
    }

    companion object {

        val QR_REQUEST = 111
    }
}
