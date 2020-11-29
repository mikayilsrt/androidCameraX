package com.app.androidcamerax

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.otaliastudios.cameraview.*
import java.io.*

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val camera: CameraView = findViewById<CameraView>(R.id.camera)
        camera.setLifecycleOwner(this)

        camera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                savePicture(result)
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
            }
        })

        val takePictureButton: FloatingActionButton = findViewById<FloatingActionButton>(R.id.takePicture)
        takePictureButton.setOnClickListener { camera.takePicture() }
    }

    private fun savePicture(result: PictureResult) {
        try {
            val file: File = File(this.getOutputDirectory(), "${R.string.app_name}-${System.currentTimeMillis()}.jpg")
            val outputFile = FileOutputStream(file)
            outputFile.write(result.data)
            outputFile.close()
        } catch (ex: IOException) {
            throw IllegalArgumentException(ex.message.toString())
        }
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
}