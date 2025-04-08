package com.example.productos

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ListaProductos()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemProducto(
    producto: Producto,
    onEditClick: (Producto) -> Unit = {},
    onDeleteClick: (Producto) -> Unit = {}
) {
    // Decodificar la imagen Base64
    val imageBitmap = remember(producto.imgBase64) {
        try {
            // Limpiar la cadena Base64
            val pureBase64 = producto.imgBase64
                .substringAfter("base64,")
                .replace("\n", "")
                .replace(" ", "")

            if (pureBase64.isEmpty()) {
                Log.e("ImageLoading", "Cadena Base64 vacía después de limpieza")
                return@remember null
            }

            Log.d("ImageLoading", "Base64 a decodificar: ${pureBase64.take(20)}... (${pureBase64.length} chars)")

            val decodedBytes = try {
                Base64.decode(pureBase64, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Log.e("ImageLoading", "Error decodificando Base64: ${e.message}")
                return@remember null
            }

            if (decodedBytes.isEmpty()) {
                Log.e("ImageLoading", "Bytes decodificados están vacíos")
                return@remember null
            }

            // Configurar opciones para la img
            val options = BitmapFactory.Options().apply {
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.RGB_565
                inJustDecodeBounds = false
            }

            val bitmap = try {
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
            } catch (e: Exception) {
                Log.e("ImageLoading", "Error creando Bitmap: ${e.message}")
                null
            }

            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("ImageLoading", "Error general: ${e.message}")
            null
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mostrar la imagen
            if (imageBitmap != null) {
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = "Imagen de ${producto.nombre}",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error cargando imagen", color = Color.White, fontSize = 12.sp)
                        Text(
                            "Tamaño Base64: ${producto.imgBase64.length} chars",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Detalles del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${"%.2f".format(producto.precio)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Botones
            Column {
                IconButton(
                    onClick = { onEditClick(producto) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { onDeleteClick(producto) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ListaProductos() {
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }

    val productos = remember {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM producto", null)
        val lista = mutableListOf<Producto>()

        try {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val imgBase64 = cursor.getString(cursor.getColumnIndexOrThrow("imagen"))

                lista.add(Producto(id, nombre, precio, descripcion, imgBase64))
            }
        } finally {
            cursor.close()
            db.close()
        }

        lista
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Implementar agregar producto */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Productos Disponibles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(productos) { producto ->
                    ItemProducto(
                        producto = producto,
                        onEditClick = { /* TODO: Implementar edición */ },
                        onDeleteClick = { /* TODO: Implementar eliminación */ }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaListaProductos() {
    ProductosTheme {
        ListaProductos()
    }
}