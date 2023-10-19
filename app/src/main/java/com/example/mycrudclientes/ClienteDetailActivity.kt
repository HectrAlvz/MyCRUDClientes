package com.example.mycrudclientes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.io.IOException
import java.io.OutputStream

object ImageUtils {
    fun saveImageToInternalStorage(context: Context, imageBitmap: Bitmap): String? {
        val timeStamp = System.currentTimeMillis() // Genera un valor de tiempo único

        val fileName = "image_$timeStamp.png"

        var fileOutputStream: OutputStream? = null
        var imagePath: String? = null

        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            imagePath = context.getFileStreamPath(fileName).absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return imagePath
    }
}

class ClienteDetailActivity : AppCompatActivity() {
    private var clientId: Long = 0
    private lateinit var edNombre: EditText
    private lateinit var edDireccion: EditText
    private lateinit var edTelefono: EditText
    private lateinit var edCorreo: EditText
    private lateinit var edFotoUrl: EditText
    private lateinit var btnChooseImage: ImageButton
    private lateinit var dbHandler: ClientsDbHandler
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_detail)

        clientId = intent.getLongExtra("CLIENT_ID", 0)
        edNombre = findViewById(R.id.edNombre)
        edDireccion = findViewById(R.id.edDireccion)
        edTelefono = findViewById(R.id.edTelefono)
        edCorreo = findViewById(R.id.edCorreo)
        edFotoUrl = findViewById(R.id.edFotoUrl)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        dbHandler = ClientsDbHandler(this)

        if (clientId > 0) {
            val cliente = dbHandler.getClient(clientId)
            if (cliente != null) {
                edNombre.setText(cliente.nombre)
                edDireccion.setText(cliente.direccion)
                edTelefono.setText(cliente.telefono)
                edCorreo.setText(cliente.correo)
                edFotoUrl.setText(cliente.fotoUrl)

                if (!cliente.fotoUrl.isNullOrEmpty()) {
                    loadImage(cliente.fotoUrl!!)
                } else {
                    setDefaultImage()
                }
            }
        } else {
            setDefaultImage()
        }
    }

    fun chooseImage(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            imageBitmap = extras?.get("data") as Bitmap
            btnChooseImage.setImageBitmap(imageBitmap)
        }
    }

    fun searchImage(view: View) {
        val url = edFotoUrl.text.toString()
        if (url.isNotEmpty()) {
            loadImage(url)
        } else {
            Toast.makeText(this, "URL de imagen vacía", Toast.LENGTH_SHORT).show()
        }
    }

    fun save(view: View) {
        val nombre = edNombre.text.toString()
        val direccion = edDireccion.text.toString()
        val telefono = edTelefono.text.toString()
        val correo = edCorreo.text.toString()
        val fotoUrl = edFotoUrl.text.toString()

        // Validar que todos los campos sean obligatorios
        if (nombre.isNotEmpty() && direccion.isNotEmpty() && correo.isNotEmpty()) {
            if (telefono.matches(Regex("\\d{10}"))) {
                val cliente = Cliente(clientId, nombre, direccion, telefono, correo, fotoUrl)

                // Guardar la imagen en el almacenamiento interno
                if (imageBitmap != null) {
                    val imagePath = saveImageLocally(imageBitmap)
                    if (imagePath != null) {
                        cliente.fotoUrl = imagePath
                    }
                }

                // Actualizar o agregar el cliente
                if (clientId > 0) {
                    val success = dbHandler.updateClient(cliente)
                    if (success > 0) {
                        Toast.makeText(applicationContext, "Cliente actualizado", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    val success = dbHandler.addClient(cliente)
                    if (success > 0) {
                        Toast.makeText(applicationContext, "Cliente agregado", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Formato de teléfono incorrecto. Debe ser de 10 Digitos.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, "Todos los campos son obligatorios, incluyendo la foto", Toast.LENGTH_SHORT).show()
        }
    }

    fun delete(view: View) {
        if (clientId > 0) {
            val cliente = dbHandler.getClient(clientId) // Obtener el cliente
            if (cliente != null) {
                val deleted = dbHandler.deleteClient(cliente)
                if (deleted > 0) {
                    Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Error al eliminar el cliente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageLocally(imageBitmap: Bitmap?): String? {
        return if (imageBitmap != null) {
            val imagePath = ImageUtils.saveImageToInternalStorage(this, imageBitmap)
            if (imagePath != null) {
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
                imagePath
            } else {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                null
            }
        } else {
            Toast.makeText(this, "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun loadImage(url: String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.default_image)
            .error(R.drawable.default_image)
            .centerCrop()
            .transform(CropCircleTransformation())
            .into(btnChooseImage)
    }

    private fun setDefaultImage() {
        btnChooseImage.setImageResource(R.drawable.default_image)
    }
}

private operator fun Unit.compareTo(i: Int): Int {
    // Define your custom comparison logic here
    return when {
        i > 0 -> -1  // Unit is considered "smaller" than i
        i < 0 -> 1   // Unit is considered "greater" than i
        else -> 0    // They are considered equal
    }
}

//private fun formatPhoneNumber(phoneNumber: String): String {
//    var digits = phoneNumber.replace(Regex("[^\\d]"), "")
//    if (digits.length > 10) {
//        digits = digits.substring(0, 10)
//    }
//    if (digits.length > 6) {
//        digits = digits.substring(0, 6) + "-" + digits.substring(6)
//    }
//    if (digits.length > 3) {
//        digits = digits.substring(0, 3) + "-" + digits.substring(3)
//    }
//    return digits
//}