package com.racing.controller.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import android.app.Activity
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.racing.controller.R

/**
 * Custom QR scanner activity providing a precise square reticle
 * with flashlight toggle and close button.
 */
class CustomScannerActivity : Activity() {

    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private var isTorchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)

        val btnTorch: ImageButton? = findViewById(R.id.btn_torch)
        val hasFlash = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlash) {
            btnTorch?.visibility = android.view.View.GONE
        } else {
            btnTorch?.setOnClickListener {
                isTorchOn = !isTorchOn
                if (isTorchOn) {
                    barcodeScannerView.setTorchOn()
                } else {
                    barcodeScannerView.setTorchOff()
                }
            }
        }

        val btnClose: ImageButton? = findViewById(R.id.btn_close_scanner)
        btnClose?.setOnClickListener {
            finish()
        }

        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.setShowMissingCameraPermissionDialog(true)
        capture.decode()
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
