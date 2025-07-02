package com.example.souffleforcetest

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private var recorder: MediaRecorder? = null
    private lateinit var indicator: View
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 33L // 30 FPS pour meilleure réactivité
    private val PERMISSION_REQUEST_CODE = 200

    private val updateTask = object : Runnable {
        override fun run() {
            try {
                recorder?.let { rec ->
                    val maxAmplitude = rec.maxAmplitude
                    val height = min((maxAmplitude / 32767.0 * 800).toInt(), 800)
                    val layoutParams = indicator.layoutParams
                    layoutParams.height = if (height < 5) 5 else height // Minimum 5dp visible
                    indicator.layoutParams = layoutParams
                    indicator.requestLayout()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        indicator = findViewById(R.id.indicator)

        // Vérifier les permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                // Permission refusée - l'app ne peut pas fonctionner
                finish()
            }
        }
    }

    private fun startRecording() {
        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }
            handler.post(updateTask)
        } catch (e: Exception) {
            e.printStackTrace()
            // En cas d'erreur, fermer l'app
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTask)
        recorder?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder = null
    }
}
