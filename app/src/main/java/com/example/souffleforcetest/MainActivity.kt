package com.example.souffleforcetest

import android.app.Activity
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.*

class MainActivity : Activity() {

    private var mediaRecorder: MediaRecorder? = null
    private var audioRecord: AudioRecord? = null
    private var handler: Handler? = null
    private var animationHandler: Handler? = null
    private var organicLineView: OrganicLineView? = null
    private var isRecording = false
    private var isAudioAnalyzing = false
    
    // Configuration audio pour FFT
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    private val fftSize = 2048
    private val audioBuffer = ShortArray(bufferSize)
    private val fftBuffer = FloatArray(fftSize)
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val UPDATE_INTERVAL = 33L // 30 FPS
        private const val AUDIO_UPDATE_INTERVAL = 50L // 20 FPS pour analyse audio
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
            startAudioAnalysis()
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
                startAudioAnalysis()
                // Redémarrer le timer jaune proprement après permission
                organicLineView?.restartCycle()
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
            
            // Démarrer la mise à jour périodique pour le souffle
            updateAmplitude()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startAudioAnalysis() {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            audioRecord?.startRecording()
            isAudioAnalyzing = true
            
            // Démarrer l'analyse des voyelles
            analyzeAudio()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateAmplitude() {
        if (isRecording && mediaRecorder != null) {
            try {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                
                // Normaliser l'amplitude (0-1)
                val normalizedAmplitude = minOf(amplitude / 32767.0f, 1.0f)
                
                // Mettre à jour la vue avec le souffle
                organicLineView?.updateForce(normalizedAmplitude)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Programmer la prochaine mise à jour
        handler?.postDelayed({ updateAmplitude() }, UPDATE_INTERVAL)
    }
    
    private fun analyzeAudio() {
        if (isAudioAnalyzing && audioRecord != null) {
            try {
                val bytesRead = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                
                if (bytesRead > 0) {
                    // Convertir en float et appliquer FFT
                    val vowelIntensities = performFFTAnalysis(audioBuffer, bytesRead)
                    
                    // Envoyer les intensités des voyelles à la vue
                    organicLineView?.updateVowelU(vowelIntensities.u)
                    organicLineView?.updateVowelA(vowelIntensities.a)
                    organicLineView?.updateVowelI(vowelIntensities.i)
                    organicLineView?.updateVowelO(vowelIntensities.o)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Programmer la prochaine analyse
        handler?.postDelayed({ analyzeAudio() }, AUDIO_UPDATE_INTERVAL)
    }
    
    data class VowelIntensities(
        val u: Float,
        val a: Float,
        val i: Float,
        val o: Float
    )
    
    private fun performFFTAnalysis(audioData: ShortArray, length: Int): VowelIntensities {
        // Préparer les données pour FFT
        val dataSize = minOf(length, fftSize)
        for (i in 0 until dataSize) {
            fftBuffer[i] = audioData[i].toFloat() / 32768.0f
        }
        
        // Remplir le reste avec des zéros
        for (i in dataSize until fftSize) {
            fftBuffer[i] = 0f
        }
        
        // Appliquer une fenêtre de Hamming pour réduire les artefacts
        applyHammingWindow(fftBuffer, dataSize)
        
        // Calculer la FFT
        val fftResult = performSimpleFFT(fftBuffer)
        
        // Analyser les fréquences pour détecter les voyelles
        return analyzeVowelFrequencies(fftResult)
    }
    
    private fun applyHammingWindow(data: FloatArray, size: Int) {
        for (i in 0 until size) {
            val window = 0.54 - 0.46 * cos(2.0 * PI * i / (size - 1))
            data[i] = (data[i] * window).toFloat()
        }
    }
    
    private fun performSimpleFFT(data: FloatArray): FloatArray {
        val n = data.size
        val magnitude = FloatArray(n / 2)
        
        // FFT simple basée sur la magnitude des fréquences
        for (k in 0 until n / 2) {
            var realSum = 0.0
            var imagSum = 0.0
            
            for (i in 0 until n) {
                val angle = -2.0 * PI * k * i / n
                realSum += data[i] * cos(angle)
                imagSum += data[i] * sin(angle)
            }
            
            magnitude[k] = sqrt(realSum * realSum + imagSum * imagSum).toFloat()
        }
        
        return magnitude
    }
    
    private fun analyzeVowelFrequencies(fftMagnitude: FloatArray): VowelIntensities {
        val freqResolution = sampleRate.toFloat() / fftSize
        
        // Définir les plages de fréquences pour chaque voyelle
        // Ces valeurs sont approximatives et peuvent être ajustées
        val uFreqRange = 250f..600f    // U : fréquences basses
        val aFreqRange = 600f..1200f   // A : fréquences moyennes-basses
        val oFreqRange = 400f..800f    // O : fréquences moyennes
        val iFreqRange = 2000f..3500f  // I : fréquences hautes
        
        var uIntensity = 0f
        var aIntensity = 0f
        var oIntensity = 0f
        var iIntensity = 0f
        
        // Calculer l'intensité totale pour normalisation
        var totalIntensity = 0f
        
        for (i in fftMagnitude.indices) {
            val frequency = i * freqResolution
            val magnitude = fftMagnitude[i]
            
            totalIntensity += magnitude
            
            when {
                frequency in uFreqRange -> uIntensity += magnitude
                frequency in aFreqRange -> aIntensity += magnitude
                frequency in oFreqRange -> oIntensity += magnitude
                frequency in iFreqRange -> iIntensity += magnitude
            }
        }
        
        // Normaliser les intensités (0-1) avec seuil minimum pour réduire le bruit
        val threshold = totalIntensity * 0.1f // Seuil à 10% de l'intensité totale
        
        return VowelIntensities(
            u = if (totalIntensity > 0) maxOf(0f, (uIntensity - threshold) / totalIntensity * 10f).coerceAtMost(1f) else 0f,
            a = if (totalIntensity > 0) maxOf(0f, (aIntensity - threshold) / totalIntensity * 10f).coerceAtMost(1f) else 0f,
            i = if (totalIntensity > 0) maxOf(0f, (iIntensity - threshold) / totalIntensity * 10f).coerceAtMost(1f) else 0f,
            o = if (totalIntensity > 0) maxOf(0f, (oIntensity - threshold) / totalIntensity * 10f).coerceAtMost(1f) else 0f
        )
    }
    
    private fun startContinuousAnimation() {
        animationHandler?.post(object : Runnable {
            override fun run() {
                // Forcer redessinage pour animation
                organicLineView?.invalidate()
                // Programmer le prochain redessinage
                animationHandler?.postDelayed(this, 33) // 30 FPS pour animation
            }
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Arrêter l'enregistrement MediaRecorder
        mediaRecorder?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Arrêter l'analyse audio
        isAudioAnalyzing = false
        audioRecord?.let {
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
