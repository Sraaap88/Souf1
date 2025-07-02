package com.example.souffleforcetest

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
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
    private var isRecording = false

    private val updateTask = object : Runnable {
        override fun run() {
            try {
                if (isRecording && recorder != null) {
                    val maxAmplitude = recorder!!.maxAmplitude
                    val height = min((maxAmplitude / 32767.0 * 800).toInt(), 800)
                    val layoutParams = indicator.layoutParams
                    layoutParams.height = if (height < 5) 5 else height // Minimum 5dp visible
                    indicator.layoutParams = layoutParams
                    indicator.requestLayout()
                }
            } catch (e: Exception) {
                Log.e("SouffleApp", "Erreur dans updateTask: ${e.message}")
            }
            if (isRecording) {
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d("SouffleApp", "onCreate - Début")
            setContentView(R.layout.activity_main)
            indicator = findViewById(R.id.indicator)
            
            Log.d("SouffleApp", "Layout chargé, vérification permissions")
            
            // Vérifier les permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.d("SouffleApp", "Demande de permission")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            } else {
                Log.d("SouffleApp", "Permission OK, démarrage recording")
                startRecording()
            }
        } catch (e: Exception) {
            Log.e("SouffleApp", "Erreur dans onCreate: ${e.message}")
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SouffleApp", "Permission accordée")
                    startRecording()
                } else {
                    Log.d("SouffleApp", "Permission refusée")
                    Toast.makeText(this, "Permission microphone requise", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        } catch (e: Exception) {
            Log.e("SouffleApp", "Erreur dans onRequestPermissionsResult: ${e.message}")
        }
    }

    private fun startRecording() {
        try {
            Log.d("SouffleApp", "Démarrage MediaRecorder")
            
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
            Log.d("SouffleApp", "MediaRecorder démarré avec succès")
            
        } catch (e: Exception) {
            Log.e("SouffleApp", "Erreur dans startRecording: ${e.message}")
            Toast.makeText(this, "Erreur microphone: ${e.message}", Toast.LENGTH_LONG).show()
            // Ne pas fermer l'app, juste afficher l'erreur
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Log.d("SouffleApp", "onDestroy")
            isRecording = false
            handler.removeCallbacks(updateTask)
            recorder?.let {
                try {
                    it.stop()
                    it.release()
                } catch (e: Exception) {
                    Log.e("SouffleApp", "Erreur lors de l'arrêt: ${e.message}")
                }
            }
            recorder = null
        } catch (e: Exception) {
            Log.e("SouffleApp", "Erreur dans onDestroy: ${e.message}")
        }
    }
}
