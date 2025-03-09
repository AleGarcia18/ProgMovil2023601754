package com.example.holatoast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holatoast.ui.theme.HolaToastTheme
import androidx.compose.material3.OutlinedTextField as OutlinedTextField
import org.w3c.dom.Text as Text


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
          UIPrincipal()
        }
    }
}

@Composable
fun UIPrincipal() {

    val contexto = LocalContext.current
    var nombre by rememberSaveable {
        mutableStateOf("")
    }
   Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "¿Cómo te llamas?",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Introduce tu nombre") },
            modifier = Modifier.fillMaxWidth() // Hace que el campo ocupetodo el ancho disponible
        )

        // Espacio entre el campo de texto y el botón
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Toast.makeText(contexto, "Hola, $nombre", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Saludar")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UIPrincipal()
}