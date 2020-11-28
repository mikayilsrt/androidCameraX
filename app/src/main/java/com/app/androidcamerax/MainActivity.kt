package com.app.androidcamerax

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File

class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val takePictureButton = findViewById<Button>(R.id.takePictureButton)
        takePictureButton.setOnClickListener {
            this.takePicture()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (this.allPermissionsGranted()) {
                this.startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Implement camera preview
     */
    private fun startCamera() {
        this.cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        this.cameraProviderFuture!!.addListener(Runnable {
            val cameraProvider = this.cameraProviderFuture!!.get()
            val cameraPreview: PreviewView = findViewById<PreviewView>(R.id.cameraPreview)

            this.preview = Preview.Builder().build()
            this.preview!!.setSurfaceProvider(cameraPreview.surfaceProvider)

            this.imageCapture = ImageCapture.Builder().build()

            this.cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

            cameraProvider.unbindAll()
            this.camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, this.cameraSelector!!, this.preview, this.imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Capture a picture from camera and save if imageCapture is not null.
     */
    private fun takePicture() {
        val imageCapture = this.imageCapture ?: return

        val imageFile = File(this.getOutputDirectory(), "${R.string.app_name}-${System.currentTimeMillis()}.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(applicationContext, "Image saved", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(applicationContext, "Error image not saved", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Get external media dirs if exist or create new folder.
     *
     * @return File
     */
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    /**
     * Check if permissions is granted.
     */
    private fun allPermissionsGranted() = REQUEST_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS: Int = 10
        private val REQUEST_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}