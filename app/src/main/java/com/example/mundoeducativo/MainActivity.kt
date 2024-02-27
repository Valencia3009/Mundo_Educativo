package com.example.mundoeducativo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa la instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inicialización de los componentes de la UI
        initUI()
    }

    private fun initUI() {
        // Obtiene referencias a los elementos de la UI
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnInvitado = findViewById<Button>(R.id.btnInvitado)
        val txtCorreo = findViewById<EditText>(R.id.txtCorreo)
        val txtContraseña = findViewById<EditText>(R.id.txtContraseña)
        val btnIngresar = findViewById<Button>(R.id.button)
        val txtRecuperarContraseña = findViewById<TextView>(R.id.txtRecuperarContraseña)

        // Manejador de clic para el botón Registrarse
        btnRegistrarse.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // Manejador de clic para el botón Continuar como Invitado
        btnInvitado.setOnClickListener {
            mostrarDialogoInvitado()
        }

        // Manejador de clic para el botón Ingresar
        btnIngresar.setOnClickListener {
            val email = txtCorreo.text.toString().trim()
            val password = txtContraseña.text.toString().trim()

            // Valida que los campos no estén vacíos
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Por favor, ingresa tus credenciales.", Toast.LENGTH_SHORT).show()
            }
        }

        // Manejador de clic para el texto "¿Olvidó su contraseña?"
        txtRecuperarContraseña.setOnClickListener {
            mostrarDialogoRecuperarContraseña()
        }
    }

    // Función para manejar el inicio de sesión
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    Toast.makeText(baseContext, "Autenticación exitosa.", Toast.LENGTH_SHORT).show()
                    // Navegar a la siguiente Activity aquí, si es necesario
                    val intent = Intent(this, Materias::class.java)
                    startActivity(intent)
                } else {
                    // Manejo de errores
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(baseContext, "Autenticación fallida: $errorMessage", Toast.LENGTH_LONG).show()
                    // Imprimir el error en el Logcat para diagnóstico
                    Log.e("AuthError", errorMessage)
                }
            }
    }

    // Función para mostrar el cuadro de diálogo de recuperación de contraseña
    private fun mostrarDialogoRecuperarContraseña() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperar Contraseña")

        // Crear un EditText para que el usuario ingrese su correo electrónico
        val input = EditText(this)
        input.hint = "Correo Electrónico"
        builder.setView(input)

        // Botón para enviar el correo de recuperación de contraseña
        builder.setPositiveButton("Enviar") { _, _ ->
            val email = input.text.toString().trim()

            if (email.isNotEmpty()) {
                enviarCorreoRecuperacion(email)
            } else {
                Toast.makeText(this, "Por favor, ingrese su correo electrónico.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    // Función para enviar el correo de recuperación de contraseña
    private fun enviarCorreoRecuperacion(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Se ha enviado un correo de recuperación a $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de recuperación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Función para mostrar el cuadro de diálogo de confirmación para continuar como invitado
    private fun mostrarDialogoInvitado() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Continuar como Invitado")
        builder.setMessage("Al continuar como invitado, tu progreso no se guardará y no podrás continuar en diferentes dispositivos.")

        builder.setPositiveButton("Continuar") { _, _ ->
            // Acción al hacer clic en Continuar
            val intent = Intent(this, Materias::class.java)
            startActivity(intent)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            // Acción al hacer clic en Cancelar (cerrar el cuadro de diálogo)
            dialog.dismiss()
        }

        builder.show()
    }
}