package com.morozione.bookmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import com.gnzlt.AndroidVisionQRReader.camera.CameraSourcePreview
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.lang.StringBuilder

class QRActivity : AppCompatActivity() {

    private var mBarcodeDetector: BarcodeDetector? = null
    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null

    private val isPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) === PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        mPreview = findViewById(R.id.cameraSourcePreview) as CameraSourcePreview

        if (isPermissionGranted) {
            setupBarcodeDetector()
            setupCameraSource()
        } else {
            requestPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPermissionGranted)
            startCameraSource()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    recreate()
                }
            } else {
                Toast.makeText(this, "This application needs Camera permission to read QR codes", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun setupBarcodeDetector() {
        mBarcodeDetector = BarcodeDetector.Builder(getApplicationContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        mBarcodeDetector!!.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.getDetectedItems()
                if (barcodes.size() != 0) {
                    val data = StringBuilder()
                    barcodes.forEach { key, value ->
                        data.append(value.displayValue)
                    }

                    Log.d(TAG, "Barcode detected: $data")
                    playBeep()

                    returnData(data.toString())
                }
            }
        })

        if (!mBarcodeDetector!!.isOperational())
            Log.w(TAG, "Detector dependencies are not yet available.")
    }

    private fun setupCameraSource() {
        mCameraSource = CameraSource.Builder(getApplicationContext(), mBarcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(15.0f)
            .setRequestedPreviewSize(1600, 1024)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun startCameraSource() {
        Log.d(TAG, "Camera Source started")
        if (mCameraSource != null) {
            try {
                mPreview!!.start(mCameraSource)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }

        }
    }

    private fun playBeep() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }

    private fun returnData(data: String?) {
        if (data != null) {
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_QR_RESULT, data)
            setResult(RESULT_OK, resultIntent)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (mPreview != null) {
            mPreview!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    companion object {

        val EXTRA_QR_RESULT = "EXTRA_QR_RESULT"
        private val TAG = "QRActivity"
        private val PERMISSIONS_REQUEST = 100
    }
}
