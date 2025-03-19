package com.example.acelerometro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var administradorSensor: SensorManager
    private var ultimaAceleracion = 9.8f
    private var ultimoTiempoGolpe = 0L
    private var golpeDetectado = mutableStateOf(false)
    private var mostrarPuntaje = mutableStateOf(false)
    private lateinit var vibrador: Vibrator
    private var reproductorMedia: MediaPlayer? = null
    private val mutex = Mutex()
    private val alcance = CoroutineScope(Dispatchers.Main)
    private var trabajoSaque: Job? = null
    private var trabajoPuntaje: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        administradorSensor = getSystemService(SensorManager::class.java)
        administradorSensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            administradorSensor.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        vibrador = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setContent {
            UIPrincipal(golpeDetectado.value, mostrarPuntaje.value)
        }

        iniciarSaqueRepetitivo()
    }

    override fun onSensorChanged(evento: SensorEvent?) {
        evento?.let {
            val aceleracion = sqrt(
                (it.values[0] * it.values[0] +
                        it.values[1] * it.values[1] +
                        it.values[2] * it.values[2]).toDouble()
            ).toFloat()

            val delta = aceleracion - ultimaAceleracion
            ultimaAceleracion = aceleracion

            if (System.nanoTime() - ultimoTiempoGolpe < 500_000_000) return

            when {
                delta > 15 -> {
                    registrarGolpe(R.raw.sonido3)
                }
                delta > 7 -> {
                    registrarGolpe(R.raw.sonido1)
                }
                golpeDetectado.value && delta < 1 -> {
                    golpeDetectado.value = false
                }
            }
        }
    }

    private fun registrarGolpe(sonido: Int) {
        golpeDetectado.value = true
        ultimoTiempoGolpe = System.nanoTime()


        cancelarSaque()
        cancelarPuntaje()

        reproducirGolpe(sonido)
        vibrar()


        trabajoPuntaje = alcance.launch {
            delay(3000)
            mostrarImagenPuntaje()
        }


        iniciarSaqueRepetitivo()
    }

    private fun mostrarImagenPuntaje() {
        mostrarPuntaje.value = true
        alcance.launch {
            reproducirSonido(R.raw.aplausos)
            delay(2000)
            mostrarPuntaje.value = false
        }
    }

    private fun vibrar() {
        if (vibrador.hasVibrator()) {
            val efectoVibracion = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrador.vibrate(efectoVibracion)
        }
    }

    private fun reproducirGolpe(sonido: Int) {
        if (reproductorMedia?.isPlaying == true) return
        alcance.launch {
            mutex.withLock {
                reproducirSonido(sonido)
            }
        }
    }

    private fun reproducirSonido(sonido: Int) {
        try {
            reproductorMedia?.release()
            reproductorMedia = MediaPlayer.create(this, sonido).apply {
                setOnCompletionListener {
                    release()
                    reproductorMedia = null
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun iniciarSaqueRepetitivo() {
        cancelarSaque()
        trabajoSaque = alcance.launch {
            while (true) {
                delay(3000)
                if (System.nanoTime() - ultimoTiempoGolpe > 3_000_000_000) {
                    mutex.withLock {
                        reproducirSonido(R.raw.saque)
                    }
                }
            }
        }
    }

    private fun cancelarSaque() {
        trabajoSaque?.cancel()
    }

    private fun cancelarPuntaje() {
        trabajoPuntaje?.cancel()
    }

    override fun onAccuracyChanged(sensor: Sensor?, precision: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        administradorSensor.unregisterListener(this)
        alcance.cancel()
        reproductorMedia?.release()
    }
}

@Composable
fun UIPrincipal(golpeDetectado: Boolean, mostrarPuntaje: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val imagen = when {
            mostrarPuntaje -> painterResource(id = R.drawable.puntaje)
            golpeDetectado -> painterResource(id = R.drawable.golpe)
            else -> painterResource(id = R.drawable.raqueta)
        }

        Image(
            painter = imagen,
            contentDescription = when {
                mostrarPuntaje -> "Puntaje"
                golpeDetectado -> "Golpe detectado"
                else -> "Raqueta"
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPrevia() {
    UIPrincipal(golpeDetectado = false, mostrarPuntaje = false)
}