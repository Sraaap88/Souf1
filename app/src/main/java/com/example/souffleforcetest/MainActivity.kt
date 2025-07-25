package com.example.souffleforcetest

import android.app.Activity
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    private var mediaRecorder: MediaRecorder? = null
    private var handler: Handler? = null
    private var animationHandler: Handler? = null
    private var organicLineView: OrganicLineView? = null
    private var isRecording = false
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val UPDATE_INTERVAL = 33L // 30 FPS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        organicLineView = findViewById(R.id.organicLineView)
        handler = Handler(Looper.getMainLooper())
        animationHandler = Handler(Looper.getMainLooper())
        
        // Démarrer l'animation continue
        startContinuousAnimation()
        
        // Vérifier et demander les permissions
        if (checkPermissions()) {
            startRecording()
            organicLineView?.startCycle()
        } else {
            requestPermissions()
        }
    }
    
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, 
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 
            PERMISSION_REQUEST_CODE)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
                organicLineView?.startCycle()
            }
        }
    }
    
    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }
            isRecording = true
            
            // Démarrer la mise à jour périodique
            updateAmplitude()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateAmplitude() {
        if (isRecording && mediaRecorder != null) {
            try {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                
                // CORRIGÉ : Amplification pour signaux faibles
                val normalizedAmplitude = minOf(amplitude / 32767.0f, 1.0f)
                
                // NOUVEAU : Amplifier x4 pour compenser les micros faibles
                val amplifiedForce = minOf(normalizedAmplitude * 4.0f, 1.0f)
                
                // Mettre à jour la vue avec le signal amplifié
                organicLineView?.updateForce(amplifiedForce)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Programmer la prochaine mise à jour
        handler?.postDelayed({ updateAmplitude() }, UPDATE_INTERVAL)
    }
    
    private fun startContinuousAnimation() {
        animationHandler?.post(object : Runnable {
            override fun run() {
                organicLineView?.invalidate()
                animationHandler?.postDelayed(this, 33) // 30 FPS
            }
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        handler?.removeCallbacksAndMessages(null)
        animationHandler?.removeCallbacksAndMessages(null)
    }
}
