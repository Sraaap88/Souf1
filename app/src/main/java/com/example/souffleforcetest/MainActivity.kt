package com.example.souffleforcetest

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast

class MainActivity : Activity() {

    private var recorder: MediaRecorder? = null
    private lateinit var indicator: View
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 33L
    private val PERMISSION_REQUEST_CODE = 200
    private var isRecording = false

    private val updateTask = object : Runnable {
        override fun run() {
            try {
                if (isRecording && recorder != null) {
                    val maxAmplitude = recorder!!.maxAmplitude
                    val height = kotlin.math.min((maxAmplitude / 32767.0 * 800).toInt(), 800)
                    val layoutParams = indicator.layoutParams
                    layoutParams.height = if (height < 10) 10 else height
                    indicator.layoutParams = layoutParams
                    indicator.requestLayout()
                }
            } catch (e: Exception) {
                // Ignore errors silently
            }
            if (isRecording) {
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        indicator = findViewById(R.id.indicator)
        
        // Vérifier permissions
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
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
                Toast.makeText(this, "Permission micro requise", Toast.LENGTH_SHORT).show()
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
            isRecording = true
            handler.post(updateTask)
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur micro", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        handler.removeCallbacks(updateTask)
        recorder?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        recorder = null
    }
}
